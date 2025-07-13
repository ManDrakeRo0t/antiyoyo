package ru.bogatov.antiyoyo.server.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Document("history")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameHistory {

    @MongoId
    private UUID id;
    private UUID sessionId;
    private String sessionName;
    private Integer playersCount;
    private OffsetDateTime startTime;
    private OffsetDateTime endTime;
    private List<HistoryPlayerEntry> players;

}
