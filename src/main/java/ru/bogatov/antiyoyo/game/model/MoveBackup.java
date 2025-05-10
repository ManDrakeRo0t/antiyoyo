package ru.bogatov.antiyoyo.game.model;

import lombok.Data;
import ru.bogatov.antiyoyo.game.model.entity.Entity;


@Data
public class MoveBackup {
    private Integer order;
    private Integer fromPlayer;
    private Integer toPlayer;
    private Integer depositChangesFrom;
    private Integer depositChangesTo;
    private Vector3 vector;
    private Entity newEntity;
    private HexColor newColor;
    private Entity oldEntity;
    private HexColor oldColor;
}
