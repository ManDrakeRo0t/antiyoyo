package ru.bogatov.antiyoyo.server.job;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.bogatov.antiyoyo.game.model.GameSession;
import ru.bogatov.antiyoyo.server.repository.SessionRepository;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
public class EndGameJob {

    private final SessionRepository sessionRepository;
    private final TaskSchedulingService taskSchedulingService;
    static final int THRESHOLD_MINUTES = 5;

    @Scheduled(fixedRate = 3 * 60 * 1000)
    public void removeInactiveSessions() {

        log.info("Started remove sessions job");
        OffsetDateTime thresholdDate = OffsetDateTime.now().minusMinutes(THRESHOLD_MINUTES);
        List<GameSession> sessions = sessionRepository.getAllSessions().stream().toList();

        sessions.forEach(session -> {

            if (session.getWinnerId() == null) {
                if (session.getLastInteraction().isBefore(thresholdDate)) {
                    removeSession(session);
                }
            } else {
                if (session.isRatingProcessed()) {
                    removeSession(session);
                }
            }

        });
    }

    private void removeSession(GameSession session) {
        sessionRepository.removeSession(session.getId());
        if (session.getSkipMoveTaskId() != null) {
            taskSchedulingService.cancelTask(session.getSkipMoveTaskId().toString());
        }
        log.info("Removed session id {}", session.getId());
    }
}
