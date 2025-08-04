package ru.bogatov.antiyoyo.server.config.mongo;



import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import ru.bogatov.antiyoyo.game.model.common.Vector3;

@Component
public class Vector3StringConverter implements Converter<Vector3, String> {

    @Override
    public String convert(Vector3 source) {
        return source.toString();
    }
}
