package kluster.klusterweb.dto;

import lombok.Data;

@Data
public class CommitPushDto {

    private String repositoryName;
    private String localRepositoryPath;
    private String branchName;
}
