package kluster.klusterweb.dto;

import lombok.Data;

@Data
public class DeployRequestDto {

    private String localRepositoryPath;
    private String repositoryName;
    private String serviceName;
    private String replicaCount;
}
