package ru.job4j.dreamjob.repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import net.jcip.annotations.ThreadSafe;

import org.springframework.stereotype.Repository;

import ru.job4j.dreamjob.model.Candidate;

@ThreadSafe
@Repository
public class MemoryCandidateRepository implements CandidateRepository {
    private final AtomicInteger nextId = new AtomicInteger(1);
    private final ConcurrentMap<Integer, Candidate> candidates = new ConcurrentHashMap<>();

    public MemoryCandidateRepository() {
        save(new Candidate(0, "Anton", "Go developer",
                LocalDateTime.of(2024, 6, 11, 0, 0), 1));
        save(new Candidate(0, "Petr", "Java developer",
                LocalDateTime.of(2025, 3, 2, 0, 0), 2));
        save(new Candidate(0, "Sidor", "Python developer",
                LocalDateTime.of(2026, 2, 13, 0, 0), 3));
        save(new Candidate(0, "Vladimir", "C# developer",
                LocalDateTime.of(2023, 1, 21, 0, 0), 1));
        save(new Candidate(0, "Dmitry", "C++ developer",
                LocalDateTime.of(2022, 6, 23, 0, 0), 2));
    }

    @Override
    public Candidate save(Candidate candidate) {
        candidate.setId(nextId.getAndIncrement());
        var savedCandidate = copy(candidate);
        candidates.put(savedCandidate.getId(), savedCandidate);
        return copy(savedCandidate);
    }

    @Override
    public boolean deleteById(int id) {
        return candidates.remove(id) != null;
    }

    @Override
    public boolean update(Candidate candidate) {
        var updatedCandidate = copy(candidate);
        return candidates.computeIfPresent(updatedCandidate.getId(),
                (id, oldCandidate) -> new Candidate(
                        oldCandidate.getId(),
                        updatedCandidate.getName(),
                        updatedCandidate.getDescription(),
                        oldCandidate.getCreationDate(),
                        updatedCandidate.getCityId())) != null;
    }

    @Override
    public Optional<Candidate> findById(int id) {
        return Optional.ofNullable(candidates.get(id)).map(this::copy);
    }

    @Override
    public Collection<Candidate> findAll() {
        return candidates.values().stream()
                .sorted(Comparator.comparing(Candidate::getId))
                .map(this::copy)
                .toList();
    }

    private Candidate copy(Candidate candidate) {
        return new Candidate(
                candidate.getId(),
                candidate.getName(),
                candidate.getDescription(),
                candidate.getCreationDate(),
                candidate.getCityId());
    }
}
