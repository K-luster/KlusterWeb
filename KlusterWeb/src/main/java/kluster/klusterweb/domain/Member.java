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
    private String githubName;
    private String githubAccessToken;
    private String dockerHubUsername;
    private String dockerHubPassword;

    @Builder
    public Member(String email, String password, String githubName, String githubAccessToken,String dockerHubUsername, String dockerHubPassword){
        this.email = email;
        this.password = password;
        this.githubName = githubName;
        this.githubAccessToken = githubAccessToken;
        this.dockerHubUsername = dockerHubUsername;
        this.dockerHubPassword = dockerHubPassword;
    }
}
