package org.example.dto;

import java.time.Instant;

public record LastMessageDto(
                UserSummaryDto sender,
                String content,
                Instant sentAt) {
}
