package kluster.klusterweb.dto;

import lombok.Builder;
import lombok.Data;

@Data
public class MemberResponseDto {
    private String email;
    private String githubAccessToken;
    private String schoolAuthenticated;
    private String jwtAccessToken;
    private String jwtRefreshToken;

    @Builder
    public MemberResponseDto(String email, String githubAccessToken, String schoolAuthenticated, String jwtAccessToken, String jwtRefreshToken){
        this.email = email;
        this.githubAccessToken = githubAccessToken;
        this.schoolAuthenticated = schoolAuthenticated;
        this.jwtAccessToken = jwtAccessToken;
        this.jwtRefreshToken = jwtRefreshToken;
    }
}
