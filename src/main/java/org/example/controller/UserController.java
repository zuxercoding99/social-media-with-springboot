package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.dto.FriendDto;
import org.example.dto.FriendRequestDto;
import org.example.dto.UserProfileDto;
import org.example.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // Perfil público
    @GetMapping("/{username}")
    public UserProfileDto getUserProfile(@PathVariable String username) {
        return userService.getUserProfile(username);
    }

    // Amigos
    @GetMapping("/{username}/friends")
    public List<FriendDto> getFriends(@PathVariable String username) {
        return userService.getFriends(username);
    }

    // Solicitudes pendientes (para usuario autenticado)
    @GetMapping("/requests")
    public List<FriendRequestDto> getPendingRequests() {
        return userService.getPendingRequests();
    }
}
