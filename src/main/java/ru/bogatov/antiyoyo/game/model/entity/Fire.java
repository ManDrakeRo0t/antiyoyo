package ru.bogatov.antiyoyo.game.model.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.bogatov.antiyoyo.game.model.common.Currency;

@EqualsAndHashCode(callSuper = true)
@Data
public class Fire extends Entity {

    private Integer stage;

    public Fire() {
        this.stage = 4;
    }

    @Override
    public EntityType getType() {
        return EntityType.FIRE;
    }

    @Override
    public Currency getStorageChanges() {
        return Currency.of(-1 * this.stage,0,0);
    }
}
