package ru.job4j.dreamjob.repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.sql.DataSource;

import org.h2.jdbcx.JdbcDataSource;
import org.h2.tools.RunScript;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sql2o.Sql2o;
import org.sql2o.converters.Converter;
import org.sql2o.converters.ConverterException;
import org.sql2o.quirks.NoQuirks;

import ru.job4j.dreamjob.model.City;
import ru.job4j.dreamjob.model.File;
import ru.job4j.dreamjob.model.Vacancy;

import static org.assertj.core.api.Assertions.assertThat;

class Sql2oRepositoriesTest {
    private Sql2o sql2o;

    @BeforeEach
    void setUp() throws SQLException, IOException {
        var dataSource = dataSource();
        sql2o = new Sql2o(dataSource, new NoQuirks(createConverters()));
        runScript(dataSource, "db/scripts/001_ddl_create_cities_table.sql");
        runScript(dataSource, "db/scripts/002_dml_insert_cities.sql");
        runScript(dataSource, "db/scripts/003_ddl_create_files_table.sql");
        runScript(dataSource, "db/scripts/004_ddl_create_vacancies_table.sql");
    }

    @Test
    void whenFindAllCitiesThenReturnCitiesFromDatabase() {
        var repository = new Sql2oCityRepository(sql2o);

        assertThat(repository.findAll())
                .containsExactly(
                        new City(1, "Москва"),
                        new City(2, "Санкт-Петербург"),
                        new City(3, "Екатеринбург")
                );
    }

    @Test
    void whenSaveFindAndDeleteFileThenRepositoryUsesDatabase() {
        var repository = new Sql2oFileRepository(sql2o);
        var savedFile = repository.save(new File("logo.png", "/tmp/logo.png"));

        assertThat(savedFile.getId()).isPositive();
        assertThat(repository.findById(savedFile.getId()))
                .get()
                .satisfies(file -> {
                    assertThat(file.getName()).isEqualTo("logo.png");
                    assertThat(file.getPath()).isEqualTo("/tmp/logo.png");
                });

        repository.deleteById(savedFile.getId());

        assertThat(repository.findById(savedFile.getId())).isEmpty();
    }

    @Test
    void whenSaveUpdateFindAndDeleteVacancyThenRepositoryUsesDatabase() {
        var fileRepository = new Sql2oFileRepository(sql2o);
        var file = fileRepository.save(new File("vacancy.png", "/tmp/vacancy.png"));
        var repository = new Sql2oVacancyRepository(sql2o);
        var creationDate = LocalDateTime.of(2026, 5, 15, 10, 30);
        var vacancy = new Vacancy(0, "Java", "Description", creationDate, true, 1, file.getId());

        var savedVacancy = repository.save(vacancy);

        assertThat(savedVacancy.getId()).isPositive();
        assertThat(repository.findById(savedVacancy.getId()))
                .get()
                .satisfies(saved -> {
                    assertThat(saved.getCreationDate()).isEqualTo(creationDate);
                    assertThat(saved.getVisible()).isTrue();
                    assertThat(saved.getCityId()).isEqualTo(1);
                    assertThat(saved.getFileId()).isEqualTo(file.getId());
                });

        var updated = new Vacancy(savedVacancy.getId(), "Updated", "New", LocalDateTime.now(), false, 2, 0);

        assertThat(repository.update(updated)).isTrue();
        assertThat(repository.findById(savedVacancy.getId()))
                .get()
                .satisfies(found -> {
                    assertThat(found.getTitle()).isEqualTo("Updated");
                    assertThat(found.getVisible()).isFalse();
                    assertThat(found.getCityId()).isEqualTo(2);
                    assertThat(found.getFileId()).isEqualTo(0);
                    assertThat(found.getCreationDate()).isEqualTo(creationDate);
                });

        assertThat(repository.deleteById(savedVacancy.getId())).isTrue();
        assertThat(repository.findById(savedVacancy.getId())).isEmpty();
    }

    private static DataSource dataSource() {
        var dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:" + UUID.randomUUID()
                + ";MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;DB_CLOSE_DELAY=-1");
        return dataSource;
    }

    private static void runScript(DataSource dataSource, String script) throws SQLException, IOException {
        try (var connection = dataSource.getConnection();
             var reader = Files.newBufferedReader(Path.of(script))) {
            RunScript.execute(connection, reader);
        }
    }

    private static Map<Class, Converter> createConverters() {
        var converters = new HashMap<Class, Converter>();
        converters.put(LocalDateTime.class, localDateTimeConverter());
        return converters;
    }

    private static Converter<LocalDateTime> localDateTimeConverter() {
        return new Converter<>() {
            @Override
            public LocalDateTime convert(Object value) throws ConverterException {
                if (value == null) {
                    return null;
                }
                if (value instanceof Timestamp timestamp) {
                    return timestamp.toLocalDateTime();
                }
                throw new ConverterException("Cannot convert value to LocalDateTime");
            }

            @Override
            public Object toDatabaseParam(LocalDateTime value) {
                return value == null ? null : Timestamp.valueOf(value);
            }
        };
    }
}
