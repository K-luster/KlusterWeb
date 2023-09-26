package kluster.klusterweb.dto.ArgoApiDto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SyncDto {

    @JsonProperty("resources")
    private ResourcesDto resourcesDto;

    @JsonProperty("source")
    private SourceDto sourceDto;
//
//    @JsonProperty("syncStrategy")
//    private SyncStrategyDto syncStrategyDto;
}
