package kluster.klusterweb.service;

import kluster.klusterweb.config.jwt.JwtTokenProvider;
import kluster.klusterweb.domain.Member;
import kluster.klusterweb.repository.MemberRepository;
import kluster.klusterweb.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
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

    private Member getMemberbyJwtToken(String jwtToken) {
        String email = jwtTokenProvider.extractSubjectFromJwt(jwtToken);
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("해당하는 이메일이 없습니다."));
        return member;
    }

    public String getGithubAccessToken(String jwtToken) {
        String email = jwtTokenProvider.extractSubjectFromJwt(jwtToken);
        Optional<Member> member = memberRepository.findByEmail(email);
        if (member.isPresent()) {
            Member findMember = member.get();
            return findMember.getGithubAccessToken();
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

    public String dockerComposeCI(String jwtToken, String repositoryName, String localRepositoryPath, String branchName) throws GitAPIException {
        Member member = getMemberbyJwtToken(jwtToken);
        String githubUsername = member.getGithubName();
        String githubAccessToken = getGithubAccessToken(jwtToken);
        String dockerhubUsername = member.getDockerHubUsername();
        String dockerhubPassword = member.getDockerHubPassword();
        githubService.cloneGitRepository(repositoryName, member.getGithubName(), githubAccessToken);
        composeBuilder(githubUsername, repositoryName);
        createDevelopBranch(localRepositoryPath, branchName);
        changeKompose(localRepositoryPath, branchName, githubAccessToken, githubUsername, dockerhubUsername, dockerhubPassword, repositoryName);
        ciService.saveProject(member, repositoryName);
        return "Action 완료";
    }

    private void composeBuilder(String githubUsername, String repositoryName) {
        try {
            // Define the kompose command
            String namespace = String.format("--namespace=%s", githubUsername);
            String[] commandArgs = {"kompose", "-f", "docker-compose.yaml", namespace, "convert"};
            System.out.println("DockerComposeService.composeBuilder1");
            // Create a ProcessBuilder
            ProcessBuilder processBuilder = new ProcessBuilder(commandArgs);
            System.out.println("DockerComposeService.composeBuilder2");
            // Set the working directory (where your Docker Compose file is located)
            processBuilder.directory(new File("/app/" + repositoryName));
            System.out.println("DockerComposeService.composeBuilder3");
            // Redirect the standard error stream
            processBuilder.redirectErrorStream(true);
            System.out.println("DockerComposeService.composeBuilder4");
            System.out.println("processBuilder = " + processBuilder);
            // Start the process
            Process process = processBuilder.start();
            System.out.println("DockerComposeService.composeBuilder5");
            // Read and print the output
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            // Wait for the process to complete
            int exitCode = process.waitFor();
            System.out.println("Process exited with code " + exitCode);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void changeKompose(String localRepositoryPath, String branchName, String githubAccessToken, String githubUsername, String dockerhubUsername, String dockerhubPassword, String repositoryName) {
        String actionContent = fileContentService.getDockerComposeCIContent(repositoryName, githubUsername);
        String directoryPath = localRepositoryPath + "/.github/workflows"; // 디렉터리 경로 지정
        String filePath = directoryPath + "/myworkflow.yaml"; // 파일 경로 지정
        File directory = new File(directoryPath);
        if (fileContentService.makeDir(directory)) return;
        File file = new File(filePath);
        fileContentService.makeFile(branchName, localRepositoryPath, githubAccessToken, githubUsername, actionContent, file, "Add githubActionFile");
    }


}
