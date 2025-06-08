package ru.bogatov.antiyoyo.game.engine.util;

import lombok.EqualsAndHashCode;
import org.springframework.util.ObjectUtils;

import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@EqualsAndHashCode
public final class Pair<S, T> {
    private final S first;
    private final T second;

    private Pair(S first, T second) {
        this.first = first;
        this.second = second;
    }

    public static <S, T> Pair<S, T> of(S first, T second) {
        return new Pair(first, second);
    }

    public S getFirst() {
        return this.first;
    }

    public T getSecond() {
        return this.second;
    }

    public static <S, T> Collector<org.springframework.data.util.Pair<S, T>, ?, Map<S, T>> toMap() {
        return Collectors.toMap(org.springframework.data.util.Pair::getFirst, org.springframework.data.util.Pair::getSecond);
    }


    public int hashCode() {
        int result = ObjectUtils.nullSafeHashCode(this.first);
        result = 31 * result + ObjectUtils.nullSafeHashCode(this.second);
        return result;
    }

    public String toString() {
        return String.format("%s->%s", this.first, this.second);
    }
}
