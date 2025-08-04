package ru.bogatov.antiyoyo.server.config.mongo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

import java.util.List;

@Configuration
public class CustomMongoConfig {

    @Bean
    MongoCustomConversions mongoCustomConversions(
            Vector3StringConverter vector3StringConverter,
            StringVector3Converter stringVector3Converter,
            OffsetDateTimeToStringConverter offsetDateTimeToStringConverter,
            StringToOffsetDateTimeConverter stringToOffsetDateTimeConverter
    ) {
        return new MongoCustomConversions(List.of(
                vector3StringConverter,
                stringVector3Converter,
                offsetDateTimeToStringConverter,
                stringToOffsetDateTimeConverter)
        );
    }



}
