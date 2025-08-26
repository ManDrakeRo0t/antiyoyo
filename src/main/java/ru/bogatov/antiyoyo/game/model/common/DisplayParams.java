package ru.bogatov.antiyoyo.game.model.common;

import lombok.*;

import java.util.Set;

@Data
@Builder
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class DisplayParams {

    private Boolean glue;
    private Boolean displayDefence;
    private Set<Color> visibleFor;
    private Boolean isAvailable;

}
