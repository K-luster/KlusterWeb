package kluster.klusterweb.dto.Member;

import lombok.Builder;
import lombok.Data;


public class SchoolDto {
    @Data
    @Builder
    public static class ResponseSuccess {
        private String univName;
        private String certified_email;
        private String certified_date;
    }

    @Data
    public static class codeCheck {
        private String email;
        private String code;
    }

}
