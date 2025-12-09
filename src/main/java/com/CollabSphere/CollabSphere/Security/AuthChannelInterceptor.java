package com.CollabSphere.CollabSphere.Security;

import com.CollabSphere.CollabSphere.Security.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Channel interceptor that sets an authenticated Principal on STOMP CONNECT
 * and blocks SEND/SUBSCRIBE when no Principal is present.
 */
@Component
@RequiredArgsConstructor
public class AuthChannelInterceptor implements ChannelInterceptor {

    private static final Logger log = LoggerFactory.getLogger(AuthChannelInterceptor.class);

    private final JWTUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    @Override
    @Nullable
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        //SimpMessageHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, SimpMessageHeaderAccessor.class);
        //SimpMessageHeaderAccessor accessor = SimpMessageHeaderAccessor.wrap(message);
        StompHeaderAccessor accessor1 = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor1 == null) return message;

       StompCommand command = accessor1.getCommand();
        if (command == null) return message;





        // Handle CONNECT: set Principal if token present
        if (StompCommand.CONNECT.equals(command)) {
            // 1) Try Authorization native header (client can pass it in CONNECT)
            String authHeader = accessor1.getFirstNativeHeader("Authorization");
            String token = null;
            if (StringUtils.hasText(authHeader) && authHeader.toLowerCase().startsWith("bearer ")) {
                token = authHeader.substring(7).trim();
            }

            // 2) Fallback to handshake attribute stored by JwtHandshakeInterceptor
            if (!StringUtils.hasText(token) && accessor1.getSessionAttributes() != null) {
                Object attr = accessor1.getSessionAttributes().get("jwt_token");
                if (attr != null) token = attr.toString();
            }

            if (!StringUtils.hasText(token)) {
                log.debug("STOMP CONNECT without token; connection will be unauthenticated");
                return message; // allow connection but no principal
            }

            if (!jwtUtil.validateToken(token)) {
                log.debug("STOMP CONNECT with invalid token - rejecting");
                return null; // drop CONNECT
            }

            String username = jwtUtil.getUserNameFromToken(token);
            UserDetails ud = userDetailsService.loadUserByUsername(username);

            // create Authentication principal and attach to accessor
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(ud, null, ud.getAuthorities());
            accessor1.setUser(auth);
            log.debug("STOMP CONNECT authenticated principal: {}", username);
            return message;
        }

        // For SEND and SUBSCRIBE, ensure the user is authenticated
        if (StompCommand.SEND.equals(command) || StompCommand.SUBSCRIBE.equals(command) || StompCommand.UNSUBSCRIBE.equals(command)) {
            if (accessor1.getUser() == null) {
                log.debug("Blocking STOMP {} because no authenticated principal", command);
                return null; // drop the message/frame
            }
        }

        return message;
    }
}