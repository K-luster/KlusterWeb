package kluster.klusterweb.dto.ArgoApi;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class Items {
    @JsonProperty("metadata")
    private ArgoApiResponseDto.Metadata metadata;

    @JsonProperty("spec")
    private ArgoApiResponseDto.Spec spec;
}
