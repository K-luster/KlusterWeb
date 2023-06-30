package kluster.klusterweb.domain;

import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Member {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
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
