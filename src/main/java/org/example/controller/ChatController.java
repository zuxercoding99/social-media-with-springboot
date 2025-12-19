package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.dto.ChatPreviewDto;
import org.example.service.ChatService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/chats")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @GetMapping
    public List<ChatPreviewDto> chats() {
        return chatService.getChats();
    }
}
