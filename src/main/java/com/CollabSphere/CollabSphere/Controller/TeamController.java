package com.CollabSphere.CollabSphere.Controller;

import com.CollabSphere.CollabSphere.DTO.TeamDTO;
import com.CollabSphere.CollabSphere.DTO.TeamDTO.TeamRequest;
import com.CollabSphere.CollabSphere.DTO.TeamDTO.TeamResponse;
import com.CollabSphere.CollabSphere.Service.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    @PostMapping
    public ResponseEntity<TeamResponse> createTeam(@Valid @RequestBody TeamRequest request,
                                                   Authentication authentication) {
        String ownerEmail = getEmailFromAuth(authentication);
        TeamResponse created = teamService.createTeam(request, ownerEmail);
        return ResponseEntity.created(URI.create("/api/teams/" + created.getId())).body(created);
    }

    /**
     * Get team by id (any authenticated user who can view).
     */
    @GetMapping("/{id}")
    public ResponseEntity<TeamResponse> getTeam(@PathVariable("id") Long id) {
        TeamResponse resp = teamService.getTeam(id);
        return ResponseEntity.ok(resp);
    }

    /**
     * List teams for the authenticated user.
     */
    @GetMapping
    public ResponseEntity<List<TeamResponse>> listMyTeams(Authentication authentication) {
        String email = getEmailFromAuth(authentication);
        List<TeamResponse> teams = teamService.listTeamsForUser(email);
        return ResponseEntity.ok(teams);
    }

    /**
     * Update a team (owner or admin).
     */
    @PutMapping("/{id}")
    public ResponseEntity<TeamResponse> updateTeam(@PathVariable("id") Long id,
                                                   @Valid @RequestBody TeamRequest request,
                                                   Authentication authentication) {
        String requesterEmail = getEmailFromAuth(authentication);
        TeamResponse updated = teamService.updateTeam(id, request, requesterEmail);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTeam(@PathVariable("id") Long id,
                                           Authentication authentication) {
        String requesterEmail = getEmailFromAuth(authentication);
        teamService.deleteTeam(id, requesterEmail);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/members")
    public ResponseEntity<TeamResponse> addMember(@PathVariable("id") Long id,
                                                  @RequestParam("email") String memberEmail,
                                                  Authentication authentication) {
        String requesterEmail = getEmailFromAuth(authentication);
        TeamResponse resp = teamService.addMember(id, memberEmail, requesterEmail);
        return ResponseEntity.ok(resp);
    }

    @DeleteMapping("/{id}/members")
    public ResponseEntity<TeamResponse> removeMember(@PathVariable("id") Long id,
                                                     @RequestParam("email") String memberEmail,
                                                     Authentication authentication) {
        String requesterEmail = getEmailFromAuth(authentication);
        TeamResponse resp = teamService.removeMember(id, memberEmail, requesterEmail);
        return ResponseEntity.ok(resp);
    }

    private String getEmailFromAuth(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user found");
        }
        // principal may be a UserDetails or the username string depending on config
        Object principal = authentication.getPrincipal();
        if (principal instanceof org.springframework.security.core.userdetails.User) {
            return ((org.springframework.security.core.userdetails.User) principal).getUsername();
        }
        return principal.toString();
    }
}