package ru.bogatov.antiyoyo.server.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.bogatov.antiyoyo.game.model.GameSession;
import ru.bogatov.antiyoyo.server.dto.SessionCreateRequest;
import ru.bogatov.antiyoyo.server.dto.SessionCreateResponse;
import ru.bogatov.antiyoyo.server.dto.SessionJoinRequest;
import ru.bogatov.antiyoyo.server.service.GameService;

@RestController
@AllArgsConstructor
@RequestMapping("/api/sessions")
public class SessionController {

    private final GameService gameService;

    @PostMapping
    public ResponseEntity<SessionCreateResponse> create(@RequestBody SessionCreateRequest request) {
        return ResponseEntity.ok(new SessionCreateResponse(gameService.createSession(request).getId()));
    }

    @PostMapping("/join")
    public ResponseEntity<GameSession> join(@RequestBody SessionJoinRequest request) {
        return ResponseEntity.ok(gameService.joinSession(request));
    }


}
