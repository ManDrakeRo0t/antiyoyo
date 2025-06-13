package ru.bogatov.antiyoyo.server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class SessionCreateResponse {
    private UUID sessionId;
}
