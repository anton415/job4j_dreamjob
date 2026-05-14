package ru.job4j.dreamjob.repository;

import org.junit.jupiter.api.Test;

import ru.job4j.dreamjob.model.Candidate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class MemoryCandidateRepositoryTest {
    private static final int THREADS_COUNT = 8;

    @Test
    void whenSaveCandidatesConcurrentlyThenIdsAreUnique() throws Exception {
        var repository = new MemoryCandidateRepository();
        var candidates = saveCandidates(repository, 100);

        assertThat(candidates)
                .extracting(Candidate::getId)
                .doesNotHaveDuplicates();
        assertThat(repository.findAll()).hasSize(105);
    }

    @Test
    void whenUpdateOrDeleteUnknownCandidateThenFalse() {
        var repository = new MemoryCandidateRepository();
        var unknownCandidate = new Candidate(100, "Unknown", "None", LocalDateTime.now());

        assertThat(repository.update(unknownCandidate)).isFalse();
        assertThat(repository.deleteById(unknownCandidate.getId())).isFalse();
        assertThat(repository.findById(unknownCandidate.getId())).isEmpty();
    }

    @Test
    void whenSavedCandidateChangedOutsideThenRepositoryKeepsSnapshot() {
        var repository = new MemoryCandidateRepository();
        var candidate = repository.save(candidate(0));

        candidate.setName("Changed");
        candidate.setCityId(3);

        assertThat(repository.findById(candidate.getId()))
                .get()
                .extracting(Candidate::getName)
                .isEqualTo("Candidate 0");
        assertThat(repository.findById(candidate.getId()))
                .get()
                .extracting(Candidate::getCityId)
                .isEqualTo(0);
    }

    @Test
    void whenUpdateCandidateThenCityIdIsUpdated() {
        var repository = new MemoryCandidateRepository();
        var candidate = repository.save(candidate(0));
        var updatedCandidate = new Candidate(
                candidate.getId(),
                "Updated",
                "Updated description",
                LocalDateTime.now(),
                2);

        assertThat(repository.update(updatedCandidate)).isTrue();
        assertThat(repository.findById(candidate.getId()))
                .get()
                .extracting(Candidate::getCityId)
                .isEqualTo(2);
    }

    private static List<Candidate> saveCandidates(MemoryCandidateRepository repository, int count)
            throws InterruptedException, ExecutionException {
        var executor = Executors.newFixedThreadPool(THREADS_COUNT);
        var start = new CountDownLatch(1);
        try {
            var futures = IntStream.range(0, count)
                    .mapToObj(index -> executor.submit(() -> {
                        start.await();
                        return repository.save(candidate(index));
                    }))
                    .toList();
            start.countDown();
            return collectCandidates(futures);
        } finally {
            executor.shutdownNow();
        }
    }

    private static List<Candidate> collectCandidates(List<java.util.concurrent.Future<Candidate>> futures)
            throws InterruptedException, ExecutionException {
        var candidates = new ArrayList<Candidate>();
        for (var future : futures) {
            candidates.add(future.get());
        }
        return candidates;
    }

    private static Candidate candidate(int index) {
        return new Candidate(
                0,
                "Candidate " + index,
                "Description " + index,
                LocalDateTime.of(2026, 1, 1, 0, 0));
    }
}
