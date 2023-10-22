package kluster.klusterweb.dto.ArgoApi;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DestinationDto {

    private String name;
    private String namespace;
    private String server;
}
