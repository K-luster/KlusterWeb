package kluster.klusterweb.dto.ArgoApiDto;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArgoApplicationResponseDto {
    private String name;
    private String repoURL;
}
