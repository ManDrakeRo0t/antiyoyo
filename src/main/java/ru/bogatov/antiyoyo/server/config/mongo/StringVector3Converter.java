package ru.bogatov.antiyoyo.server.config.mongo;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import ru.bogatov.antiyoyo.game.model.common.Vector3;

@Component
public class StringVector3Converter implements Converter<String, Vector3> {
    @Override
    public Vector3 convert(String source) {
        String values = source.substring(source.indexOf('(') + 1, source.indexOf(')'));
        String[] parts = values.split(",\\s*");

        int x = Integer.parseInt(parts[0].split("=")[1]);
        int y = Integer.parseInt(parts[1].split("=")[1]);
        int z = Integer.parseInt(parts[2].split("=")[1]);
        return Vector3.from(x, y, z);
    }
}
