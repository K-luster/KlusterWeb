package kluster.klusterweb.repository;

import kluster.klusterweb.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface MemberRepository extends JpaRepository<Member,Long> {
}
