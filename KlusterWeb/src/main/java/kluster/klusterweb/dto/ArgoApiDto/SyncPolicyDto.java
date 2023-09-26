package kluster.klusterweb.dto.ArgoApiDto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SyncPolicyDto {

    @JsonProperty("automated")
    private AutomatedDto automatedDto;

    private List<Object> syncOptions;

}
