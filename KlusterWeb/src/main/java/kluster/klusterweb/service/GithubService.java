package kluster.klusterweb.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import kluster.klusterweb.config.jwt.JwtTokenProvider;
import kluster.klusterweb.domain.Member;
import kluster.klusterweb.domain.Project;
import kluster.klusterweb.dto.Github.GitHubRepository;
import kluster.klusterweb.repository.MemberRepository;
import kluster.klusterweb.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.*;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

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
    private final ProjectRepository projectRepository;
    private final CIService CIService;
    private final CDService CDService;
    private final EncryptService encryptService;
    private static final String NEW_IMAGE_PREFIX = "docku.ddns.net/library/";

    public String getGithubAccessToken(String jwtToken) {
        String email = jwtTokenProvider.extractSubjectFromJwt(jwtToken);
        Optional<Member> member = memberRepository.findByEmail(email);
        if (member.isPresent()) {
            Member findMember = member.get();
            return encryptService.decrypt(findMember.getGithubAccessToken());
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
            Map<String, Object> info = objectMapper.readValue(response, new TypeReference<>() {
            });
            return info.get("login").toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String createGitHubRepository(String jwtToken, String repositoryName, String localPath) throws IOException, GitAPIException {
        String githubAccessToken = getGithubAccessToken(jwtToken);
        String githubRepoUrl = createGithubRepositoryAndGetUrl(githubAccessToken, repositoryName);
        cloneLocalGitRepository(githubRepoUrl, localPath, githubAccessToken, repositoryName);
        initializeAndCommitLocalGitRepository(localPath, repositoryName);
        configureRemoteAndPush(localPath, githubRepoUrl, githubAccessToken, repositoryName);
        return "Success";
    }

    private String createGithubRepositoryAndGetUrl(String githubAccessToken, String repositoryName) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "token " + githubAccessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        String jsonBody = "{\"name\":\"" + repositoryName + "\"}";
        HttpEntity<String> requestEntity = new HttpEntity<>(jsonBody, headers);
        try {
            ResponseEntity<String> responseEntity = restTemplate.exchange(GITHUB_REPO_URL, HttpMethod.POST, requestEntity, String.class);
        } catch (Exception exception) {
            // 예외 처리: 리포지토리 생성 실패 또는 GitHub API 응답 실패
            exception.printStackTrace();
            throw new RuntimeException("Repository creation failed or GitHub API response failed");
        }

        String githubUserName = getUserIdFromAccessToken(githubAccessToken);
        return "https://github.com/" + githubUserName + "/" + repositoryName + ".git";
    }

    private void cloneLocalGitRepository(String githubRepoUrl, String localPath, String githubAccessToken, String repositoryName) throws GitAPIException {
        File localRepoPath = new File(localPath + "/" + repositoryName);
        Git.cloneRepository()
                .setURI(githubRepoUrl)
                .setDirectory(localRepoPath)
                .setCredentialsProvider(new UsernamePasswordCredentialsProvider(githubAccessToken, ""))
                .call();
    }

    private void initializeAndCommitLocalGitRepository(String localPath, String repositoryName) throws GitAPIException, IOException {
        File localRepoPath = new File(localPath + "/" + repositoryName);
        Git.init()
                .setDirectory(localRepoPath)
                .call();
        System.out.println("Git 초기화 완료");
        File readmeFile = new File(localRepoPath, "README.md");
        readmeFile.createNewFile();
        try (Git git = Git.open(localRepoPath)) {
            git.add().addFilepattern(".").call();
            git.commit().setMessage("first commit").call();
            System.out.println("Git 커밋 성공");
        }
    }

    private void configureRemoteAndPush(String localPath, String githubRepoUrl, String githubAccessToken, String repositoryName) throws IOException {
        File localRepoPath = new File(localPath + "/" + repositoryName);
        Git git = Git.open(localRepoPath);
        try {
            git.checkout()
                    .setName("main")
                    .setCreateBranch(true)
                    .call();
            System.out.println("브랜치 이름 변경 성공");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Branch name change failed");
        }

        StoredConfig config = git.getRepository().getConfig();
        System.out.println(git.getRepository());
        config.setString("remote", "origin", "url", githubRepoUrl);
        config.save();

        try {
            git.push()
                    .setRemote("origin")
                    .setRefSpecs(new RefSpec("refs/heads/main:refs/heads/main"))
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider(githubAccessToken, ""))
                    .call();
            System.out.println("Git 푸시 완료");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Git push failed: " + e.getMessage());
        } finally {
            git.close();
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
        Member member = getMemberbyJwtToken(jwtToken);
        String apiUrl = "https://api.github.com/users/" + member.getGithubName() + "/repos";
        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Authorization", "token " + encryptService.decrypt(member.getGithubAccessToken()));
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

    private Member getMemberbyJwtToken(String jwtToken) {
        String email = jwtTokenProvider.extractSubjectFromJwt(jwtToken);
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("해당하는 이메일이 없습니다."));
        return member;
    }

    public String autoCI(String jwtToken, String repositoryName, String localRepositoryPath, String branchName) {
        Member member = getMemberbyJwtToken(jwtToken);

        String githubUsername = member.getGithubName();
        String githubAccessToken = getGithubAccessToken(jwtToken);
        String dockerhubUsername = encryptService.decrypt(member.getDockerHubUsername());
        String dockerhubPassword = encryptService.decrypt(member.getDockerHubPassword());

        cloneGitRepository(repositoryName, member.getGithubName(), githubAccessToken);
        createDevelopBranch(localRepositoryPath, branchName);
        CIService.saveProject(member, repositoryName);
        CIService.addDockerfile(localRepositoryPath, branchName, githubUsername, githubAccessToken);
        CIService.commitAndPushGithubAction(localRepositoryPath, branchName, githubAccessToken, githubUsername, dockerhubUsername, dockerhubPassword, repositoryName);
        return "CI 성공";
    }

    public void cloneGitRepository(String repositoryName, String githubUsername, String githubAccessToken) {
        String repositoryUrl = "https://github.com/" + githubUsername + "/" + repositoryName + ".git";
        String localPath = "/app/" + repositoryName;
        System.out.println(repositoryUrl);
        try {
            Git.cloneRepository()
                    .setURI(repositoryUrl)
                    .setDirectory(new File(localPath))
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider(githubAccessToken, ""))
                    .call();
            System.out.println("Repository clone success");
        } catch (GitAPIException e) {
            System.out.println("Exception occured" + e);
        }
    }

