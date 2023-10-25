package kluster.klusterweb.repository;

import kluster.klusterweb.domain.Project;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    Project findByMemberIdAndName(Long memberId, String name);
}
