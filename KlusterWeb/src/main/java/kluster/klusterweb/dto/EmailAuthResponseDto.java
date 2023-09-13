package kluster.klusterweb.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.relational.core.sql.In;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailAuthResponseDto {
    private Integer status;
    private boolean success;
    private String message;
    private String univName;
    private String certifiedEmail;
    private String certifiedDate;
}
