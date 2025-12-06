package com.CollabSphere.CollabSphere.Repository;

import com.CollabSphere.CollabSphere.Entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeamRepository extends JpaRepository<Team,Long> {
    List<Team> findByOwnerId(Long ownerId);

    // find teams where given user is a member (members is a Set<User>)
    List<Team> findByMembers_Id(Long userId);

    // membership check — single-db query, useful in security/filters
    boolean existsByIdAndMembers_Id(Long teamId, Long memberId);
}
