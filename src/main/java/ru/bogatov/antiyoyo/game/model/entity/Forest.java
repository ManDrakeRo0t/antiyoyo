package ru.bogatov.antiyoyo.game.model.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.bogatov.antiyoyo.game.model.common.Currency;

import java.util.Set;

import static ru.bogatov.antiyoyo.game.model.entity.EntityType.FOREST;
import static ru.bogatov.antiyoyo.game.model.entity.EntityType.TREE;

@EqualsAndHashCode(callSuper = true)
@Data
public class Forest extends Entity implements Farmable {

    @Override
    public EntityType getType() {
        return FOREST;
    }

    @Override
    public Currency getStorageChanges() {
        return Currency.of(0,1,0);
    }

    @Override
    public Currency getFarm(Set<Entity> neighbors) {
        if (neighbors.stream().anyMatch(entity -> entity instanceof ForestFarm)) {
            return Currency.of(0,3,0);
        }
        return Currency.EMPTY.clone();
    }

    @Override
    public EntityType farmableType() {
        return TREE;
    }
}
