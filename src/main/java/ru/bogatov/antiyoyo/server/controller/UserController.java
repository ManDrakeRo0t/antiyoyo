package ru.bogatov.antiyoyo.server.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.bogatov.antiyoyo.game.model.GameSession;
import ru.bogatov.antiyoyo.server.dto.UserDto;
import ru.bogatov.antiyoyo.server.service.UserService;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @GetMapping("/history/{id}")
    public ResponseEntity<UserDto> get(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getUserWithHistory(id));
    }

    @GetMapping("/top-rating")
    public ResponseEntity<List<UserDto>> get() {
        return ResponseEntity.ok(userService.getTopRatingUsers());
    }

    @GetMapping("/{userId}/active-session")
    public ResponseEntity<UUID> getActiveSession(@PathVariable UUID userId) {
        UUID id = userService.findActiveGameSession(userId);
        if (id == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(id);
    }

}
