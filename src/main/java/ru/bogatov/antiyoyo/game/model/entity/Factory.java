package ru.bogatov.antiyoyo.game.model.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class Factory extends Entity implements Sellable {

    @Override
    public EntityType getType() {
        return null;
    }

    @Override
    public Integer getPrice() {
        return 12;
    }
}
