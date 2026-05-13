package ru.job4j.dreamjob.repository;

import ru.job4j.dreamjob.model.Vacancy;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Repository;

@Repository
public class MemoryVacancyRepository implements VacancyRepository {
    private int nextId = 1;
    private final Map<Integer, Vacancy> vacancies = new HashMap<>();

    private MemoryVacancyRepository() {
        save(new Vacancy(0, "Intern Java Developer", "Description for Intern Java Developer", LocalDateTime.of(2024, 6, 11, 0, 0)));
        save(new Vacancy(0, "Junior Java Developer", "Description for Junior Java Developer", LocalDateTime.of(2025, 3, 2, 0, 0)));
        save(new Vacancy(0, "Junior Java Developer", "Description for Junior Java Developer", LocalDateTime.of(2026, 2, 13, 0, 0)));
        save(new Vacancy(0, "Junior+ Java Developer", "Description for Junior+ Java Developer", LocalDateTime.of(2023, 1, 21, 0, 0)));
        save(new Vacancy(0, "Middle Java Developer", "Description for Middle Java Developer", LocalDateTime.of(2022, 6, 23, 0, 0)));
        save(new Vacancy(0, "Middle+ Java Developer", "Description for Middle+ Java Developer", LocalDateTime.of(2021, 5, 11, 0, 0)));
        save(new Vacancy(0, "Senior Java Developer", "Description for Senior Java Developer", LocalDateTime.of(2021, 6, 30, 0, 0)));
    }

    @Override
    public Vacancy save(Vacancy vacancy) {
        vacancy.setId(nextId++);
        vacancies.put(vacancy.getId(), vacancy);
        return vacancy;
    }

    @Override
    public boolean deleteById(int id) {
        return vacancies.remove(id) != null;
    }

    @Override
    public boolean update(Vacancy vacancy) {
        return vacancies.computeIfPresent(vacancy.getId(), 
                (id, oldVacancy) -> new Vacancy(oldVacancy.getId(), vacancy.getTitle(), vacancy.getDescription(), vacancy.getCreationDate())) != null;
    }

    @Override
    public Optional<Vacancy> findById(int id) {
        return Optional.ofNullable(vacancies.get(id));
    }

    @Override
    public Collection<Vacancy> findAll() {
        return vacancies.values();
    }
}