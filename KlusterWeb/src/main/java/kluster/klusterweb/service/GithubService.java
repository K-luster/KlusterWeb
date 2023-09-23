package kluster.klusterweb.service;

import kluster.klusterweb.config.jwt.JwtTokenProvider;
import kluster.klusterweb.domain.Member;
import kluster.klusterweb.dto.GitHubRepository;
import kluster.klusterweb.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.errors.*;
import org.eclipse.jgit.lib.Repository;
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
public class GithubService {

    private static final String GITHUB_REPO_URL = "https://api.github.com/user/repos";
    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;

    public String getGithubAccessToken(String accessToken) {
        String email = jwtTokenProvider.extractSubjectFromJwt(accessToken);
        Optional<Member> member = memberRepository.findByEmail(email);
        if (member.isPresent()) {
            Member findMember = member.get();
            return findMember.getGithubAccessToken();
        } else {
            throw new RuntimeException("존재하지 않는 이메일입니다.");
        }
    }

    public ResponseEntity<String> createRepository(String githubToken, String repositoryName) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "token " + githubToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        String jsonBody = "{\"name\":\"" + repositoryName + "\"}";
        HttpEntity<String> requestEntity = new HttpEntity<>(jsonBody, headers);
        ResponseEntity<String> responseEntity = restTemplate.exchange(GITHUB_REPO_URL, HttpMethod.POST, requestEntity, String.class);
        return responseEntity;
    }

    public void commitAndPush(String githubAccessToken, String repositoryName, String branchName) throws Exception {
        String localRepositoryPath = "/Users/kimtaeheon/Documents/GitHub/githubActionTest/githubActionTest";
        String githubUsername = "Jake-huen";
        String dockerfileContent = "FROM openjdk:11\n" +
                "ARG JAR_FILE=build/libs/*.jar\n" +
                "COPY ${JAR_FILE} app.jar\n" +
                "ENTRYPOINT [\"java\",\"-jar\",\"/app.jar\"]\n" +
                "\n";
        String dockerfilePath = localRepositoryPath + "/Dockerfile";
        String commitMessage = "Add Dockerfile";

        try {
            // Dockerfile 생성
            FileWriter writer = new FileWriter(dockerfilePath);
            writer.write(dockerfileContent);
            writer.close();

            // 로컬 Git 레포지토리 열기
            Repository repository = Git.open(new File(localRepositoryPath)).getRepository();

            // Git 객체 생성
            Git git = new Git(repository);

            // 파일을 스테이징하고 커밋
            git.add().addFilepattern(".").call();
            git.commit().setMessage(commitMessage).call();

            // 푸시
            CredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider(githubUsername, githubAccessToken);
            PushCommand pushCommand = git.push().setCredentialsProvider(credentialsProvider);
            pushCommand.call();

            System.out.println("Dockerfile committed and pushed successfully.");
        } catch (IOException | GitAPIException e) {
            e.printStackTrace();
        }

        commitAndPushGithubAction(githubAccessToken);
    }

    public void commitAndPushGithubAction(String githubAccessToken) {
        String localRepositoryPath = "/Users/kimtaeheon/Documents/GitHub/githubActionTest/githubActionTest";
        String githubUsername = "Jake-huen";
        String actionContent = "name: CI with gradle\n" +
                "\n" +
                "on:\n" +
                "  push:\n" +
                "    branches:\n" +
                "      - main\n" +
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
                "    - name: Grant execute permission for gradlew and build\n" +
                "      run: |\n" +
                "        chmod +x gradlew\n" +
                "        ./gradlew build\n" +
                "      \n" +
                "    - name: Set up JDK 11\n" +
                "      uses: actions/setup-java@v3\n" +
                "      with:\n" +
                "        java-version: '11'\n" +
                "        distribution: 'temurin'\n" +
                "        \n" +
                "    - name: init with Gradle\n" +
                "      uses: gradle/gradle-build-action@v2\n" +
                "\n" +
                "    - name: Build with Gradle\n" +
                "      run: |\n" +
                "        chmod +x ./gradlew\n" +
                "        ./gradlew clean build\n" +
                "\n" +
                "    - name: Set up Docker Buildx\n" +
                "      uses: docker/setup-buildx-action@v1\n" +
                "      with:\n" +
                "        version: v0.7.0\n" +
                "\n" +
                "    - name: Docker build & push to docker repo\n" +
                "      run: |\n" +
                "          docker login -u ${{ secrets.DOCKER_HUB_USERNAME }} -p ${{ secrets.DOCKER_HUB_PASSWORD }}\n" +
                "          docker build -t jakeheon/githubaction -f Dockerfile .\n" +
                "          docker push jakeheon/githubaction\n" +
                "\n" +
                "    - name: Build and deploy\n" +
                "      uses: appleboy/ssh-action@master\n" +
                "      id: deploy\n" +
                "      with:\n" +
                "        host: ${{ secrets.HOST }}\n" +
                "        username: ubuntu\n" +
                "        key: ${{ secrets.KEY }}\n" +
                "        envs: GITHUB_SHA\n" +
                "        script: |\n" +
                "          sudo docker stop $(sudo docker ps -aq)\n" +
                "          sudo docker rm -f $(docker ps -qa)\n" +
                "          sudo docker pull jakeheon/githubaction\n" +
                "          sudo docker run -it -d -p 80:8080 jakeheon/githubaction";
        String githubActionfilePath = localRepositoryPath + "/.github/workflows/CI.yml";
        String commitMessage = "Add githubActionFile";

        try {
            // githubAction 파일 생성
            FileWriter writer = new FileWriter(githubActionfilePath);
            writer.write(actionContent);
            writer.close();

            // 로컬 Git 레포지토리 열기
            Repository repository = Git.open(new File(localRepositoryPath)).getRepository();

            // Git 객체 생성
            Git git = new Git(repository);

            // 파일을 스테이징하고 커밋
            git.add().addFilepattern(".").call();
            git.commit().setMessage(commitMessage).call();

            // 푸시
            CredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider(githubUsername, githubAccessToken);
            PushCommand pushCommand = git.push().setCredentialsProvider(credentialsProvider);
            pushCommand.call();

            System.out.println("Dockerfile committed and pushed successfully.");
        } catch (IOException | GitAPIException e) {
            e.printStackTrace();
        }
    }

    public void deleteRepository(String githubToken, String repositoryName){
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


    public List<GitHubRepository> getAllRepository(String githubAccessToken, String username) throws RuntimeException, IOException {
        String apiUrl = "https://api.github.com/users/" + username + "/repos";

        // API 요청을 보내기 위한 URL 생성
        URL url = new URL(apiUrl);

        // HttpURLConnection을 사용하여 API 요청 설정
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Authorization", "token " + githubAccessToken);

        // API 응답을 읽기 위한 BufferedReader 설정
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
}
