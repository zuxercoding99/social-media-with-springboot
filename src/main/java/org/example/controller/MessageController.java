package org.example.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.dto.MessageDto;
import org.example.dto.SendMessageRequest;
import org.example.service.MessageService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/chats/{chatId}/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @GetMapping
    public List<MessageDto> getMessages(@PathVariable Long chatId) {
        return messageService.getMessages(chatId);
    }

    @PostMapping
    public MessageDto sendMessage(
            @PathVariable Long chatId,
            @RequestBody @Valid SendMessageRequest request) {
        return messageService.sendMessage(chatId, request.content());
    }
}
