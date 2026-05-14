package ru.job4j.dreamjob.service;

import java.util.Optional;

import ru.job4j.dreamjob.dto.FileDto;
import ru.job4j.dreamjob.model.File;

public interface FileService {

    File save(File file);

    File save(FileDto fileDto);

    Optional<File> findById(int id);

    Optional<FileDto> getFileById(int id);

    void deleteById(int id);
}
