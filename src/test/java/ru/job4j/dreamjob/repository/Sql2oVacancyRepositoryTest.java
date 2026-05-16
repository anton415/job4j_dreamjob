package ru.job4j.dreamjob.repository;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sql2o.Sql2o;

import ru.job4j.dreamjob.model.File;
import ru.job4j.dreamjob.model.Vacancy;

import static org.assertj.core.api.Assertions.assertThat;

class Sql2oVacancyRepositoryTest {
    private Sql2o sql2o;

    @BeforeEach
    void setUp() throws SQLException, IOException {
        sql2o = Sql2oTestHelper.initSql2o();
    }

    @Test
    void whenSaveThenCanFindById() {
        var repository = new Sql2oVacancyRepository(sql2o);
        var file = saveFile("java.png");
        var creationDate = LocalDateTime.of(2026, 5, 16, 10, 30);
        var vacancy = new Vacancy(0, "Java", "Backend", creationDate, true, 1, file.getId());

        var savedVacancy = repository.save(vacancy);

        assertThat(savedVacancy.getId()).isPositive();
        assertThat(repository.findById(savedVacancy.getId()))
                .get()
                .satisfies(found -> {
                    assertThat(found.getTitle()).isEqualTo("Java");
                    assertThat(found.getDescription()).isEqualTo("Backend");
                    assertThat(found.getCreationDate()).isEqualTo(creationDate);
                    assertThat(found.getVisible()).isTrue();
                    assertThat(found.getCityId()).isEqualTo(1);
                    assertThat(found.getFileId()).isEqualTo(file.getId());
                });
    }

    @Test
    void whenFindAllThenReturnVacanciesOrderedById() {
        var repository = new Sql2oVacancyRepository(sql2o);
        repository.save(new Vacancy(0, "Junior Java", "First", LocalDateTime.now()));
        repository.save(new Vacancy(0, "Middle Java", "Second", LocalDateTime.now()));

        assertThat(repository.findAll())
                .extracting(Vacancy::getTitle)
                .containsExactly("Junior Java", "Middle Java");
    }

    @Test
    void whenUpdateThenFieldsAreChanged() {
        var repository = new Sql2oVacancyRepository(sql2o);
        var creationDate = LocalDateTime.of(2026, 5, 16, 11, 0);
        var savedVacancy = repository.save(new Vacancy(0, "Java", "Backend", creationDate, true, 1));
        var updated = new Vacancy(savedVacancy.getId(), "Kotlin", "Server", LocalDateTime.now(), false, 2, 0);

        assertThat(repository.update(updated)).isTrue();
        assertThat(repository.findById(savedVacancy.getId()))
                .get()
                .satisfies(found -> {
                    assertThat(found.getTitle()).isEqualTo("Kotlin");
                    assertThat(found.getDescription()).isEqualTo("Server");
                    assertThat(found.getCreationDate()).isEqualTo(creationDate);
                    assertThat(found.getVisible()).isFalse();
                    assertThat(found.getCityId()).isEqualTo(2);
                    assertThat(found.getFileId()).isEqualTo(0);
                });
    }

    @Test
    void whenDeleteThenVacancyIsNotFound() {
        var repository = new Sql2oVacancyRepository(sql2o);
        var savedVacancy = repository.save(new Vacancy(0, "Java", "Backend", LocalDateTime.now()));

        assertThat(repository.deleteById(savedVacancy.getId())).isTrue();

        assertThat(repository.findById(savedVacancy.getId())).isEmpty();
    }

    @Test
    void whenUpdateOrDeleteUnknownVacancyThenFalse() {
        var repository = new Sql2oVacancyRepository(sql2o);
        var unknownVacancy = new Vacancy(100, "Unknown", "None", LocalDateTime.now());

        assertThat(repository.update(unknownVacancy)).isFalse();
        assertThat(repository.deleteById(unknownVacancy.getId())).isFalse();
        assertThat(repository.findById(unknownVacancy.getId())).isEmpty();
    }

    private File saveFile(String name) {
        return new Sql2oFileRepository(sql2o).save(new File(name, "/tmp/" + name));
    }
}
