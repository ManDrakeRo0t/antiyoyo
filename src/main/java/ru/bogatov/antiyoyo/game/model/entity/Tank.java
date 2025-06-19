package ru.bogatov.antiyoyo.game.model.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.bogatov.antiyoyo.game.model.common.Currency;

@EqualsAndHashCode(callSuper = true)
@Data
public class Tank extends Entity implements Sellable, Interactable {

    @Override
    public EntityType getType() {
        return EntityType.TANK;
    }

    @Override
    public Integer getGoldChanges() {
        return -54;
    }

    @Override
    public Currency getPrice(Integer unitsCount) {
        return Currency.of(40, 0,0);
    }

    @Override
    public Integer getLevel() {
        return 4;
    }

    @Override
    public Integer getAttackRadius() {
        return 1;
    }

    @Override
    public Integer getMoveRadius() {
        return 4;
    }




}
