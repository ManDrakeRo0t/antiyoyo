package ru.bogatov.antiyoyo.game.model.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class Grave extends Entity {

    @Override
    public EntityType getType() {
        return EntityType.GRAVE;
    }

}
