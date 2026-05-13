package ru.job4j.dreamjob.repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import ru.job4j.dreamjob.model.Candidate;

@Repository
public class MemoryCandidateRepository implements CandidateRepository {
    private int nextId = 1;
    private final Map<Integer, Candidate> candidates = new HashMap<>();

    private MemoryCandidateRepository() {
        save(new Candidate(0, "Anton", "Go developer", LocalDateTime.of(2024, 6, 11, 0, 0)));
        save(new Candidate(0, "Petr", "Java developer", LocalDateTime.of(2025, 3, 2, 0, 0)));
        save(new Candidate(0, "Sidor", "Python developer", LocalDateTime.of(2026, 2, 13, 0, 0)));
        save(new Candidate(0, "Vladimir", "C# developer", LocalDateTime.of(2023, 1, 21, 0, 0)));
        save(new Candidate(0, "Dmitry", "C++ developer", LocalDateTime.of(2022, 6, 23, 0, 0)));
    }

    @Override
    public Candidate save(Candidate candidate) {
        candidate.setId(nextId++);
        candidates.put(candidate.getId(), candidate);
        return candidate;
    }

    @Override
    public boolean deleteById(int id) {
        return candidates.remove(id) != null;
    }

    @Override
    public boolean update(Candidate candidate) {
        return candidates.computeIfPresent(candidate.getId(), 
                (id, oldCandidate) -> new Candidate(oldCandidate.getId(), candidate.getName(), candidate.getDescription(), oldCandidate.getCreationDate())) != null;
    }

    @Override
    public Optional<Candidate> findById(int id) {
        return Optional.ofNullable(candidates.get(id));
    }

    @Override
    public Collection<Candidate> findAll() {
        return candidates.values();
    }
}
