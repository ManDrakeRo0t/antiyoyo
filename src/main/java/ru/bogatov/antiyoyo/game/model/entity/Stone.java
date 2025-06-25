package ru.bogatov.antiyoyo.game.model.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.bogatov.antiyoyo.game.model.common.Currency;

@EqualsAndHashCode(callSuper = true)
@Data
public class Stone extends Entity implements Mineable{
    @Override
    public EntityType getType() {
        return EntityType.STONE;
    }

    @Override
    public Currency getReward() {
        return Currency.of(0,0,3);
    }
}
