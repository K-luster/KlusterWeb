package kluster.klusterweb.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class RepositoryDto {

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class RepositoryRequestDto {
        private String repositoryName;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class RepositoryResponseDto {
        private String repositoryName;
        private String userName;
        private String githubUrl;
        private String createdTime;
    }
}
