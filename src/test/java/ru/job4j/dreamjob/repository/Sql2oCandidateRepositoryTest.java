package ru.job4j.dreamjob.repository;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sql2o.Sql2o;

import ru.job4j.dreamjob.model.Candidate;
import ru.job4j.dreamjob.model.File;

import static org.assertj.core.api.Assertions.assertThat;

class Sql2oCandidateRepositoryTest {
    private Sql2o sql2o;

    @BeforeEach
    void setUp() throws SQLException, IOException {
        sql2o = Sql2oTestHelper.initSql2o();
    }

    @Test
    void whenSaveThenCanFindById() {
        var repository = new Sql2oCandidateRepository(sql2o);
        var file = saveFile("resume.png");
        var creationDate = LocalDateTime.of(2026, 5, 16, 10, 30);
        var candidate = new Candidate(0, "Anton", "Java developer", creationDate, 1, file.getId());

        var savedCandidate = repository.save(candidate);

        assertThat(savedCandidate.getId()).isPositive();
        assertThat(repository.findById(savedCandidate.getId()))
                .get()
                .satisfies(found -> {
                    assertThat(found.getName()).isEqualTo("Anton");
                    assertThat(found.getDescription()).isEqualTo("Java developer");
                    assertThat(found.getCreationDate()).isEqualTo(creationDate);
                    assertThat(found.getCityId()).isEqualTo(1);
                    assertThat(found.getFileId()).isEqualTo(file.getId());
                });
    }

    @Test
    void whenFindAllThenReturnCandidatesOrderedById() {
        var repository = new Sql2oCandidateRepository(sql2o);
        repository.save(new Candidate(0, "Anton", "Java", LocalDateTime.now()));
        repository.save(new Candidate(0, "Petr", "Go", LocalDateTime.now()));

        assertThat(repository.findAll())
                .extracting(Candidate::getName)
                .containsExactly("Anton", "Petr");
    }

    @Test
    void whenUpdateThenFieldsAreChanged() {
        var repository = new Sql2oCandidateRepository(sql2o);
        var creationDate = LocalDateTime.of(2026, 5, 16, 11, 0);
        var savedCandidate = repository.save(new Candidate(0, "Anton", "Java", creationDate, 1));
        var updated = new Candidate(savedCandidate.getId(), "Petr", "Go", LocalDateTime.now(), 2, 0);

        assertThat(repository.update(updated)).isTrue();
        assertThat(repository.findById(savedCandidate.getId()))
                .get()
                .satisfies(found -> {
                    assertThat(found.getName()).isEqualTo("Petr");
                    assertThat(found.getDescription()).isEqualTo("Go");
                    assertThat(found.getCreationDate()).isEqualTo(creationDate);
                    assertThat(found.getCityId()).isEqualTo(2);
                    assertThat(found.getFileId()).isEqualTo(0);
                });
    }

    @Test
    void whenDeleteThenCandidateIsNotFound() {
        var repository = new Sql2oCandidateRepository(sql2o);
        var savedCandidate = repository.save(new Candidate(0, "Anton", "Java", LocalDateTime.now()));

        assertThat(repository.deleteById(savedCandidate.getId())).isTrue();

        assertThat(repository.findById(savedCandidate.getId())).isEmpty();
    }

    @Test
    void whenUpdateOrDeleteUnknownCandidateThenFalse() {
        var repository = new Sql2oCandidateRepository(sql2o);
        var unknownCandidate = new Candidate(100, "Unknown", "None", LocalDateTime.now());

        assertThat(repository.update(unknownCandidate)).isFalse();
        assertThat(repository.deleteById(unknownCandidate.getId())).isFalse();
        assertThat(repository.findById(unknownCandidate.getId())).isEmpty();
    }

    private File saveFile(String name) {
        return new Sql2oFileRepository(sql2o).save(new File(name, "/tmp/" + name));
    }
}
