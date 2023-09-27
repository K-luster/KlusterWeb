package kluster.klusterweb.dto.ArgoApiDto;

import kluster.klusterweb.service.MemberService;
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
