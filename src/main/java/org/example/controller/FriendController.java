package org.example.controller;

import org.example.service.FriendService;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/friends")
public class FriendController {

    private final FriendService friendService;

    public FriendController(FriendService friendService) {
        this.friendService = friendService;

    }

    @PostMapping("/send/{receiverId}")
    public void sendRequest(@PathVariable UUID receiverId) {
        friendService.sendRequest(receiverId);
    }

    @PostMapping("/accept/{requesterId}")
    public void acceptRequest(@PathVariable UUID requesterId) {
        friendService.acceptRequest(requesterId);
    }

    @PostMapping("/reject/{requesterId}")
    public void rejectRequest(@PathVariable UUID requesterId) {
        friendService.rejectRequest(requesterId);
    }

    @DeleteMapping("/cancel/{receiverId}")
    public void cancelRequest(@PathVariable UUID receiverId) {
        friendService.cancelRequest(receiverId);
    }

    @DeleteMapping("/remove/{friendId}")
    public void removeFriend(@PathVariable UUID friendId) {
        friendService.removeFriend(friendId);
    }

    @PostMapping("/block/{blockedId}")
    public void blockUser(@PathVariable UUID blockedId) {
        friendService.blockUser(blockedId);
    }
}
