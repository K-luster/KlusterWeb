package kluster.klusterweb.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
public class EmailAuthDto {
    private String email;
    private String univName;
    private Integer code;
}
