package org.example.config;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtHandshakeInterceptor jwtHandshakeInterceptor;
    private final CustomHandshakeHandler customHandshakeHandler;
    private final WebSocketAuthorizationInterceptor authorizationInterceptor;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {

        registry.addEndpoint("/ws")
                // JWT por query param ?token=
                .addInterceptors(jwtHandshakeInterceptor)

                // inyecta Authentication como Principal
                .setHandshakeHandler(customHandshakeHandler)

                // CORS
                .setAllowedOriginPatterns("*")

                // (opcional, si usás SockJS en frontend)
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {

        // solo push privado
        config.enableSimpleBroker("/queue", "/topic");

        // /user/queue/...
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {

        // bloquea SUBSCRIBE a /user/** sin login
        registration.interceptors(authorizationInterceptor);
    }
}
