package ru.bogatov.antiyoyo.game.model;

import lombok.Data;
import lombok.experimental.Accessors;
import ru.bogatov.antiyoyo.game.model.common.Color;

@Data
@Accessors(chain = true)
public class Alliance {
    private Color from;
    private Color to;
    private Integer untilLap;
}
