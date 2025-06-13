package ru.bogatov.antiyoyo.server.repository;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import ru.bogatov.antiyoyo.server.domain.User;

@Repository
public class UserRepository {

    private final MongoTemplate mongoTemplate;

    public UserRepository(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public User saveUser(User user) {
        if (findByLogin(user.getLogin()) != null) {
            throw new RuntimeException("Login already taken");
        }
        return mongoTemplate.save(user);
    }

    public User findByLoginAndPassword(String login, String password) {
        return mongoTemplate.findOne(new Query(Criteria.where("login").is(login).and("password").is(password)), User.class);
    }

    public User findByLogin(String login) {
        return mongoTemplate.findOne(new Query(Criteria.where("login").is(login)), User.class);
    }


}
