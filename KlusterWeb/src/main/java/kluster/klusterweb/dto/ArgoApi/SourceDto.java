package kluster.klusterweb.dto.ArgoApi;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SourceDto {

    private String path;
    private String repoURL;
    private String targetRevision;
}
