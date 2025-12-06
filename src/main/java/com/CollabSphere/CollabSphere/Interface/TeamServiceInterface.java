package com.CollabSphere.CollabSphere.Interface;

import com.CollabSphere.CollabSphere.DTO.TeamDTO;
import jakarta.transaction.Transactional;

import java.util.List;

public interface TeamServiceInterface {

    TeamDTO.TeamResponse createTeam(TeamDTO.TeamRequest request, String ownerEmail);

    TeamDTO.TeamResponse getTeam(Long teamId);

    List<TeamDTO.TeamResponse> listTeamsForUser(String userEmail);

    TeamDTO.TeamResponse updateTeam(Long teamId, TeamDTO.TeamRequest request, String requesterEmail);

    void deleteTeam(Long teamId, String requesterEmail);

    TeamDTO.TeamResponse addMember(Long teamId, String memberEmail, String requesterEmail);

    TeamDTO.TeamResponse removeMember(Long teamId, String memberEmail, String requesterEmail);

}