//    public void cloneGitRepositoryAndEditDockerCompose(String repositoryName, String githubUsername, String githubAccessToken) {
//        String repositoryUrl = "https://github.com/" + githubUsername + "/" + repositoryName + ".git";
//        String localPath = "/app/" + repositoryName;
//        System.out.println(repositoryUrl);
//        try {
//            Git git = Git.cloneRepository()
//                    .setURI(repositoryUrl)
//                    .setDirectory(new File(localPath))
//                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider(githubAccessToken, ""))
//                    .call();
//            System.out.println("Repository clone success");
//
//            File composeFile = new File(localPath + "/docker-compose.yml");
//            FileInputStream fis = new FileInputStream(composeFile);
//
//            // Parse YAML
//            Yaml yaml = new Yaml();
//            Map<String, Object> yamlMap = yaml.load(fis);
//
//            // Modify image in the services section
//            if (yamlMap.containsKey("services")) {
//                Map<String, Object> services = (Map<String, Object>) yamlMap.get("services");
//                services.forEach((serviceName, serviceConfig) -> {
//                    if (serviceConfig instanceof Map) {
//                        Map<String, Object> serviceConfigMap = (Map<String, Object>) serviceConfig;
//                        if (serviceConfigMap.containsKey("image")) {
//                            String currentImage = (String) serviceConfigMap.get("image");
//                            if (currentImage.equals("gurwls0122/dockertest13")) {
//                                serviceConfigMap.put("image", NEW_IMAGE_PREFIX + "{username}_{repo_name}");
//                            }
//                        }
//                    }
//                });
//            }
//
//            DumperOptions options = new DumperOptions();
//            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
//
//            Yaml updatedYaml = new Yaml(new MyRepresenter(), options);
//            FileWriter writer = new FileWriter(composeFile);
//            updatedYaml.dump(yamlMap, writer);
//
//            git.add().addFilepattern(".").call();
//            git.commit().setMessage("Update Docker Compose file").call();
//            git.push().call();
//
//            git.close();
//        } catch (GitAPIException e) {
//            System.out.println("Exception occured" + e);
//        } catch (FileNotFoundException e) {
//            throw new RuntimeException(e);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }

