package ru.job4j.dreamjob.repository;

import java.util.Optional;

import net.jcip.annotations.ThreadSafe;

import org.sql2o.Sql2o;
import org.springframework.stereotype.Repository;

import ru.job4j.dreamjob.model.File;

@ThreadSafe
@Repository
public class Sql2oFileRepository implements FileRepository {
    private static final String SAVE = """
            INSERT INTO files (name, path)
            VALUES (:name, :path)
            """;
    private static final String FIND_BY_ID = """
            SELECT id, name, path
            FROM files
            WHERE id = :id
            """;
    private static final String DELETE_BY_ID = """
            DELETE FROM files
            WHERE id = :id
            """;

    private final Sql2o sql2o;

    public Sql2oFileRepository(Sql2o sql2o) {
        this.sql2o = sql2o;
    }

    @Override
    public File save(File file) {
        try (var connection = sql2o.open()) {
            var id = (Number) connection.createQuery(SAVE, true)
                    .bind(file)
                    .executeUpdate()
                    .getKey();
            return new File(id.intValue(), file.getName(), file.getPath());
        }
    }

    @Override
    public Optional<File> findById(int id) {
        try (var connection = sql2o.open()) {
            var file = connection.createQuery(FIND_BY_ID)
                    .addParameter("id", id)
                    .executeAndFetchFirst(File.class);
            return Optional.ofNullable(file);
        }
    }

    @Override
    public void deleteById(int id) {
        try (var connection = sql2o.open()) {
            connection.createQuery(DELETE_BY_ID)
                    .addParameter("id", id)
                    .executeUpdate();
        }
    }
}
