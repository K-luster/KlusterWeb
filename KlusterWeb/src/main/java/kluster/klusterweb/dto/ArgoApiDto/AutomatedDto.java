package kluster.klusterweb.dto.ArgoApiDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AutomatedDto {

    private Boolean prune;
    private Boolean selfHeal;
}
