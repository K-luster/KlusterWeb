package kluster.klusterweb.service;

import kluster.klusterweb.domain.Member;
import kluster.klusterweb.domain.Project;
import kluster.klusterweb.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CIService {

    private final FileContentService fileContentService;
    private final ProjectRepository projectRepository;

    public void saveProject(Member member, String repositoryName) {
        Optional<Project> project = projectRepository.findByMemberIdAndName(member.getId(), repositoryName);
        if (project.isEmpty()) {
            Project saveProject = Project.builder()
                    .name(repositoryName)
                    .isCI(Boolean.FALSE)
                    .isCD(Boolean.FALSE)
                    .member(member).build();
            projectRepository.save(saveProject);
        }
    }

    public Boolean isCICompleted(Member member, String repositoryName) {
        Project project = projectRepository.findByMemberIdAndName(member.getId(), repositoryName).orElseThrow(() -> new RuntimeException("해당하는 프로젝트가 존재하지 않습니다."));
        if (project.getIsCI().equals(Boolean.TRUE)) {
            return Boolean.TRUE;
        } else {
            return Boolean.FALSE;
        }
    }

    public void commitAndPushGithubAction(String localRepositoryPath, String branchName, String githubAccessToken, String githubUsername, String dockerhubUsername, String dockerhubPassword, String repositoryName) {
        String actionContent = fileContentService.getActionContent(githubUsername, branchName, dockerhubUsername, dockerhubPassword, repositoryName);
        String directoryPath = localRepositoryPath + "/.github/workflows"; // 디렉터리 경로 지정
        String filePath = directoryPath + "/myworkflow.yaml"; // 파일 경로 지정
        File directory = new File(directoryPath);
        if (fileContentService.makeDir(directory)) return;
        File file = new File(filePath);
        fileContentService.makeFile(branchName, localRepositoryPath, githubAccessToken, githubUsername, actionContent, file, "Add githubActionFile");
    }

    public void addDockerfile(String localRepositoryPath, String branchName, String githubUsername, String githubAccessToken) {
        String javaDockerfileContent = String.format("FROM openjdk:11\n" +
                "ARG JAR_FILE=*.jar\n" +
                "COPY ${JAR_FILE} app.jar\n" +
                "ENTRYPOINT [\"java\",\"-jar\",\"./app.jar\"]\n" +
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
                    .setName(branchName) // 푸시할 브랜치 이름을 지정
                    .call();
            git.add().addFilepattern(".").call();
            git.commit().setMessage(commitMessage).call();
            CredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider(githubUsername, githubAccessToken);
            PushCommand pushCommand = git.push().setCredentialsProvider(credentialsProvider);
            pushCommand.call();
        } catch (IOException | GitAPIException e) {
            e.printStackTrace();
        }
    }
}
