package kluster.klusterweb.service;

import org.springframework.stereotype.Service;

@Service
public class ContentService {

    public String getActionContent(String branchName, String dockerhubUsername, String dockerhubPassword, String repositoryName) {
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
}
