package ru.job4j.dreamjob.repository;

import java.util.Collection;
import java.util.Optional;

import net.jcip.annotations.ThreadSafe;

import org.sql2o.Query;
import org.sql2o.Sql2o;
import org.springframework.stereotype.Repository;

import ru.job4j.dreamjob.model.Candidate;

@ThreadSafe
@Repository
public class Sql2oCandidateRepository implements CandidateRepository {
    private static final String SAVE = """
            INSERT INTO candidates (name, description, creation_date, city_id, file_id)
            VALUES (:name, :description, :creationDate, NULLIF(:cityId, 0), NULLIF(:fileId, 0))
            """;
    private static final String UPDATE = """
            UPDATE candidates
            SET name = :name,
                description = :description,
                city_id = NULLIF(:cityId, 0),
                file_id = NULLIF(:fileId, 0)
            WHERE id = :id
            """;
    private static final String DELETE_BY_ID = """
            DELETE FROM candidates
            WHERE id = :id
            """;
    private static final String FIND_BY_ID = """
            SELECT id, name, description, creation_date,
                   COALESCE(city_id, 0) AS city_id,
                   COALESCE(file_id, 0) AS file_id
            FROM candidates
            WHERE id = :id
            """;
    private static final String FIND_ALL = """
            SELECT id, name, description, creation_date,
                   COALESCE(city_id, 0) AS city_id,
                   COALESCE(file_id, 0) AS file_id
            FROM candidates
            ORDER BY id
            """;

    private final Sql2o sql2o;

    public Sql2oCandidateRepository(Sql2o sql2o) {
        this.sql2o = sql2o;
    }

    @Override
    public Candidate save(Candidate candidate) {
        try (var connection = sql2o.open()) {
            var id = (Number) connection.createQuery(SAVE, true)
                    .bind(candidate)
                    .executeUpdate()
                    .getKey();
            candidate.setId(id.intValue());
            return candidate;
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
    public boolean update(Candidate candidate) {
        try (var connection = sql2o.open()) {
            var result = connection.createQuery(UPDATE)
                    .bind(candidate)
                    .executeUpdate()
                    .getResult();
            return result > 0;
        }
    }

    @Override
    public Optional<Candidate> findById(int id) {
        try (var connection = sql2o.open()) {
            var candidate = withCandidateMapping(connection.createQuery(FIND_BY_ID))
                    .addParameter("id", id)
                    .executeAndFetchFirst(Candidate.class);
            return Optional.ofNullable(candidate);
        }
    }

    @Override
    public Collection<Candidate> findAll() {
        try (var connection = sql2o.open()) {
            return withCandidateMapping(connection.createQuery(FIND_ALL))
                    .executeAndFetch(Candidate.class);
        }
    }

    private Query withCandidateMapping(Query query) {
        return query.setColumnMappings(Candidate.COLUMN_MAPPING);
    }
}
