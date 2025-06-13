package ru.bogatov.antiyoyo.server.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.bogatov.antiyoyo.server.domain.GameMap;
import ru.bogatov.antiyoyo.server.service.GameMapService;
import ru.bogatov.antiyoyo.server.service.GameService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@RestController
@RequestMapping("/api/maps")
@AllArgsConstructor
public class GameMapController {

    private final GameService gameService;
    private final GameMapService gameMapService;

    @PostMapping
    public ResponseEntity<GameMap> saveMap(@RequestBody GameMap gameMap) {
        gameService.validateGameMap(gameMap);
        return ResponseEntity.accepted().body(gameMapService.saveMap(gameMap));
    }

    @GetMapping
    public ResponseEntity<List<GameMap>> list() {
        return ResponseEntity.ok(gameMapService.getMaps());
    }

    @GetMapping("/{id}")
    public ResponseEntity<GameMap> list(@PathVariable String id) {
        return ResponseEntity.ok(gameMapService.getById(UUID.fromString(id)));
    }


    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleException(RuntimeException ex) {
        return ResponseEntity.status(BAD_REQUEST).body(Map.of("error", ex.getMessage()));
    }
}
