package ru.bogatov.antiyoyo.server.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.bogatov.antiyoyo.game.model.GameSession;
import ru.bogatov.antiyoyo.server.dto.SessionCreateRequest;
import ru.bogatov.antiyoyo.server.dto.SessionCreateResponse;
import ru.bogatov.antiyoyo.server.dto.SessionJoinRequest;
import ru.bogatov.antiyoyo.server.dto.SessionResponse;
import ru.bogatov.antiyoyo.server.service.GameService;
import ru.bogatov.antiyoyo.server.service.SessionService;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/api/sessions")
public class SessionController {

    private final GameService gameService;
    private final SessionService service;

    @PostMapping
    public ResponseEntity<SessionCreateResponse> create(@RequestBody SessionCreateRequest request) {
        return ResponseEntity.ok(new SessionCreateResponse(gameService.createSession(request).getId()));
    }

    @PostMapping("/save/{id}")
    public ResponseEntity<Void> save(@PathVariable UUID id) {
        service.saveSessionToBase(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/restore/{id}")
    public ResponseEntity<Void> restore(@PathVariable UUID id) {
        gameService.restoreSession(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/join")
    public ResponseEntity<GameSession> join(@RequestBody SessionJoinRequest request) {
        return ResponseEntity.ok(gameService.joinSession(request));
    }

    @GetMapping
    public ResponseEntity<List<SessionResponse>> getSessions() {
        return ResponseEntity.ok(service.getActiveSessions());
    }


}
