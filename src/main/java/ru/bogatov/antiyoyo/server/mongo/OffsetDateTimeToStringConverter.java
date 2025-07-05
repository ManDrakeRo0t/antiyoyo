package ru.bogatov.antiyoyo.server.mongo;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
class OffsetDateTimeToStringConverter implements Converter<OffsetDateTime, String> {
    @Override
    public String convert(OffsetDateTime source) {
        return source.toString();
    }
}


