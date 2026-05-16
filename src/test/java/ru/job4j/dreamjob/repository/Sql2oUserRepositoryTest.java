package ru.job4j.dreamjob.repository;

import java.io.IOException;
import java.sql.SQLException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sql2o.Sql2o;

import ru.job4j.dreamjob.model.User;

import static org.assertj.core.api.Assertions.assertThat;

class Sql2oUserRepositoryTest {
    private Sql2o sql2o;

    @BeforeEach
    void setUp() throws SQLException, IOException {
        sql2o = Sql2oTestHelper.initSql2o();
    }

    @Test
    void whenSaveThenCanFindByEmailAndPassword() {
        var repository = new Sql2oUserRepository(sql2o);
        var user = new User(0, "mail@test.com", "Anton", "password");

        var savedUser = repository.save(user);

        assertThat(savedUser).isPresent();
        assertThat(savedUser.get().getId()).isPositive();
        assertThat(repository.findByEmailAndPassword("mail@test.com", "password"))
                .get()
                .satisfies(found -> {
                    assertThat(found.getId()).isEqualTo(savedUser.get().getId());
                    assertThat(found.getEmail()).isEqualTo("mail@test.com");
                    assertThat(found.getName()).isEqualTo("Anton");
                    assertThat(found.getPassword()).isEqualTo("password");
                });
    }

    @Test
    void whenSaveUsersWithSameEmailThenSecondSaveReturnsEmpty() {
        var repository = new Sql2oUserRepository(sql2o);
        var firstUser = new User(0, "mail@test.com", "Anton", "password");
        var secondUser = new User(0, "mail@test.com", "Petr", "secret");

        assertThat(repository.save(firstUser)).isPresent();

        assertThat(repository.save(secondUser)).isEmpty();
    }

    @Test
    void whenFindByWrongCredentialsThenEmpty() {
        var repository = new Sql2oUserRepository(sql2o);
        repository.save(new User(0, "mail@test.com", "Anton", "password"));

        assertThat(repository.findByEmailAndPassword("mail@test.com", "wrong")).isEmpty();
        assertThat(repository.findByEmailAndPassword("unknown@test.com", "password")).isEmpty();
    }
}
