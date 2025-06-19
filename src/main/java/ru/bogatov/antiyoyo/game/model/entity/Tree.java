package ru.bogatov.antiyoyo.game.model.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.bogatov.antiyoyo.game.model.common.Currency;

@EqualsAndHashCode(callSuper = true)
@Data
public class Tree extends Entity implements Mineable {

    @Override
    public EntityType getType() {
         return EntityType.TREE;
    }


    @Override
    public Currency getReward() {
        return Currency.of(0, 3, 0);
    }
}
