package com.CollabSphere.CollabSphere.Configuration;

import com.CollabSphere.CollabSphere.Security.AuthChannelInterceptor;
import com.CollabSphere.CollabSphere.Security.JwtHandshakeInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

/**
 * WebSocket / STOMP configuration for CollabSphere.
 *
 * - Endpoint: /ws (SockJS fallback enabled)
 * - Application destination prefix: /app
 * - Simple in-memory broker destinations: /topic, /queue
 * - User destination prefix: /user
 * - Applies JwtHandshakeInterceptor to the handshake
 * - Applies AuthChannelInterceptor to inbound messages
 */
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtHandshakeInterceptor jwtHandshakeInterceptor;
    private final AuthChannelInterceptor authChannelInterceptor;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // simple broker for demonstration (replace with broker relay for scale)
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Client connects to /ws. Accept token either as header or ?token= query param.
        registry.addEndpoint("/ws")
                .addInterceptors(jwtHandshakeInterceptor)
                .setAllowedOriginPatterns("*") // tighten in production to your origins
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // run inbound messages (CONNECT, SUBSCRIBE, SEND) through the auth channel interceptor
        registration.interceptors(authChannelInterceptor);
    }

    // Optional: configure outbound channel interceptors if you need (not required here)
    // @Override
    // public void configureClientOutboundChannel(ChannelRegistration registration) { }

}
