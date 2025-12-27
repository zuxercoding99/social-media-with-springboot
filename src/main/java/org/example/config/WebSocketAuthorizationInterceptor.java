package org.example.config;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;

import org.springframework.stereotype.Component;

@Component
public class WebSocketAuthorizationInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) {
            return message;
        }

        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {

            String destination = accessor.getDestination();
            Authentication auth = (Authentication) accessor.getUser();

            if (destination == null)
                return message;

            // privado
            if (destination.startsWith("/user/")) {
                if (auth == null || !auth.isAuthenticated()) {
                    throw new AccessDeniedException("Login requerido");
                }
            }

            // público autenticado
            if (destination.equals("/topic/public")) {
                if (auth == null || !auth.isAuthenticated()) {
                    throw new AccessDeniedException("Login requerido");
                }
            }

        }

        return message;
    }
}
