package com.CollabSphere.CollabSphere.Security;

import com.CollabSphere.CollabSphere.Entity.FileStorage;
import com.CollabSphere.CollabSphere.Entity.Team;
import com.CollabSphere.CollabSphere.Entity.User;
import com.CollabSphere.CollabSphere.Repository.FileStorageRepository;
import com.CollabSphere.CollabSphere.Repository.TeamMemberRepository;
import com.CollabSphere.CollabSphere.Repository.TeamRepository;
import com.CollabSphere.CollabSphere.Repository.UserRepository;

import jakarta.annotation.Nullable;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * JWTAuthenticationFilter with team- and file-level authorization for CollabSphere.
 *
 * - Authenticates requests using JWT (sets SecurityContext)
 * - If request targets /api/teams/{teamId}/... enforces membership/owner/admin
 * - If request targets /api/files/... enforces file permissions:
 *     * GET /api/files/{id}/download  -> uploader OR team member/owner OR admin
 *     * GET /api/files/{id}/stream    -> same
 *     * DELETE /api/files/{id}         -> uploader OR team member/owner OR admin
 *     * GET /api/files/team/{teamId}   -> only team members/owner/admin (listing)
 */
@Component
public class JWTAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JWTAuthenticationFilter.class);

    private final JWTUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final FileStorageRepository fileStorageRepository;

    // Patterns
    private static final Pattern TEAM_PATTERN = Pattern.compile("^/api/teams/(\\d+)(/.*)?$");
    private static final Pattern FILE_BY_ID_PATTERN = Pattern.compile("^/api/files/(\\d+)(/.*)?$");
    private static final Pattern FILE_TEAM_LIST_PATTERN = Pattern.compile("^/api/files/team/(\\d+)(/.*)?$");

    public JWTAuthenticationFilter(JWTUtil jwtUtil,
                                   UserDetailsService userDetailsService,
                                   UserRepository userRepository,
                                   TeamRepository teamRepository,
                                   TeamMemberRepository teamMemberRepository,
                                   FileStorageRepository fileStorageRepository) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.userRepository = userRepository;
        this.teamRepository = teamRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.fileStorageRepository = fileStorageRepository;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String header = request.getHeader("Authorization");
            String token = resolveToken(header);

            if (token != null && jwtUtil.validateToken(token) &&
                    SecurityContextHolder.getContext().getAuthentication() == null) {

                String username = jwtUtil.getUserNameFromToken(token);
                if (username != null) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(auth);

                    log.debug("Authenticated user: {} for request {}", username, request.getRequestURI());

                    // Team endpoint check
                    Long teamId = extractTeamId(request.getRequestURI());
                    if (teamId != null) {
                        if (!isAllowedForTeam(teamId, username, userDetails)) {
                            log.debug("Access denied (team) for user {} to team {}", username, teamId);
                            response.sendError(HttpServletResponse.SC_FORBIDDEN, "You are not a member/owner of this team");
                            return;
                        }
                    }

                    // File endpoint check
                    Long fileId = extractFileId(request.getRequestURI());
                    if (fileId != null) {
                        if (!isAllowedForFile(fileId, username, userDetails)) {
                            log.debug("Access denied (file) for user {} to file {}", username, fileId);
                            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Not authorized to access this file");
                            return;
                        }
                    }

                    // Team-file listing endpoint: /api/files/team/{teamId}
                    Long listTeamId = extractFileTeamListId(request.getRequestURI());
                    if (listTeamId != null) {
                        if (!isAllowedForTeam(listTeamId, username, userDetails)) {
                            log.debug("Access denied (file list) for user {} to team {} files", username, listTeamId);
                            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Not authorized to list files for this team");
                            return;
                        }
                    }
                }
            }
        } catch (Exception ex) {
            log.error("Error in JWTAuthenticationFilter: {}", ex.getMessage(), ex);
            // do not set authentication on error; downstream will handle
        }

        filterChain.doFilter(request, response);
    }

    // -------------------------
    // Helper: Team check
    // -------------------------
    private boolean isAllowedForTeam(Long teamId, String username, UserDetails userDetails) {
        // load user entity
        Optional<User> optUser = userRepository.findByEmail(username);
        if (optUser.isEmpty()) return false;
        User user = optUser.get();

        // load team
        Optional<Team> optTeam = teamRepository.findById(teamId);
        if (optTeam.isEmpty()) return false;
        Team team = optTeam.get();

        boolean isOwner = team.getOwner() != null && team.getOwner().getId().equals(user.getId());
        // try fast repo check if available
        boolean isMember = false;
        try {
            isMember = teamMemberRepository.existsByTeamIdAndUserId(teamId, user.getId());
        } catch (Exception ignored) {
            // fallback to entity collection if repo not available / configured
            isMember = team.getMembers() != null && team.getMembers().stream().anyMatch(u -> u.getId().equals(user.getId()));
        }

        boolean isAdmin = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_ADMIN"));

        return isOwner || isMember || isAdmin;
    }

    // -------------------------
    // Helper: File check
    // -------------------------
    private boolean isAllowedForFile(Long fileId, String username, UserDetails userDetails) {
        Optional<FileStorage> optFile = fileStorageRepository.findById(fileId);
        if (optFile.isEmpty()) return false;
        FileStorage f = optFile.get();
        if (f.isDeleted()) return false;

        // load user
        Optional<User> optUser = userRepository.findByEmail(username);
        if (optUser.isEmpty()) return false;
        User user = optUser.get();

        // uploader allowed
        if (f.getUploader() != null && f.getUploader().getId().equals(user.getId())) return true;

        // if file attached to team -> owner or member allowed
        if (f.getTeam() != null) {
            Long teamId = f.getTeam().getId();
            boolean isOwner = f.getTeam().getOwner() != null && f.getTeam().getOwner().getId().equals(user.getId());
            boolean isMember = false;
            try {
                isMember = teamMemberRepository.existsByTeamIdAndUserId(teamId, user.getId());
            } catch (Exception ignored) {
                isMember = f.getTeam().getMembers() != null && f.getTeam().getMembers().stream()
                        .anyMatch(u -> u.getId().equals(user.getId()));
            }
            if (isOwner || isMember) return true;
        }

        // global admin allowed
        boolean isAdmin = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_ADMIN"));

        return isAdmin;
    }

    // -------------------------
    // URI extractors
    // -------------------------
    @Nullable
    private Long extractTeamId(String uri) {
        if (uri == null) return null;
        Matcher m = TEAM_PATTERN.matcher(uri);
        if (m.find()) {
            try {
                return Long.parseLong(m.group(1));
            } catch (NumberFormatException ignored) {}
        }
        return null;
    }

    @Nullable
    private Long extractFileId(String uri) {
        if (uri == null) return null;
        Matcher m = FILE_BY_ID_PATTERN.matcher(uri);
        if (m.find()) {
            try {
                return Long.parseLong(m.group(1));
            } catch (NumberFormatException ignored) {}
        }
        return null;
    }

    @Nullable
    private Long extractFileTeamListId(String uri) {
        if (uri == null) return null;
        Matcher m = FILE_TEAM_LIST_PATTERN.matcher(uri);
        if (m.find()) {
            try {
                return Long.parseLong(m.group(1));
            } catch (NumberFormatException ignored) {}
        }
        return null;
    }

    // -------------------------
    // Token helpers
    // -------------------------
    @Nullable
    private String resolveToken(String header) {
        if (!StringUtils.hasText(header)) return null;
        String h = header.trim();
        if (h.toLowerCase().startsWith("bearer ")) {
            return h.substring(7).trim();
        }
        return h;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // Keep auth endpoints public, allow WS and docs, and allow preflight
        return path.startsWith("/api/auth") || path.startsWith("/ws") || path.startsWith("/swagger")
                || path.startsWith("/v3/api-docs") || path.startsWith("/swagger-ui") || "OPTIONS".equalsIgnoreCase(request.getMethod());
    }
}