package kluster.klusterweb.service;

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

@Service
@RequiredArgsConstructor
public class CIService {

    private final FileContentService fileContentService;

    public void commitAndPushGithubAction(String localRepositoryPath, String branchName, String githubAccessToken, String githubUsername, String dockerhubUsername, String dockerhubPassword, String repositoryName) {
        String actionContent = fileContentService.getActionContent(branchName, dockerhubUsername, dockerhubPassword, repositoryName);
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
