package ru.bogatov.antiyoyo.game.model.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
public class TownHall extends Entity implements Interactable {

    private UUID uuid = UUID.randomUUID();
    private Integer balance;
    private Integer balanceChanges;
    private Map<EntityType, Integer> prices;

    public TownHall(Integer balance, Integer balanceChanges) {
        this.balance = balance;
        this.balanceChanges = balanceChanges;
    }

    @Override
    public EntityType getType() {
        return EntityType.TOWN_HALL;
    }

    @Override
    public Integer getLevel() {
        return 1;
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
    public Integer getDefenceRadius() {
        return 1;
    }
}
