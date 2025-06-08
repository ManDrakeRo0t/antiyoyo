package ru.bogatov.antiyoyo.server.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;
import ru.bogatov.antiyoyo.game.model.GameSession;
import ru.bogatov.antiyoyo.server.domain.GameEvent;
import ru.bogatov.antiyoyo.server.service.GameService;

@Controller
@Slf4j
@AllArgsConstructor
public class GameWsController {

    private final GameService gameService;

    @MessageMapping("sessions.{session_id}.event.send")
    public void sendMessage(@DestinationVariable("session_id") String sessionId, @Payload GameEvent event) {
        gameService.handleEvent(sessionId, event);
    }

    @SubscribeMapping("sessions.{session_id}.event.fetch")
    public GameSession fetchMessages(@DestinationVariable("session_id") String sessionId) {
        return null;
    }

}
