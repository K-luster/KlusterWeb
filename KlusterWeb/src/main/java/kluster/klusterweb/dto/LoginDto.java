package kluster.klusterweb.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class LoginDto {

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Request {
        private String email;
        private String password;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Response {
        private String email;
        private String githubUsername;
        private String generateType;
        private String accessToken;
    }
}
