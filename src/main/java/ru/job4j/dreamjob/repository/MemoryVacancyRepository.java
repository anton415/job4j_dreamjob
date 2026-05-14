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

import ru.job4j.dreamjob.model.Vacancy;

@ThreadSafe
@Repository
public class MemoryVacancyRepository implements VacancyRepository {
    private final AtomicInteger nextId = new AtomicInteger(1);
    private final ConcurrentMap<Integer, Vacancy> vacancies = new ConcurrentHashMap<>();

    public MemoryVacancyRepository() {
        save(new Vacancy(0, "Intern Java Developer", "Description for Intern Java Developer",
                LocalDateTime.of(2024, 6, 11, 0, 0), 1));
        save(new Vacancy(0, "Junior Java Developer", "Description for Junior Java Developer",
                LocalDateTime.of(2025, 3, 2, 0, 0), 2));
        save(new Vacancy(0, "Junior Java Developer", "Description for Junior Java Developer",
                LocalDateTime.of(2026, 2, 13, 0, 0), 3));
        save(new Vacancy(0, "Junior+ Java Developer", "Description for Junior+ Java Developer",
                LocalDateTime.of(2023, 1, 21, 0, 0), 1));
        save(new Vacancy(0, "Middle Java Developer", "Description for Middle Java Developer",
                LocalDateTime.of(2022, 6, 23, 0, 0), 2));
        save(new Vacancy(0, "Middle+ Java Developer", "Description for Middle+ Java Developer",
                LocalDateTime.of(2021, 5, 11, 0, 0), 3));
        save(new Vacancy(0, "Senior Java Developer", "Description for Senior Java Developer",
                LocalDateTime.of(2021, 6, 30, 0, 0), 1));
    }

    @Override
    public Vacancy save(Vacancy vacancy) {
        vacancy.setId(nextId.getAndIncrement());
        var savedVacancy = copy(vacancy);
        vacancies.put(savedVacancy.getId(), savedVacancy);
        return copy(savedVacancy);
    }

    @Override
    public boolean deleteById(int id) {
        return vacancies.remove(id) != null;
    }

    @Override
    public boolean update(Vacancy vacancy) {
        var updatedVacancy = copy(vacancy);
        return vacancies.computeIfPresent(updatedVacancy.getId(),
                (id, oldVacancy) -> new Vacancy(
                        oldVacancy.getId(),
                        updatedVacancy.getTitle(),
                        updatedVacancy.getDescription(),
                        oldVacancy.getCreationDate(),
                        updatedVacancy.getVisible(),
                        updatedVacancy.getCityId(),
                        updatedVacancy.getFileId())) != null;
    }

    @Override
    public Optional<Vacancy> findById(int id) {
        return Optional.ofNullable(vacancies.get(id)).map(this::copy);
    }

    @Override
    public Collection<Vacancy> findAll() {
        return vacancies.values().stream()
                .sorted(Comparator.comparing(Vacancy::getId))
                .map(this::copy)
                .toList();
    }

    private Vacancy copy(Vacancy vacancy) {
        return new Vacancy(
                vacancy.getId(),
                vacancy.getTitle(),
                vacancy.getDescription(),
                vacancy.getCreationDate(),
                vacancy.getVisible(),
                vacancy.getCityId(),
                vacancy.getFileId());
    }
}
