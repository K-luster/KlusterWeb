package kluster.klusterweb.dto.ArgoApi;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ArgoApiRequestDto {

    @JsonProperty("metadata")
    private MetaDataDto metaDataDto;

//    @JsonProperty("operation")
//    private OperationDto operationDto;

    @JsonProperty("spec")
    private SpecDto specDto;

//    @JsonProperty("status")
//    private StatusDto statusDto;
}
