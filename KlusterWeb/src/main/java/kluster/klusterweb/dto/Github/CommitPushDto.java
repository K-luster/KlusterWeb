package kluster.klusterweb.dto.Github;

import lombok.Data;

@Data
public class CommitPushDto {

    private String repositoryName;
    private String localRepositoryPath;
    private String branchName;
}
