package org.example.controller;

import org.example.dto.PublicMessageDto;
import org.example.dto.SendMessageRequest;
import org.example.service.PublicChatService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/public-chat")
@RequiredArgsConstructor
public class PublicChatController {

    private final PublicChatService publicChatService;

    @PostMapping("/messages")
    public PublicMessageDto send(
            @RequestBody @Valid SendMessageRequest request) {

        return publicChatService.send(request.content());
    }
}
