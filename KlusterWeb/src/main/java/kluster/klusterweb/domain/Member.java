package kluster.klusterweb.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.persistence.*;

@Entity
@RequiredArgsConstructor
@Getter
public class Member {

    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column
    private String email;

    @Column
    private String password;

    @Column
    private String githubAccessToken;

    @Column
    private String schoolAuthenticated;

    @Builder
    public Member(String email, String password, String githubAccessToken, String schoolAuthenticated){
        this.email = email;
        this.password = password;
        this.githubAccessToken = githubAccessToken;
        this.schoolAuthenticated = schoolAuthenticated;
    }
}
