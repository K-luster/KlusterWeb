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
public class DeployService {

    private final ContentService contentService;

    public void commitAndPushDeployContents(String localRepositoryPath, String githubUsername, String githubAccessToken, String serviceName, String replicaCount, String dockerhubUsername) {
        commitAndPushDeploymentYml(localRepositoryPath, githubUsername, githubAccessToken, serviceName, replicaCount, dockerhubUsername);
        commitAndPushServiceYml(localRepositoryPath, githubUsername, githubAccessToken, serviceName);
        commitAndPushHpaTestYml(localRepositoryPath, githubUsername, githubAccessToken, serviceName);
    }

    private void commitAndPushDeploymentYml(String localRepositoryPath, String githubUsername, String githubAccessToken, String serviceName, String replicaCount, String dockerhubUsername) {
        String deploymentYmlContent = contentService.getDeploymentYmlContent(serviceName, replicaCount, dockerhubUsername);
        String directoryPath = localRepositoryPath; // 디렉터리 경로 지정
        String filePath = directoryPath + "/deployment.yml"; // 파일 경로 지정
        File directory = new File(directoryPath);
        if (makeDir(directory)) return;
        makeFile("develop",localRepositoryPath, githubAccessToken, githubUsername, deploymentYmlContent, new File(filePath), "feat: deploymentYmlContent 추가");
    }

    private void commitAndPushServiceYml(String localRepositoryPath, String githubUsername, String githubAccessToken, String serviceName) {
        String serviceYmlContent = contentService.getServiceYmlContent(serviceName);
        String directoryPath = localRepositoryPath; // 디렉터리 경로 지정
        String filePath = directoryPath + "/service.yml"; // 파일 경로 지정
        File directory = new File(directoryPath);
        if (makeDir(directory)) return;
        makeFile("develop", localRepositoryPath, githubAccessToken, githubUsername, serviceYmlContent, new File(filePath), "feat: serviceYmlContent 추가");
    }

    private void commitAndPushHpaTestYml(String localRepositoryPath, String githubUsername, String githubAccessToken, String serviceName) {
        String hpaTestContent = contentService.getHpaTestContent(serviceName);
        String directoryPath = localRepositoryPath; // 디렉터리 경로 지정
        String filePath = directoryPath + "/hpa-test.yml"; // 파일 경로 지정
        File directory = new File(directoryPath);
        if (makeDir(directory)) return;
        makeFile("develop", localRepositoryPath, githubAccessToken, githubUsername, hpaTestContent, new File(filePath), "feat: hpaTestContent 추가");
    }

    private static boolean makeDir(File directory) {
        if (!directory.exists()) {
            if (directory.mkdirs()) {
                System.out.println("디렉터리가 생성되었습니다.");
            } else {
                System.err.println("디렉터리 생성에 실패했습니다.");
                return true;
            }
        }
        return false;
    }

    private void makeFile(String branchName, String localRepositoryPath, String githubAccessToken, String githubUsername, String actionContent, File file, String commitMessage) {
        // 파일이 존재하지 않으면 생성
        try {
            if (file.createNewFile()) {
                FileWriter writer = new FileWriter(file);
                writer.write(actionContent);
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
                System.out.println("파일이 생성되었습니다.");
            } else {
                System.err.println("파일 생성에 실패했습니다.");
            }
        } catch (IOException | GitAPIException e) {
            e.printStackTrace();
        }
    }
}
