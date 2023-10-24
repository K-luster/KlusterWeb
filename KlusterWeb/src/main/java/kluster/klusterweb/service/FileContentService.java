package kluster.klusterweb.service;

import lombok.NoArgsConstructor;
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
@NoArgsConstructor
public class FileContentService {

    private static final String SERVER_URL = "http://54.180.150.131/github/action-completed"

    public String getActionContent(String githubUsername, String branchName, String dockerhubUsername, String dockerhubPassword, String repositoryName) {
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
                "\n" +
                "    - name: CI 완료 알려주기\n" +
                "      run: |\n" +
                "        repositoryName=%s\n" +
                "        githubUsername=%s\n" +
                "        serverURL=%s\n" +
                "        curl -X POST $serverURL \\\n" +
                "          -H \"Content-Type: application/json\" \\\n" +
                "          -d '{\n" +
                "            \"repositoryName\": \"'\"$repositoryName\"'\",\n" +
                "            \"githubUsername\": \"'\"$githubUsername\"'\"\n" +
                "          }\n" +
                "\n", branchName, dockerhubUsername, dockerhubPassword, dockerhubUsername, repositoryName, dockerhubUsername, repositoryName, repositoryName, githubUsername, SERVER_URL);
        return actionContent;
    }

    public String getDeploymentYmlContent(String serviceName, String replicaCount, String dockerhubUsername) {
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
        return deploymentYmlContent;
    }

    public String getServiceYmlContent(String serviceName) {
        String serviceYmlContent = String.format("apiVersion: v1\n" +
                "kind: Service\n" +
                "metadata:\n" +
                "  name: %s\n" +
                "spec:\n" +
                "  type: LoadBalancer\n" +
                "  ports:\n" +
                "    - port: 80\n" +
                "      protocol: TCP\n" +
                "      targetPort: 8080\n" +
                "  selector:\n" +
                "    app: %s", serviceName, serviceName);
        return serviceYmlContent;
    }

    public String getHpaTestContent(String serviceName) {
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
                "    name: %s\n", serviceName, serviceName);
        return hpaTestContent;
    }

    public boolean makeDir(File directory) {
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

    public void makeFile(String branchName, String localRepositoryPath, String githubAccessToken, String githubUsername, String actionContent, File file, String commitMessage) {
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
