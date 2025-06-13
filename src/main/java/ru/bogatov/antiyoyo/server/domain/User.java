package ru.bogatov.antiyoyo.server.domain;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.util.UUID;

@Data
@Document("users")
@Builder
public class User {

    @MongoId
    private UUID id;
    private String login;
    private String password;

}
