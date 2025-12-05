package com.CollabSphere.CollabSphere.Security;

import com.CollabSphere.CollabSphere.Entity.Team;
import com.CollabSphere.CollabSphere.Entity.User;
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

    private static final Pattern TEAM_PATTERN = Pattern.compile("^/api/teams/(\\d+)(/.*)?$");

    public JWTAuthenticationFilter(JWTUtil jwtUtil,
                                   UserDetailsService userDetailsService,
                                   UserRepository userRepository,
                                   TeamRepository teamRepository) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.userRepository = userRepository;
        this.teamRepository = teamRepository;
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
                            response.sendError(HttpServletResponse.SC_FORBIDDEN, "User not found");
                            return;
                        }
                        User user = optUser.get();

                        Optional<Team> optTeam = teamRepository.findById(teamId);
                        if (optTeam.isEmpty()) {
                            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Team not found");
                            return;
                        }
                        Team team = optTeam.get();

                        boolean isOwner = team.getOwner() != null && team.getOwner().getId().equals(user.getId());
                        boolean isMember = team.getMembers() != null && team.getMembers().stream()
                                .anyMatch(u -> u.getId().equals(user.getId()));
                        boolean isAdmin = userDetails.getAuthorities().stream()
                                .map(GrantedAuthority::getAuthority)
                                .anyMatch(a -> a.equals("ROLE_ADMIN"));

                        if (!(isOwner || isMember || isAdmin)) {
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
            // If something unexpected occurs, do not set authentication — let downstream handle (or fail)
        }

        filterChain.doFilter(request, response);
    }

    @Nullable
    private String resolveToken(String header) {
        if (!StringUtils.hasText(header)) return null;
        String h = header.trim();
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
                || path.startsWith("/v3/api-docs");
    }
}
