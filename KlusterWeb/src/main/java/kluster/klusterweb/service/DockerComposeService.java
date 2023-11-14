package kluster.klusterweb.service;

import kluster.klusterweb.config.jwt.JwtTokenProvider;
import kluster.klusterweb.domain.Member;
import kluster.klusterweb.repository.MemberRepository;
import kluster.klusterweb.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DockerComposeService {

    private static final String GITHUB_REPO_URL = "https://api.github.com/user/repos";
    private final String GITHUB_API_URL = "https://api.github.com/user";
    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;
    private final FileContentService fileContentService;
    private final CIService ciService;
    private final GithubService githubService;
    private final ProjectRepository projectRepository;
    private final EncryptService encryptService;

    private Member getMemberbyJwtToken(String jwtToken) {
        String email = jwtTokenProvider.extractSubjectFromJwt(jwtToken);
        return memberRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("해당하는 이메일이 없습니다."));
    }

    public String getGithubAccessToken(String jwtToken) {
        String email = jwtTokenProvider.extractSubjectFromJwt(jwtToken);
        Optional<Member> member = memberRepository.findByEmail(email);
        if (member.isPresent()) {
            Member findMember = member.get();
            return encryptService.decrypt(findMember.getGithubAccessToken());
        }
        throw new RuntimeException("존재하지 않는 이메일입니다.");
    }

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

    public String dockerComposeCI(String jwtToken, String repositoryName, String localRepositoryPath, String branchName) {
        Member member = getMemberbyJwtToken(jwtToken);
        String githubUsername = member.getGithubName();
        String githubAccessToken = getGithubAccessToken(jwtToken);
        String dockerhubUsername = encryptService.decrypt(member.getDockerHubUsername());
        String dockerhubPassword = encryptService.decrypt(member.getDockerHubPassword());
        githubService.cloneGitRepository(repositoryName, member.getGithubName(), githubAccessToken);
        // 여기서 docker-compose 에 label 달기
        // modifyDockerCompose(localRepositoryPath);
        githubService.addLabel(repositoryName, member.getGithubName());
        composeBuilder(githubUsername, repositoryName);
        createDevelopBranch(localRepositoryPath, branchName);
        changeKompose(localRepositoryPath, branchName, githubAccessToken, githubUsername, dockerhubUsername, dockerhubPassword, repositoryName);
        ciService.saveProject(member, repositoryName);
        return "Action 완료";
    }

    private void modifyDockerCompose(String localRepositoryPath) {
        Path dockerComposePath = Paths.get(localRepositoryPath, "docker-compose.yml");
        if (dockerComposePath == null) {
            dockerComposePath = Paths.get(localRepositoryPath, "docker-compose.yaml");
        }
        try {
            String dockerComposeContent = new String(Files.readAllBytes(dockerComposePath));
            String modifiedContent = modifyContent(dockerComposeContent);
            Files.write(dockerComposePath, modifiedContent.getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String modifyContent(String originalContent) {
        // Modify the content here as needed
        // For example, add or replace some text
        return originalContent.replace("oldValue", "newValue");
    }

    private void composeBuilder(String githubUsername, String repositoryName) {
        try {
            String namespace = String.format("--namespace=%s", githubUsername);
            String filePath = "/app/" + repositoryName;
            String[] commandArgs = {"kompose", "-f", "docker-compose.yml", namespace, "--controller", "statefulset", "convert"};

            ProcessBuilder processBuilder = new ProcessBuilder(commandArgs);
            processBuilder.directory(new File(filePath));
            processBuilder.redirectErrorStream(true);

            System.out.println("processBuilder = " + processBuilder);

            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            int exitCode = process.waitFor();

            if (exitCode != 0) {
                System.out.println("Retrying with docker-compose.yaml");
                commandArgs[2] = "docker-compose.yaml"; // Change to "docker-compose.yml"
                processBuilder = new ProcessBuilder(commandArgs);
                processBuilder.directory(new File(filePath));
                processBuilder.redirectErrorStream(true);

                System.out.println("processBuilder = " + processBuilder);

                process = processBuilder.start();
                reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }

                exitCode = process.waitFor();

                if (exitCode != 0) {
                    System.out.println("Error occurred while running kompose.");
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void changeKompose(String localRepositoryPath, String branchName, String githubAccessToken, String githubUsername, String dockerhubUsername, String dockerhubPassword, String repositoryName) {
        String actionContent;
        if (isDockerfileExists(localRepositoryPath)) { // Dockerfile 있는 경우
            actionContent = fileContentService.getDockerBuildAndComposeCIContent(dockerhubUsername, dockerhubPassword, repositoryName, githubUsername);
        } else {
            actionContent = fileContentService.getDockerComposeCIContent(repositoryName, githubUsername);
        }
        String directoryPath = localRepositoryPath + "/.github/workflows"; // 디렉터리 경로 지정
        String filePath = directoryPath + "/myworkflow.yaml"; // 파일 경로 지정
        File directory = new File(directoryPath);
        if (fileContentService.makeDir(directory)) return;
        File file = new File(filePath);
        fileContentService.makeFile(branchName, localRepositoryPath, githubAccessToken, githubUsername, actionContent, file, "Add githubActionFile");
    }

    public boolean isDockerfileExists(String localRepositoryPath) {
        String dockerfilePath = "Dockerfile"; // Dockerfile의 경로
        try (Git git = Git.open(Path.of(localRepositoryPath).toFile())) {
            Iterable<RevCommit> commits = git.log().all().call();
            for (RevCommit commit : commits) {
                try (RevWalk walk = new RevWalk(git.getRepository())) {
                    RevTree tree = walk.parseTree(commit.getTree().getId());
                    try (TreeWalk treeWalk = new TreeWalk(git.getRepository())) {
                        treeWalk.addTree(tree);
                        treeWalk.setRecursive(true);
                        treeWalk.setFilter(PathFilter.create(dockerfilePath));

                        if (treeWalk.next()) {
                            return true; // Dockerfile이 최소한 한 번 이상의 커밋에서 발견됨
                        }
                    }
                }
            }
        } catch (IOException | GitAPIException e) {
            e.printStackTrace();
        }
        return false;
    }

}
