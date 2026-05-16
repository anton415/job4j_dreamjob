package ru.job4j.dreamjob.repository;

import java.sql.SQLException;
import java.util.Optional;

import net.jcip.annotations.ThreadSafe;

import org.sql2o.Sql2o;
import org.sql2o.Sql2oException;
import org.springframework.stereotype.Repository;

import ru.job4j.dreamjob.model.User;

@ThreadSafe
@Repository
public class Sql2oUserRepository implements UserRepository {
    private static final String SAVE = """
            INSERT INTO users (email, name, password)
            VALUES (:email, :name, :password)
            """;
    private static final String FIND_BY_EMAIL_AND_PASSWORD = """
            SELECT id, email, name, password
            FROM users
            WHERE email = :email AND password = :password
            """;

    private final Sql2o sql2o;

    public Sql2oUserRepository(Sql2o sql2o) {
        this.sql2o = sql2o;
    }

    @Override
    public Optional<User> save(User user) {
        try (var connection = sql2o.open()) {
            var id = (Number) connection.createQuery(SAVE, true)
                    .bind(user)
                    .executeUpdate()
                    .getKey();
            user.setId(id.intValue());
            return Optional.of(user);
        } catch (Sql2oException e) {
            if (isUniqueConstraintViolation(e)) {
                return Optional.empty();
            }
            throw e;
        }
    }

    @Override
    public Optional<User> findByEmailAndPassword(String email, String password) {
        try (var connection = sql2o.open()) {
            var user = connection.createQuery(FIND_BY_EMAIL_AND_PASSWORD)
                    .addParameter("email", email)
                    .addParameter("password", password)
                    .executeAndFetchFirst(User.class);
            return Optional.ofNullable(user);
        }
    }

    private boolean isUniqueConstraintViolation(Throwable exception) {
        var current = exception;
        while (current != null) {
            if (current instanceof SQLException sqlException
                    && "23505".equals(sqlException.getSQLState())) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}
