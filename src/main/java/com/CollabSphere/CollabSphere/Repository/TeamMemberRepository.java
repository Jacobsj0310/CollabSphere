package com.CollabSphere.CollabSphere.Repository;

import org.springframework.stereotype.Repository;

@Repository
public interface TeamMemberRepository {
    // quick membership test (one SQL)
    boolean existsByTeamIdAndUserId(Long teamId, Long userId);

}
