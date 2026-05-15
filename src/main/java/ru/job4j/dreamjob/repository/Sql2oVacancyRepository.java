package ru.job4j.dreamjob.repository;

import java.util.Collection;
import java.util.Optional;

import net.jcip.annotations.ThreadSafe;

import org.sql2o.Query;
import org.sql2o.Sql2o;
import org.springframework.stereotype.Repository;

import ru.job4j.dreamjob.model.Vacancy;

@ThreadSafe
@Repository
public class Sql2oVacancyRepository implements VacancyRepository {
    private static final String SAVE = """
            INSERT INTO vacancies (title, description, creation_date, visible, city_id, file_id)
            VALUES (:title, :description, :creationDate, :visible, NULLIF(:cityId, 0), NULLIF(:fileId, 0))
            """;
    private static final String UPDATE = """
            UPDATE vacancies
            SET title = :title,
                description = :description,
                visible = :visible,
                city_id = NULLIF(:cityId, 0),
                file_id = NULLIF(:fileId, 0)
            WHERE id = :id
            """;
    private static final String DELETE_BY_ID = """
            DELETE FROM vacancies
            WHERE id = :id
            """;
    private static final String FIND_BY_ID = """
            SELECT id, title, description, creation_date, visible,
                   COALESCE(city_id, 0) AS city_id,
                   COALESCE(file_id, 0) AS file_id
            FROM vacancies
            WHERE id = :id
            """;
    private static final String FIND_ALL = """
            SELECT id, title, description, creation_date, visible,
                   COALESCE(city_id, 0) AS city_id,
                   COALESCE(file_id, 0) AS file_id
            FROM vacancies
            ORDER BY id
            """;

    private final Sql2o sql2o;

    public Sql2oVacancyRepository(Sql2o sql2o) {
        this.sql2o = sql2o;
    }

    @Override
    public Vacancy save(Vacancy vacancy) {
        try (var connection = sql2o.open()) {
            var id = (Number) connection.createQuery(SAVE, true)
                    .bind(vacancy)
                    .executeUpdate()
                    .getKey();
            vacancy.setId(id.intValue());
            return vacancy;
        }
    }

    @Override
    public boolean deleteById(int id) {
        try (var connection = sql2o.open()) {
            var result = connection.createQuery(DELETE_BY_ID)
                    .addParameter("id", id)
                    .executeUpdate()
                    .getResult();
            return result > 0;
        }
    }

    @Override
    public boolean update(Vacancy vacancy) {
        try (var connection = sql2o.open()) {
            var result = connection.createQuery(UPDATE)
                    .bind(vacancy)
                    .executeUpdate()
                    .getResult();
            return result > 0;
        }
    }

    @Override
    public Optional<Vacancy> findById(int id) {
        try (var connection = sql2o.open()) {
            var vacancy = withVacancyMapping(connection.createQuery(FIND_BY_ID))
                    .addParameter("id", id)
                    .executeAndFetchFirst(Vacancy.class);
            return Optional.ofNullable(vacancy);
        }
    }

    @Override
    public Collection<Vacancy> findAll() {
        try (var connection = sql2o.open()) {
            return withVacancyMapping(connection.createQuery(FIND_ALL))
                    .executeAndFetch(Vacancy.class);
        }
    }

    private Query withVacancyMapping(Query query) {
        return query.setColumnMappings(Vacancy.COLUMN_MAPPING);
    }
}
