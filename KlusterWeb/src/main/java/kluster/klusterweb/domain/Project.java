package kluster.klusterweb.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class Project extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(name = "is_ci")
    private Boolean isCI;

    @Column(name = "is_cd")
    private Boolean isCD;

    private int projectVersion;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @Transactional
    public void updateCI() {
        this.isCI = Boolean.TRUE;
    }

    @Transactional
    public int updateProjectVersion() {
        return this.projectVersion = this.projectVersion + 1;
    }

    @Builder
    public Project(String name, Boolean isCI, Boolean isCD, Member member) {
        this.name = name;
        this.isCI = isCI;
        this.isCD = isCD;
        this.projectVersion = 0;
        this.member = member;
    }
}
