package kluster.klusterweb.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import kluster.klusterweb.config.jwt.JwtTokenProvider;
import kluster.klusterweb.domain.Member;
import kluster.klusterweb.dto.GitHubRepository;
import kluster.klusterweb.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.errors.*;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.net.*;
import java.util.*;

@Service
@RequiredArgsConstructor
@Log4j2
public class GithubService {

    private static final String GITHUB_REPO_URL = "https://api.github.com/user/repos";
    private final String GITHUB_API_URL = "https://api.github.com/user";
    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;

    public String getGithubAccessToken(String jwtToken) {
        String email = jwtTokenProvider.extractSubjectFromJwt(jwtToken);
        Optional<Member> member = memberRepository.findByEmail(email);
        if (member.isPresent()) {
            Member findMember = member.get();
            return findMember.getGithubAccessToken();
        }
        throw new RuntimeException("존재하지 않는 이메일입니다.");
    }


    public String getUserIdFromAccessToken(String githubAccessToken) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + githubAccessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<String> responseEntity = restTemplate.exchange(
                GITHUB_API_URL,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        );
        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            String response = responseEntity.getBody();
            return extractUserIdFromResponse(response);
        } else {
            return null;
        }
    }

    public String extractUserIdFromResponse(String response) {
         ObjectMapper objectMapper = new ObjectMapper();
         try {
             Map<String, Object> info = objectMapper.readValue(response, new TypeReference<Map<String, Object>>() {});
             return info.get("login").toString();
         } catch (IOException e) {
             e.printStackTrace();
             return null;
         }
    }

    public String createRepository(String jwtToken, String repositoryName) {
        String githubAccessToken = getGithubAccessToken(jwtToken);

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "token " + githubAccessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        String jsonBody = "{\"name\":\"" + repositoryName + "\"}";
        HttpEntity<String> requestEntity = new HttpEntity<>(jsonBody, headers);
        ResponseEntity<String> responseEntity = restTemplate.exchange(GITHUB_REPO_URL, HttpMethod.POST, requestEntity, String.class);
        return responseEntity.getBody().toString();
    }

    public String buildDockerAndGithubAction(String jwtToken, String repositoryName, String localRepositoryPath, String branchName) throws Exception {
        String email = jwtTokenProvider.extractSubjectFromJwt(jwtToken);
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("해당하는 이메일이 없습니다."));
        String githubUsername = member.getGithubName();
        String githubAccessToken = getGithubAccessToken(jwtToken);
        String dockerhubUsername = member.getDockerHubUsername();
        String dockerhubPassword = member.getDockerHubPassword();

        createDevelopBranch(localRepositoryPath, branchName);

        String javaDockerfileContent = String.format("FROM openjdk:11\n" +
                "ARG JAR_FILE=*.jar\n" +
                "COPY ${JAR_FILE} app.jar\n" +
                "ENTRYPOINT [\"java\",\"-jar\",\"/app.jar\"]\n" +
                "\n");

        String dockerfilePath = localRepositoryPath + "/Dockerfile";
        String commitMessage = "Add Dockerfile";

        try {
            FileWriter writer = new FileWriter(dockerfilePath);
            writer.write(javaDockerfileContent);
            writer.close();
            Repository repository = Git.open(new File(localRepositoryPath)).getRepository();
            Git git = new Git(repository);
            git.checkout()
                    .setName("develop") // 푸시할 브랜치 이름을 지정
                    .call();
            git.add().addFilepattern(".").call();
            git.commit().setMessage(commitMessage).call();
            CredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider(githubUsername, githubAccessToken);
            PushCommand pushCommand = git.push().setCredentialsProvider(credentialsProvider);
            pushCommand.call();

            System.out.println("java Dockerfile 커밋 푸쉬 성공");
        } catch (IOException | GitAPIException e) {
            e.printStackTrace();
        }

        commitAndPushGithubAction(localRepositoryPath, branchName, githubAccessToken, githubUsername, dockerhubUsername, dockerhubPassword, repositoryName);
        return "CI 성공";
    }

    public void createDevelopBranch(String localRepositoryPath, String branchName){
        File repositoryDirectory = new File(localRepositoryPath);

        String startPoint = "main"; // 새 브랜치의 시작 지점

        try {
            Git git = Git.open(repositoryDirectory);

            Ref branchRef = git.branchCreate()
                    .setName(branchName)
                    .setStartPoint(startPoint)
                    .call();
            git.checkout()
                    .setName("develop") // 브랜치 이름 지정
                    .call();
            System.out.println("새로운 브랜치가 생성되었습니다:");
            System.out.println("브랜치 이름: " + branchRef.getName());

            git.close();
        } catch (IOException | GitAPIException e) {
            e.printStackTrace();
        }
    }

    public void commitAndPushGithubAction(String localRepositoryPath, String branchName, String githubAccessToken, String githubUsername, String dockerhubUsername, String dockerhubPassword, String repositoryName) {
        String actionContent = String.format("\n" +
                "name: CI with Gradle\n" +
                "\n" +
                "on:\n" +
                "  push:\n" +
                "    branches:\n" +
                "      - %s\n" +
                "\n" +
                "permissions:\n" +
                "  contents: read\n" +
                "\n" +
                "jobs:\n" +
                "  build:\n" +
                "\n" +
                "    runs-on: ubuntu-latest\n" +
                "\n" +
                "    steps:\n" +
                "    - uses: actions/checkout@v3\n" +
                "\n" +
                "    - name: Set up Docker Buildx\n" +
                "      uses: docker/setup-buildx-action@v1\n" +
                "      with:\n" +
                "        version: v0.7.0\n" +
                "\n" +
                "    - name: Docker build & push to docker repo\n" +
                "      run: |\n" +
                "          docker login -u %s -p %s\n" +
                "          docker build -t %s/%s -f Dockerfile .\n" +
                "          docker push %s/%s\n" +
                "\n", branchName, dockerhubUsername, dockerhubPassword, dockerhubUsername, repositoryName, dockerhubUsername, repositoryName);

        String directoryPath = localRepositoryPath + "/.github/workflows"; // 디렉터리 경로 지정
        String filePath = directoryPath + "/myworkflow.yaml"; // 파일 경로 지정

        File directory = new File(directoryPath);

        // 디렉터리가 존재하지 않으면 생성
        if (!directory.exists()) {
            if (directory.mkdirs()) {
                System.out.println("디렉터리가 생성되었습니다.");
            } else {
                System.err.println("디렉터리 생성에 실패했습니다.");
                return;
            }
        }

        File file = new File(filePath);

        // 파일이 존재하지 않으면 생성
        if (!file.exists()) {
            try {
                if (file.createNewFile()) {
                    FileWriter writer = new FileWriter(file);
                    writer.write(actionContent);
                    writer.close();
                    String commitMessage = "Add githubActionFile";
                    Repository repository = Git.open(new File(localRepositoryPath)).getRepository();
                    Git git = new Git(repository);
                    git.checkout()
                            .setName("develop") // 푸시할 브랜치 이름을 지정
                            .call();
                    git.add().addFilepattern(".").call();
                    git.commit().setMessage(commitMessage).call();
                    CredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider(githubUsername, githubAccessToken);
                    PushCommand pushCommand = git.push().setCredentialsProvider(credentialsProvider);
                    pushCommand.call();
                    System.out.println("파일이 생성되었습니다.");
                } else {
                    System.err.println("파일 생성에 실패했습니다.");
                }
            } catch (IOException | GitAPIException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("파일이 이미 존재합니다.");
        }
    }

    public void deleteRepository(String githubToken, String repositoryName) {
        String apiUrl = GITHUB_REPO_URL + "jake-huen/" + repositoryName;
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + githubToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        RequestEntity<Void> requestEntity = new RequestEntity<>(headers, HttpMethod.DELETE, URI.create(apiUrl));
        ResponseEntity<Void> responseEntity = restTemplate.exchange(requestEntity, Void.class);
        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            System.out.println("리포지토리가 성공적으로 삭제되었습니다.");
        } else {
            System.err.println("리포지토리 삭제 실패: " + responseEntity.getStatusCode());
        }
    }


    public List<GitHubRepository> getAllRepository(String jwtToken) throws RuntimeException, IOException {
        String email = jwtTokenProvider.extractSubjectFromJwt(jwtToken);
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("해당하는 이메일이 없습니다."));
        String apiUrl = "https://api.github.com/users/" + member.getGithubName() + "/repos";
        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Authorization", "token " + member.getGithubAccessToken());
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder jsonResponse = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            jsonResponse.append(line);
        }
        JSONArray jsonArray = new JSONArray(jsonResponse.toString());
        List<GitHubRepository> repositories = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonRepo = jsonArray.getJSONObject(i);
            GitHubRepository repository = new GitHubRepository();
            repository.setName(jsonRepo.getString("name"));
            repository.setDescription(jsonRepo.get("description").toString());
            repositories.add(repository);
        }
        reader.close();
        connection.disconnect();
        return repositories;
    }

    public String deploy(String jwtToken, String localRepositoryPath, String repositoryName, String serviceName, String replicaCount) {
        String email = jwtTokenProvider.extractSubjectFromJwt(jwtToken);
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("해당하는 이메일이 없습니다."));
        String githubUsername = member.getGithubName();
        String githubAccessToken = getGithubAccessToken(jwtToken);
        String dockerhubUsername = member.getDockerHubUsername();
        String dockerhubPassword = member.getDockerHubPassword();
        commitAndPushDeploymentYml(localRepositoryPath, githubUsername, githubAccessToken, serviceName, replicaCount, dockerhubUsername);
        commitAndPushServiceYml(localRepositoryPath, githubUsername, githubAccessToken, serviceName);
        commitAndPushHpaTestYml(localRepositoryPath, githubUsername, githubAccessToken, serviceName);
        return null;
    }

    private void commitAndPushDeploymentYml(String localRepositoryPath, String githubUsername, String githubAccessToken, String serviceName, String replicaCount, String dockerhubUsername) {
        String deploymentYmlContent = String.format("apiVersion: apps/v1\n" +
                "kind: Deployment\n" +
                "metadata:\n" +
                "  name: %s\n" +
                "spec:\n" +
                "  selector:\n" +
                "    matchLabels:\n" +
                "      app: %s\n" +
                "  replicas: %s\n" +
                "  template:\n" +
                "    metadata:\n" +
                "      labels:\n" +
                "        app: %s\n" +
                "    spec:\n" +
                "      containers:\n" +
                "        - name: core\n" +
                "          image: %s/%s\n" +
                "          imagePullPolicy: Always\n" +
                "          ports:\n" +
                "            - containerPort: 8080\n" +
                "              protocol: TCP\n" +
                "          resources:\n" +
                "            requests:\n" +
                "              cpu: 500m\n" +
                "              memory: 1000Mi", serviceName, serviceName, replicaCount, serviceName, dockerhubUsername, serviceName);
        String directoryPath = localRepositoryPath; // 디렉터리 경로 지정

        String filePath = directoryPath + "/deployment.yml"; // 파일 경로 지정

        File directory = new File(directoryPath);

        // 디렉터리가 존재하지 않으면 생성
        if (!directory.exists()) {
            if (directory.mkdirs()) {
                System.out.println("디렉터리가 생성되었습니다.");
            } else {
                System.err.println("디렉터리 생성에 실패했습니다.");
                return;
            }
        }

        File file = new File(filePath);

        // 파일이 존재하지 않으면 생성
        if (!file.exists()) {
            try {
                if (file.createNewFile()) {
                    FileWriter writer = new FileWriter(file);
                    writer.write(deploymentYmlContent);
                    writer.close();
                    String commitMessage = "Add deploymentYmlContent";
                    Repository repository = Git.open(new File(localRepositoryPath)).getRepository();
                    Git git = new Git(repository);
                    git.checkout()
                            .setName("develop") // 푸시할 브랜치 이름을 지정
                            .call();
                    git.add().addFilepattern(".").call();
                    git.commit().setMessage(commitMessage).call();
                    CredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider(githubUsername, githubAccessToken);
                    PushCommand pushCommand = git.push().setCredentialsProvider(credentialsProvider);
                    pushCommand.call();
                    System.out.println("파일이 생성되었습니다.");
                } else {
                    System.err.println("파일 생성에 실패했습니다.");
                }
            } catch (IOException | GitAPIException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("파일이 이미 존재합니다.");
        }
    }

    private void commitAndPushServiceYml(String localRepositoryPath, String githubUsername, String githubAccessToken, String serviceName) {
        String serviceYmlContent = String.format("apiVersion: v1\n" +
                "kind: Service\n" +
                "metadata:\n" +
                "  name: %s\n" +
                "spec:\n" +
                "  type: NodePort\n" +
                "  ports:\n" +
                "    - port: 80\n" +
                "      protocol: TCP\n" +
                "      targetPort: 8080\n" +
                "  selector:\n" +
                "    app: %s", serviceName, serviceName);
        String directoryPath = localRepositoryPath; // 디렉터리 경로 지정

        String filePath = directoryPath + "/service.yml"; // 파일 경로 지정

        File directory = new File(directoryPath);

        // 디렉터리가 존재하지 않으면 생성
        if (!directory.exists()) {
            if (directory.mkdirs()) {
                System.out.println("디렉터리가 생성되었습니다.");
            } else {
                System.err.println("디렉터리 생성에 실패했습니다.");
                return;
            }
        }

        File file = new File(filePath);

        // 파일이 존재하지 않으면 생성
        if (!file.exists()) {
            try {
                if (file.createNewFile()) {
                    FileWriter writer = new FileWriter(file);
                    writer.write(serviceYmlContent);
                    writer.close();
                    String commitMessage = "Add serviceYmlContent";
                    Repository repository = Git.open(new File(localRepositoryPath)).getRepository();
                    Git git = new Git(repository);
                    git.checkout()
                            .setName("develop") // 푸시할 브랜치 이름을 지정
                            .call();
                    git.add().addFilepattern(".").call();
                    git.commit().setMessage(commitMessage).call();
                    CredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider(githubUsername, githubAccessToken);
                    PushCommand pushCommand = git.push().setCredentialsProvider(credentialsProvider);
                    pushCommand.call();
                    System.out.println("파일이 생성되었습니다.");
                } else {
                    System.err.println("파일 생성에 실패했습니다.");
                }
            } catch (IOException | GitAPIException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("파일이 이미 존재합니다.");
        }
    }

    private void commitAndPushHpaTestYml(String localRepositoryPath, String githubUsername, String githubAccessToken, String serviceName) {
        String hpaTestContent = String.format("apiVersion: autoscaling/v2beta1\n" +
                "kind: HorizontalPodAutoscaler\n" +
                "metadata:\n" +
                "  name: %s\n" +
                "spec:\n" +
                "  minReplicas: 1\n" +
                "  maxReplicas: 5\n" +
                "  metrics:\n" +
                "  - resource:\n" +
                "      name: cpu \n" +
                "      targetAverageUtilization: 10\n" +
                "    type: Resource\n" +
                "  scaleTargetRef:\n" +
                "    apiVersion: apps/v1\n" +
                "    kind: Deployment\n" +
                "    name: devops-spring-deployment\n", serviceName);
        String directoryPath = localRepositoryPath; // 디렉터리 경로 지정

        String filePath = directoryPath + "/hpa-test.yml"; // 파일 경로 지정

        File directory = new File(directoryPath);

        // 디렉터리가 존재하지 않으면 생성
        if (!directory.exists()) {
            if (directory.mkdirs()) {
                System.out.println("디렉터리가 생성되었습니다.");
            } else {
                System.err.println("디렉터리 생성에 실패했습니다.");
                return;
            }
        }

        File file = new File(filePath);

        // 파일이 존재하지 않으면 생성
        if (!file.exists()) {
            try {
                if (file.createNewFile()) {
                    FileWriter writer = new FileWriter(file);
                    writer.write(hpaTestContent);
                    writer.close();
                    String commitMessage = "Add hpaTestContent";
                    Repository repository = Git.open(new File(localRepositoryPath)).getRepository();
                    Git git = new Git(repository);
                    git.checkout()
                            .setName("develop") // 푸시할 브랜치 이름을 지정
                            .call();
                    git.add().addFilepattern(".").call();
                    git.commit().setMessage(commitMessage).call();
                    CredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider(githubUsername, githubAccessToken);
                    PushCommand pushCommand = git.push().setCredentialsProvider(credentialsProvider);
                    pushCommand.call();
                    System.out.println("파일이 생성되었습니다.");
                } else {
                    System.err.println("파일 생성에 실패했습니다.");
                }
            } catch (IOException | GitAPIException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("파일이 이미 존재합니다.");
        }
    }
}