//    private static class MyRepresenter extends Representer {
//        @Override
//        protected NodeTuple representJavaBeanProperty(Object javaBean, Property property, Object propertyValue, Tag customTag) {
//            NodeTuple defaultNodeTuple = super.representJavaBeanProperty(javaBean, property, propertyValue, customTag);
//            if (property.getName().equals("image")) {
//                return new NodeTuple(
//                        defaultNodeTuple.getKeyNode(),
//                        new ScalarNode(Tag.STR, NEW_IMAGE_PREFIX + "{username}_{repo_name}", defaultNodeTuple.getValueNode().getStartMark(), defaultNodeTuple.getValueNode().getEndMark(), true, true)
//                );
//            }
//            return defaultNodeTuple;
//        }
//    }

    public void createDevelopBranch(String localRepositoryPath, String branchName) {
        File repositoryDirectory = new File(localRepositoryPath);
        String startPoint = "main"; // 새 브랜치의 시작 지점
        try {
            Git git = Git.open(repositoryDirectory);
            Ref branchRef = git.branchCreate()
                    .setName(branchName)
                    .setStartPoint(startPoint)
                    .call();
            git.checkout()
                    .setName(branchName) // 브랜치 이름 지정
                    .call();
            System.out.println("새로운 브랜치가 생성되었습니다:");
            System.out.println("브랜치 이름: " + branchRef.getName());
            git.close();
        } catch (IOException | GitAPIException e) {
            e.printStackTrace();
        }
    }

    public Boolean autoCD(String jwtToken, String localRepositoryPath, String serviceName, String replicaCount) {
        Member member = getMemberbyJwtToken(jwtToken);
        String githubUsername = member.getGithubName();
        String githubAccessToken = getGithubAccessToken(jwtToken);
        String dockerhubUsername = encryptService.decrypt(member.getDockerHubUsername());
        Project project = projectRepository.findByMemberIdAndName(member.getId(), member.getGithubName()).orElseThrow(() -> new RuntimeException("아직 CI과정이 완료되지 않았습니다."));
        System.out.println("project = " + project);
        if (CIService.isCICompleted(member, serviceName)) {
            return CDService.commitAndPushDeployContents(localRepositoryPath, githubUsername, githubAccessToken, serviceName, replicaCount, dockerhubUsername);
        } else {
            return Boolean.FALSE;
        }
    }

    @Transactional
    public String actionCompleted(String userName) {
        Member member = memberRepository.findByGithubName(userName).orElseThrow(() -> new RuntimeException("해당하는 유저가 없습니다."));
        Project project = projectRepository.findByMemberIdAndName(member.getId(), member.getGithubName()).orElseThrow(() -> new RuntimeException("아직 CI과정이 완료되지 않았습니다."));
        project.updateCI();
        return "Action에서 정상적으로 요청되었습니다.";
    }

    public void addLabel(String repositoryName, String githubName) {
        String localPath = "/app/" + repositoryName;

    }
}
