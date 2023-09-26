package kluster.klusterweb.dto;

import kluster.klusterweb.dto.ArgoApiDto.ArgoApiRequestDto;
import lombok.Data;

@Data
public class DeployRequestDto {

    private String localRepositoryPath;
    private String repositoryName;
    private String serviceName;
    private String replicaCount;

    private ArgoApiRequestDto argoApiRequestDto;
}
