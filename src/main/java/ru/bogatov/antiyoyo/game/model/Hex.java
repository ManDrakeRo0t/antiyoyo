package ru.bogatov.antiyoyo.game.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.*;
import ru.bogatov.antiyoyo.game.model.entity.Entity;

import java.io.IOException;

@Data
@Builder
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class Hex {

    private Vector3 vector;
    private Boolean isAvailable;
    private HexColor color;
    @JsonInclude
    @JsonDeserialize(using = IgnoreDeserializer.class)
    private Entity entity;
    private Integer defenseLevel;
    private Boolean displayDefence;

}

 class IgnoreDeserializer extends JsonDeserializer<Object> {


     @Override
     public Object deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
         return null;
     }
 }