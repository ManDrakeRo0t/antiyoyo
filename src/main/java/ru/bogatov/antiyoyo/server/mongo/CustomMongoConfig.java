package ru.bogatov.antiyoyo.server.mongo;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.jsr310.Jsr310CodecProvider;
import org.bson.codecs.pojo.PojoCodecProvider;
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
