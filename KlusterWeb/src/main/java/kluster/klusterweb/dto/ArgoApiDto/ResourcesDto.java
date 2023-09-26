package kluster.klusterweb.dto.ArgoApiDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResourcesDto {

    private String group;
    private String kind;
    private String name;
    private String namespace;
}
