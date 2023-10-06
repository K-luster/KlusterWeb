package kluster.klusterweb.dto.ArgoApiDto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SyncPolicyDto {

    @JsonProperty("automated")
    private AutomatedDto automatedDto;

    private List<Object> syncOptions;

}
