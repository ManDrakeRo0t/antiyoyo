package ru.bogatov.antiyoyo.game.model.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Currency implements Cloneable {
    private int gold;
    private int tree;
    private int stone;

    public static final Currency EMPTY = Currency.of(0,0,0).clone();

    public boolean isAffordable(Currency price) {
        return price.getGold() <= this.gold &&
                price.getTree() <= this.tree &&
                price.getStone() <= this.stone;
    }

    public static Currency of(Integer gold, Integer tree, Integer stone) {
        return Currency.builder()
                .gold(gold)
                .stone(stone)
                .tree(tree)
                .build();
    }

    public void add(Currency other) {
        this.stone += other.getStone();
        this.gold += other.getGold();
        this.tree += other.getTree();
    }

    public void remove(Currency other) {
        this.stone -= other.getStone();
        this.gold -= other.getGold();
        this.tree -= other.getTree();
    }

    public Currency split(int count) {
        return Currency.of(
                this.gold / count,
                this.tree / count,
                this.stone / count
        );
    }

    @Override
    public Currency clone() {
        return Currency.of(
                this.getGold(),
                this.getTree(),
                this.getStone()
        );
    }
}
