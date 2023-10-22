package kluster.klusterweb.dto.ArgoApi;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OperationDto {

    @JsonProperty("initiatedBy")
    private InitiatedByDto initiatedByDto;

    @JsonProperty("sync")
    private SyncDto syncDto;
}
