package kluster.klusterweb.domain;

import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Member {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String email;
    private String password;
    private String githubAccessToken;
    private Boolean schoolAuthenticated;

    @Builder
    public Member(String email, String password, String githubAccessToken, Boolean schoolAuthenticated){
        this.email = email;
        this.password = password;
        this.githubAccessToken = githubAccessToken;
        this.schoolAuthenticated = schoolAuthenticated;
    }

    public void updateSchoolAuthenticate() {
        this.schoolAuthenticated = Boolean.TRUE;
    }
}
