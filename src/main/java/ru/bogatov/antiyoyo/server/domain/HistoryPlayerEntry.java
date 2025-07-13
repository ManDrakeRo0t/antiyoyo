package ru.bogatov.antiyoyo.server.domain;

import lombok.Data;

import java.util.UUID;

@Data
public class HistoryPlayerEntry {

    private UUID userId;
    private Integer place;
    private Integer rankDelta;

}
