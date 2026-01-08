package org.example.dto;

import java.util.UUID;

import org.example.entity.ThemeMode;
import org.example.entity.User;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginResponse {
    @JsonProperty(access = Access.READ_ONLY)
    private UUID id;
    @JsonProperty(access = Access.READ_ONLY)
    private String username;
    @JsonProperty(access = Access.READ_ONLY)
    private String displayName;
    @JsonProperty(access = Access.READ_ONLY)
    private String avatarUrl;
    @JsonProperty(access = Access.READ_ONLY)
    private ThemeMode themeMode;

    public static UserLoginResponse from(User user) {
        return UserLoginResponse.builder().id(user.getId()).username(user.getUsername())
                .displayName(user.getDisplayName()).avatarUrl("/api/v1/avatars/" + user.getAvatarKey())
                .themeMode(user.getThemeMode()).build();
    }
}
