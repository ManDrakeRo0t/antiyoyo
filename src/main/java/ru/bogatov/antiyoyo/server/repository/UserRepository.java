package ru.bogatov.antiyoyo.server.repository;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import ru.bogatov.antiyoyo.server.domain.User;

import java.util.List;
import java.util.UUID;

@Repository
public class UserRepository {

    private final MongoTemplate mongoTemplate;

    public UserRepository(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public User getUsersById(UUID id) {
        return mongoTemplate.findOne(new Query().addCriteria(Criteria.where("_id").is(id)), User.class);
    }

    public User saveUser(User user) {
        if (findByLogin(user.getLogin()) != null) {
            throw new RuntimeException("Login already taken");
        }
        return mongoTemplate.save(user);
    }

    public void updateUserStats(UUID id, Integer newRating, Integer winGames, Integer totalGames) {
        Query query = new Query().addCriteria(Criteria.where("_id").is(id));
        Update update = new Update()
                .set("rating", newRating)
                .set("totalGames", totalGames)
                .set("winGames", winGames);
        mongoTemplate.updateFirst(query, update, User.class);
    }

    public List<User> findUsersByIds(List<UUID> ids) {
        Query query = new Query(Criteria.where("id").in(ids));
        return mongoTemplate.find(query, User.class);
    }

    public User findByLoginAndPassword(String login, String password) {
        return mongoTemplate.findOne(new Query(Criteria.where("login").is(login).and("password").is(password)), User.class);
    }

    public User findByLogin(String login) {
        return mongoTemplate.findOne(new Query(Criteria.where("login").is(login)), User.class);
    }


}
