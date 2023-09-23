package kluster.klusterweb.dto;

import lombok.Builder;
import lombok.Data;

@Data
public class ContainerDto {
    private String name;
    private String cpu;
    private String memory;

    @Builder
    public ContainerDto(String name, String cpu, String memory){
        this.name = name;
        this.cpu = cpu;
        this.memory = memory;
    }
}
