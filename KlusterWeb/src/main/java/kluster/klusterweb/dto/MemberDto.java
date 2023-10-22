package kluster.klusterweb.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MemberDto {
    private String email;
    private String password;
    private String githubName;
    private String githubAccessToken;
    private String dockerHubUsername;
    private String dockerHubPassword;
    private Boolean schoolAuthenticated;

    @Builder
    public MemberDto(String email, String password, String githubName, String githubAccessToken, String dockerHubUsername, String dockerHubPassword, Boolean schoolAuthenticated) {
        this.email = email;
        this.password = password;
        this.githubName = githubName;
        this.githubAccessToken = githubAccessToken;
        this.dockerHubUsername = dockerHubUsername;
        this.dockerHubPassword = dockerHubPassword;
        this.schoolAuthenticated = schoolAuthenticated;
    }
}
