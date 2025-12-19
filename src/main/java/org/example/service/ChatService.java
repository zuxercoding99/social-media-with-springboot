package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.dto.ChatPreviewDto;
import org.example.dto.LastMessageDto;
import org.example.entity.Friend;
import org.example.entity.Message;
import org.example.entity.User;
import org.example.repository.MessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final FriendService friendService;
    private final MessageRepository messageRepository;
    private final AuthService authService;

    @Transactional(readOnly = true)
    public List<ChatPreviewDto> getChats() {

        User me = authService.getCurrentUser();
        List<Friend> friends = friendService.getAllAcceptedFriends(me);

        return friends.stream().map(friend -> {

            User other = friend.getRequester().equals(me)
                    ? friend.getReceiver()
                    : friend.getRequester();

            Message last = messageRepository
                    .findTopByFriendOrderBySentAtDesc(friend);

            LastMessageDto lastDto = last == null ? null
                    : new LastMessageDto(
                            last.getSender().getId(),
                            last.getContent(),
                            last.getSentAt());

            return new ChatPreviewDto(
                    friend.getId(),
                    other.getId(),
                    other.getUsername(),
                    other.getDisplayName(),
                    "/api/v1/avatars/" + other.getAvatarKey(),
                    lastDto,
                    0);
        }).toList();
    }
}
