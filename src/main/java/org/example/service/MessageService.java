package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.dto.MessageDto;
import org.example.entity.Friend;
import org.example.entity.Message;
import org.example.entity.User;
import org.example.exception.customs.httpstatus.BadRequestException;
import org.example.repository.MessageRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {

        private final MessageRepository messageRepository;
        private final FriendService friendService;
        private final AuthService authService;
        private final SimpMessagingTemplate messagingTemplate;

        @Transactional(readOnly = true)
        public List<MessageDto> getMessages(Long friendId) {

                User me = authService.getCurrentUser();
                Friend friend = friendService.getAcceptedFriendForUser(friendId, me);

                return messageRepository.findByFriendOrderBySentAtAsc(friend)
                                .stream()
                                .map(this::toDto)
                                .toList();
        }

        @Transactional
        public MessageDto sendMessage(Long friendId, String content) {

                if (content == null || content.isBlank()) {
                        throw new BadRequestException("Mensaje vacío");
                }

                User sender = authService.getCurrentUser();
                Friend friend = friendService.getAcceptedFriendForUser(friendId, sender);

                User recipient = friend.getRequester().equals(sender)
                                ? friend.getReceiver()
                                : friend.getRequester();

                Message message = Message.builder()
                                .friend(friend)
                                .sender(sender)
                                .content(content)
                                .sentAt(Instant.now())
                                .build();

                message = messageRepository.save(message);

                MessageDto dto = toDto(message);

                messagingTemplate.convertAndSendToUser(
                                recipient.getUsername(),
                                "/queue/messages",
                                dto);

                messagingTemplate.convertAndSendToUser(
                                sender.getUsername(),
                                "/queue/messages",
                                dto);

                return dto;
        }

        private MessageDto toDto(Message m) {
                return new MessageDto(
                                m.getId(),
                                m.getFriend().getId(),
                                m.getSender().getId(),
                                m.getSender().getUsername(),
                                m.getSender().getDisplayName(),
                                "/api/v1/avatars/" + m.getSender().getAvatarKey(),
                                m.getContent(),
                                m.getSentAt());
        }
}
