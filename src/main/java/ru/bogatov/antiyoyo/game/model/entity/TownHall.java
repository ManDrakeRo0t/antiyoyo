package ru.bogatov.antiyoyo.game.model.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.bogatov.antiyoyo.game.model.common.Currency;

import java.util.Map;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
public class TownHall extends Entity implements Interactable {

    private UUID uuid = UUID.randomUUID();
    private Currency storage;
    private Currency storageUpdate;
    private boolean isDronesAvailable;
    private Integer dronesLimit;
    private Map<EntityType, Currency> prices;

    public TownHall(Currency storage, Currency storageUpdate) {
        this.storage = storage;
        this.storageUpdate = storageUpdate;
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


}
