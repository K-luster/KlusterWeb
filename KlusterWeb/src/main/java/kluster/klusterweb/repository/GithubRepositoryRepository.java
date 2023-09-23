package kluster.klusterweb.repository;

import kluster.klusterweb.domain.GithubRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GithubRepositoryRepository extends JpaRepository<GithubRepository, Long> {
}
