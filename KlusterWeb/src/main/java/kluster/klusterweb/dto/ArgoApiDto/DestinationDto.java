package kluster.klusterweb.dto.ArgoApiDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DestinationDto {

    private String name;
    private String namespace;
    private String server;
}
