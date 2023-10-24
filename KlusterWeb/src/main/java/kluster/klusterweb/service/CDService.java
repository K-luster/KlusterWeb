package kluster.klusterweb.service;


import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
@RequiredArgsConstructor
public class CDService {

    private final FileContentService fileContentService;

    public Boolean commitAndPushDeployContents(String localRepositoryPath, String githubUsername, String githubAccessToken, String serviceName, String replicaCount, String dockerhubUsername) {
        commitAndPushDeploymentYml(localRepositoryPath, githubUsername, githubAccessToken, serviceName, replicaCount, dockerhubUsername);
        commitAndPushServiceYml(localRepositoryPath, githubUsername, githubAccessToken, serviceName);
        commitAndPushHpaTestYml(localRepositoryPath, githubUsername, githubAccessToken, serviceName);
        return Boolean.TRUE;
    }

    private void commitAndPushDeploymentYml(String localRepositoryPath, String githubUsername, String githubAccessToken, String serviceName, String replicaCount, String dockerhubUsername) {
        String deploymentYmlContent = fileContentService.getDeploymentYmlContent(serviceName, replicaCount, dockerhubUsername);
        String directoryPath = localRepositoryPath; // 디렉터리 경로 지정
        String filePath = directoryPath + "/deployment.yml"; // 파일 경로 지정
        File directory = new File(directoryPath);
        if (fileContentService.makeDir(directory)) return;
        fileContentService.makeFile("develop",localRepositoryPath, githubAccessToken, githubUsername, deploymentYmlContent, new File(filePath), "feat: deploymentYmlContent 추가");
    }

    private void commitAndPushServiceYml(String localRepositoryPath, String githubUsername, String githubAccessToken, String serviceName) {
        String serviceYmlContent = fileContentService.getServiceYmlContent(serviceName);
        String directoryPath = localRepositoryPath; // 디렉터리 경로 지정
        String filePath = directoryPath + "/service.yml"; // 파일 경로 지정
        File directory = new File(directoryPath);
        if (fileContentService.makeDir(directory)) return;
        fileContentService.makeFile("develop", localRepositoryPath, githubAccessToken, githubUsername, serviceYmlContent, new File(filePath), "feat: serviceYmlContent 추가");
    }

    private void commitAndPushHpaTestYml(String localRepositoryPath, String githubUsername, String githubAccessToken, String serviceName) {
        String hpaTestContent = fileContentService.getHpaTestContent(serviceName);
        String directoryPath = localRepositoryPath; // 디렉터리 경로 지정
        String filePath = directoryPath + "/hpa-test.yml"; // 파일 경로 지정
        File directory = new File(directoryPath);
        if (fileContentService.makeDir(directory)) return;
        fileContentService.makeFile("develop", localRepositoryPath, githubAccessToken, githubUsername, hpaTestContent, new File(filePath), "feat: hpaTestContent 추가");
    }
}
