package ru.bogatov.antiyoyo.game.model.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class Tree extends Entity implements Mineable {

    @Override
    public EntityType getType() {
         return EntityType.TREE;
    }


    @Override
    public Integer getReward() {
        return 3;
    }
}
