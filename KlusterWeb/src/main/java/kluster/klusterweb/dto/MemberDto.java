package kluster.klusterweb.dto;

import lombok.Builder;
import lombok.Data;

@Data
public class MemberDto {
    private String email;
    private String password;
    private String githubAccessToken;
    private Boolean schoolAuthenticated;

    @Builder
    public MemberDto(String email, String password, String githubAccessToken, Boolean schoolAuthenticated){
        this.email = email;
        this.password = password;
        this.githubAccessToken = githubAccessToken;
        this.schoolAuthenticated = schoolAuthenticated;
    }
}
