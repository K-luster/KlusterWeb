package kluster.klusterweb.dto.ArgoApi;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArgoApplicationResponseDto {
    private String name;
    private String repoURL;
}
