package ru.bogatov.antiyoyo.server.dto;

import lombok.Builder;
import lombok.Data;
import ru.bogatov.antiyoyo.server.domain.GameHistory;

import java.util.List;

@Data
@Builder
public class UserDto {

    private String login;
    private Integer rating;
    private Integer totalGames;
    private Integer winGames;

    private List<GameHistory> userHistory;

}
