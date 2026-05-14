package ru.job4j.dreamjob.service;

import ru.job4j.dreamjob.dto.FileDto;
import ru.job4j.dreamjob.model.Vacancy;

import java.util.Collection;
import java.util.Optional;

public interface VacancyService {

    Vacancy save(Vacancy vacancy);

    Vacancy save(Vacancy vacancy, FileDto fileDto);

    boolean deleteById(int id);

    boolean update(Vacancy vacancy);

    boolean update(Vacancy vacancy, FileDto fileDto);

    Optional<Vacancy> findById(int id);

    Collection<Vacancy> findAll();
}
