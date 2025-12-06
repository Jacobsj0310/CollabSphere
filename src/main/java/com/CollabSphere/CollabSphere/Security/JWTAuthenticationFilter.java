package com.CollabSphere.CollabSphere.Security;

import com.CollabSphere.CollabSphere.Entity.Team;
import com.CollabSphere.CollabSphere.Entity.User;
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

@Component
public class JWTAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JWTAuthenticationFilter.class);

    private final JWTUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository; // stored so we can use it if needed

    private static final Pattern TEAM_PATTERN = Pattern.compile("^/api/teams/(\\d+)(/.*)?$");

    public JWTAuthenticationFilter(JWTUtil jwtUtil,
                                   UserDetailsService userDetailsService,
                                   UserRepository userRepository,
                                   TeamRepository teamRepository,
                                   TeamMemberRepository teamMemberRepository) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.userRepository = userRepository;
        this.teamRepository = teamRepository;
        this.teamMemberRepository = teamMemberRepository; // assign to field
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

                    // If path targets a team resource, enforce team-level authorization
                    Long teamId = extractTeamId(request.getRequestURI());
                    if (teamId != null) {
                        // load user entity to check membership/ownership
                        Optional<User> optUser = userRepository.findByEmail(username);
                        if (optUser.isEmpty()) {
                            log.debug("User entity not found for email: {}", username);
                            response.sendError(HttpServletResponse.SC_FORBIDDEN, "User not found");
                            return;
                        }
                        User user = optUser.get();

                        Optional<Team> optTeam = teamRepository.findById(teamId);
                        if (optTeam.isEmpty()) {
                            log.debug("Team not found: {}", teamId);
                            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Team not found");
                            return;
                        }
                        Team team = optTeam.get();

                        boolean isOwner = team.getOwner() != null && team.getOwner().getId().equals(user.getId());

                        // Option A: check members collection (if team.members is eagerly available)
                        boolean isMemberFromEntity = team.getMembers() != null && team.getMembers().stream()
                                .anyMatch(u -> u.getId().equals(user.getId()));

                        // Option B: check membership via TeamMemberRepository (uncomment/use if you have such a table)
                        // boolean isMemberFromRepo = teamMemberRepository.isMember(teamId, user.getId());
                        // (Implement isMember in repo or use an appropriate query method)

                        boolean isAdmin = userDetails.getAuthorities().stream()
                                .map(GrantedAuthority::getAuthority)
                                .anyMatch(a -> a.equals("ROLE_ADMIN"));

                        if (!(isOwner || isMemberFromEntity || isAdmin /*|| isMemberFromRepo */)) {
                            log.debug("Access denied for user {} to team {}", username, teamId);
                            response.sendError(HttpServletResponse.SC_FORBIDDEN, "You are not a member/owner of this team");
                            return;
                        }
                        // else allowed — continue
                    }
                }
            }
        } catch (Exception ex) {
            log.error("Error in JWTAuthenticationFilter: {}", ex.getMessage(), ex);
            // Do not set authentication if errors — downstream handlers will manage response if necessary
        }

        filterChain.doFilter(request, response);
    }

    @Nullable
    private String resolveToken(String header) {
        if (!StringUtils.hasText(header)) return null;
        String h = header.trim();
        // Support both "Bearer token" and raw token
        if (h.toLowerCase().startsWith("bearer ")) {
            return h.substring(7).trim();
        }
        return h;
    }

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

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // Keep auth endpoints public, allow WS and docs
        return path.startsWith("/api/auth") || path.startsWith("/ws") || path.startsWith("/swagger")
                || path.startsWith("/v3/api-docs") || path.startsWith("/swagger-ui") || "OPTIONS".equalsIgnoreCase(request.getMethod());
    }
}
