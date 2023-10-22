package kluster.klusterweb.dto.ArgoApi;

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
public class SpecDto {
    @JsonProperty("destination")
    private DestinationDto destinationDto;

    @JsonProperty("source")
    private SourceDto sourceDto;

    @JsonProperty("sources")
    private List<Object> sourceDtos;

    private String project;

    @JsonProperty("syncPolicy")
    private SyncPolicyDto syncPolicyDto;
}
