package kluster.klusterweb.dto.ArgoApi;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ArgoApiResponseDto {
    private Metadata metadata;
    private Spec spec;
    private Status status;

    @Getter
    public static class Metadata {
        private String name;
        private String namespace;
        private String uid;
        private String resourceVersion;
        private int generation;
        private String creationTimestamp;
    }

    @Getter
    public static class Spec {
        private Source source;
        private Destination destination;
        private String project;
        private SyncPolicy syncPolicy;
    }

    @Getter
    public static class Source {
        @JsonProperty("repoURL")
        private String repoUrl;
        private String path;
        private String targetRevision;
    }

    @Getter
    public static class Destination {
        private String server;
        private String namespace;
    }

    @Getter
    public static class SyncPolicy {
        private Automated automated;
    }

    @Getter
    public static class Automated {
        private boolean prune;
    }

    @Getter
    public static class Status {
        private Sync sync;
        private Health health;
        private Summary summary;
    }

    @Getter
    public static class Sync {
        private String status;
        private ComparedTo comparedTo;
    }

    @Getter
    public static class ComparedTo {
        private Source source;
        private Destination destination;
    }

    @Getter
    @JsonSerialize
    public static class Health {
    }

    @Getter
    @JsonSerialize
    public static class Summary {
    }

}