package kluster.klusterweb.repository;

import kluster.klusterweb.domain.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    Optional<Project> findByMemberIdAndName(Long memberId, String name);
}
