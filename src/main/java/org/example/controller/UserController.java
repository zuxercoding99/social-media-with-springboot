package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.dto.FriendDto;
import org.example.dto.FriendRequestDto;
import org.example.dto.UpdateBannerColorDto;
import org.example.dto.UpdateBioDto;
import org.example.dto.UpdateDisplayNameDto;
import org.example.dto.UpdateThemeModeDto;
import org.example.dto.UserProfileDto;
import org.example.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // Perfil público
    @GetMapping("/{username}/profile")
    public UserProfileDto getUserProfile(@PathVariable String username) {
        return userService.getUserProfile(username);
    }

    // Mi perfil
    @GetMapping("/me")
    public UserProfileDto getMyProfile() {
        return userService.getMyProfile();
    }

    // Amigos
    @GetMapping("/{username}/friends")
    public List<FriendDto> getFriends(@PathVariable String username) {
        return userService.getFriends(username);
    }

    // Solicitudes recibidas pendientes (para usuario autenticado)
    @GetMapping("/me/requests-received")
    public List<FriendRequestDto> getPendingRequests() {
        return userService.getPendingRequests();
    }

    // Solicitudes enviadas pendientes (para usuario autenticado)
    @GetMapping("/me/requests-sent")
    public List<FriendRequestDto> getSentRequests() {
        return userService.getSentRequests();
    }

    // Actualiza el nombre de usuario
    @PatchMapping("/me/display-name")
    public ResponseEntity<Void> updateDisplayName(
            @RequestBody @Valid UpdateDisplayNameDto dto) {

        userService.updateDisplayName(dto.displayName());

        return ResponseEntity.noContent().build();
    }

    // Actualiza la bio
    @PatchMapping("/me/bio")
    public ResponseEntity<Void> updateBio(
            @RequestBody @Valid UpdateBioDto dto) {

        userService.updateBio(dto.bio());

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/me/banner-color")
    public ResponseEntity<Void> updateBannerColor(
            @RequestBody @Valid UpdateBannerColorDto dto) {

        userService.updateBannerColor(dto.bannerColor());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/me/theme")
    public ResponseEntity<Void> updateTheme(
            @RequestBody @Valid UpdateThemeModeDto dto) {

        userService.updateThemeMode(dto.themeMode());
        return ResponseEntity.noContent().build();
    }

}
