package org.example.service;

import java.time.Instant;

import org.example.dto.PublicMessageDto;
import org.example.entity.User;
import org.example.exception.customs.httpstatus.BadRequestException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PublicChatService {

    private final AuthService authService;
    private final SimpMessagingTemplate messagingTemplate;

    public PublicMessageDto send(String content) {

        if (content == null || content.isBlank()) {
            throw new BadRequestException("Mensaje vacío");
        }

        User sender = authService.getCurrentUser();

        PublicMessageDto dto = new PublicMessageDto(
                sender.getId(),
                sender.getUsername(),
                sender.getDisplayName(),
                "/api/v1/avatars/" + sender.getAvatarKey(),
                content.trim(),
                Instant.now());

        // 🔥 broadcast a TODOS
        messagingTemplate.convertAndSend("/topic/public", dto);

        return dto;
    }
}
