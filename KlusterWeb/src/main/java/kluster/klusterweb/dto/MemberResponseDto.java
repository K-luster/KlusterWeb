package kluster.klusterweb.dto;

import lombok.Builder;
import lombok.Data;

@Data
public class MemberResponseDto {
    private String email;
    private String githubAccessToken;
    private String schoolAuthenticated;

    @Builder
    public MemberResponseDto(String email, String githubAccessToken, String schoolAuthenticated){
        this.email = email;
        this.githubAccessToken = githubAccessToken;
        this.schoolAuthenticated = schoolAuthenticated;
    }
}
