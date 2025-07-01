package ru.bogatov.antiyoyo.game.model.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.bogatov.antiyoyo.game.model.common.Currency;

import static ru.bogatov.antiyoyo.game.model.entity.EntityType.FOREST_FARM;

@EqualsAndHashCode(callSuper = true)
@Data
public class ForestFarm extends Entity implements Interactable, Sellable {

    @Override
    public EntityType getType() {
        return FOREST_FARM;
    }

    @Override
    public Currency getStorageChanges() {
        return Currency.of(0, 1,0);
    }

    @Override
    public Integer getLevel() {
        return 0;
    }

    @Override
    public Integer getAttackRadius() {
        return -1;
    }

    @Override
    public Integer getMoveRadius() {
        return -1;
    }

    @Override
    public Currency getPrice(Integer unitsCount) {
        return Currency.of(10, 2,1);
    }
}
