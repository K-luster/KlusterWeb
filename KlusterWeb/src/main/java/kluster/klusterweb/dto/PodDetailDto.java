package kluster.klusterweb.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
public class PodDetailDto {
    private String name;
    private String namespace;
    private List<ContainerDto> container;

    @Builder
    public PodDetailDto(String name, String namespace, List<ContainerDto> containerDto) {
        this.name = name;
        this.namespace = namespace;
        this.container = containerDto;
    }
}
