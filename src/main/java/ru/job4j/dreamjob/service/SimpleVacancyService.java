package ru.job4j.dreamjob.service;

import net.jcip.annotations.ThreadSafe;

import ru.job4j.dreamjob.dto.FileDto;
import ru.job4j.dreamjob.model.File;
import ru.job4j.dreamjob.model.Vacancy;
import ru.job4j.dreamjob.repository.VacancyRepository;

import java.util.Collection;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@ThreadSafe
@Service
public class SimpleVacancyService implements VacancyService {
    private final VacancyRepository vacancyRepository;
    private final FileService fileService;

    @Autowired
    public SimpleVacancyService(VacancyRepository vacancyRepository, FileService fileService) {
        this.vacancyRepository = vacancyRepository;
        this.fileService = fileService;
    }

    public SimpleVacancyService(VacancyRepository vacancyRepository) {
        this(vacancyRepository, null);
    }

    @Override
    public Vacancy save(Vacancy vacancy) {
        return vacancyRepository.save(vacancy);
    }

    @Override
    public Vacancy save(Vacancy vacancy, FileDto fileDto) {
        saveFile(fileDto).ifPresent(file -> vacancy.setFileId(file.getId()));
        return vacancyRepository.save(vacancy);
    }

    @Override
    public boolean deleteById(int id) {
        var vacancyOptional = vacancyRepository.findById(id);
        if (vacancyOptional.isEmpty()) {
            return false;
        }
        var isDeleted = vacancyRepository.deleteById(id);
        if (isDeleted) {
            deleteFile(vacancyOptional.get().getFileId());
        }
        return isDeleted;
    }

    @Override
    public boolean update(Vacancy vacancy) {
        return vacancyRepository.update(vacancy);
    }

    @Override
    public boolean update(Vacancy vacancy, FileDto fileDto) {
        var oldVacancyOptional = vacancyRepository.findById(vacancy.getId());
        if (oldVacancyOptional.isEmpty()) {
            return false;
        }
        var oldVacancy = oldVacancyOptional.get();
        var savedFileOptional = saveFile(fileDto);
        savedFileOptional.ifPresent(file -> vacancy.setFileId(file.getId()));
        if (savedFileOptional.isEmpty()) {
            vacancy.setFileId(oldVacancy.getFileId());
        }
        var isUpdated = vacancyRepository.update(vacancy);
        if (isUpdated) {
            savedFileOptional.ifPresent(file -> deleteFile(oldVacancy.getFileId()));
        } else {
            savedFileOptional.ifPresent(file -> deleteFile(file.getId()));
        }
        return isUpdated;
    }

    @Override
    public Optional<Vacancy> findById(int id) {
        return vacancyRepository.findById(id);
    }

    @Override
    public Collection<Vacancy> findAll() {
        return vacancyRepository.findAll();
    }

    private Optional<File> saveFile(FileDto fileDto) {
        if (!hasContent(fileDto) || fileService == null) {
            return Optional.empty();
        }
        return Optional.of(fileService.save(fileDto));
    }

    private boolean hasContent(FileDto fileDto) {
        return fileDto != null && fileDto.getContent().length > 0;
    }

    private void deleteFile(int fileId) {
        if (fileId > 0 && fileService != null) {
            fileService.deleteById(fileId);
        }
    }
}
