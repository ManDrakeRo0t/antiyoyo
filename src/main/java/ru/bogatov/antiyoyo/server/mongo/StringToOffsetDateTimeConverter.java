package ru.bogatov.antiyoyo.server.mongo;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
class StringToOffsetDateTimeConverter implements Converter<String, OffsetDateTime> {
    @Override
    public OffsetDateTime convert(String source) {
        return OffsetDateTime.parse(source);
    }
}
