package ru.bogatov.antiyoyo.game.model.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.bogatov.antiyoyo.game.model.common.Currency;

import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@Data
public class Mine extends Entity implements Farmable {

    @Override
    public EntityType getType() {
        return EntityType.MINE;
    }

    @Override
    public Currency getStorageChanges() {
        return Currency.of(0,0,1);
    }


    @Override
    public Currency getFarm(Set<Entity> neighbors) {
        if (neighbors.stream().anyMatch(MineFarm.class::isInstance)) {
            return Currency.of(0,0,3);
        }
        return Currency.EMPTY.clone();
    }

    @Override
    public EntityType farmableType() {
        return EntityType.STONE;
    }
}
