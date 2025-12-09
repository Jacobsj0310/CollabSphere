package com.CollabSphere.CollabSphere.Security;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;

import java.util.Map;

/**
 * Handshake interceptor: checks JWT on the initial WS handshake.
 * Stores "jwt_token" and "ws_user" in session attributes if valid.
 */
@Component
@RequiredArgsConstructor
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private static final Logger log = LoggerFactory.getLogger(JwtHandshakeInterceptor.class);

    private final JWTUtil jwtUtil; // your bean

    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) throws Exception {

        // Try to extract from servlet request (works when using SockJS)
        if (request instanceof ServletServerHttpRequest servletReq) {
            HttpServletRequest http = servletReq.getServletRequest();

            // 1) Authorization header "Bearer <token>"
            String auth = http.getHeader("Authorization");
            String token = extractTokenFromHeader(auth);

            // 2) Fallback to query param ?token=...
            if (!StringUtils.hasText(token)) {
                String qp = http.getParameter("token");
                if (StringUtils.hasText(qp)) token = qp.trim();
            }

            if (!StringUtils.hasText(token)) {
                log.debug("WebSocket handshake without token (allowed, but connection will be unauthenticated)");
                return true; // allow handshake; channel interceptor can block later
            }

            if (!jwtUtil.validateToken(token)) {
                log.debug("WebSocket handshake token invalid");
                // reject handshake
                if (response != null) response.getHeaders().set("X-WS-Auth-Error", "invalid_token");
                return false;
            }

            String username = jwtUtil.getUserNameFromToken(token);
            attributes.put("jwt_token", token);
            attributes.put("ws_user", username);
            log.debug("WebSocket handshake authenticated user: {}", username);
        }

        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               WebSocketHandler wsHandler,
                               @Nullable Exception exception) {
        // no-op
    }

    private String extractTokenFromHeader(String header) {
        if (!StringUtils.hasText(header)) return null;
        String h = header.trim();
        if (h.toLowerCase().startsWith("bearer ")) return h.substring(7).trim();
        return h;
    }
}