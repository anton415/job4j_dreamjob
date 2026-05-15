package ru.job4j.dreamjob.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

import net.jcip.annotations.ThreadSafe;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import ru.job4j.dreamjob.dto.FileDto;
import ru.job4j.dreamjob.model.File;
import ru.job4j.dreamjob.repository.FileRepository;

@ThreadSafe
@Service
public class SimpleFileService implements FileService {
    private final FileRepository fileRepository;
    private final String storageDirectory;

    @Autowired
    public SimpleFileService(@Qualifier("sql2oFileRepository") FileRepository sql2oFileRepository,
                             @Value("${file.directory}") String storageDirectory) {
        this.fileRepository = sql2oFileRepository;
        this.storageDirectory = storageDirectory;
    }

    public SimpleFileService(FileRepository fileRepository) {
        this(fileRepository, "files");
    }

    @Override
    public File save(File file) {
        return fileRepository.save(file);
    }

    @Override
    public File save(FileDto fileDto) {
        var sourceName = getSourceName(fileDto.getName());
        var path = storageDirectory + java.io.File.separator + UUID.randomUUID() + sourceName;
        try {
            Files.createDirectories(Path.of(storageDirectory));
            Files.write(Path.of(path), fileDto.getContent());
            return fileRepository.save(new File(sourceName, path));
        } catch (IOException exception) {
            throw new IllegalStateException("Could not save file", exception);
        }
    }

    @Override
    public Optional<File> findById(int id) {
        return fileRepository.findById(id);
    }

    @Override
    public Optional<FileDto> getFileById(int id) {
        return findById(id).flatMap(this::readFile);
    }

    @Override
    public void deleteById(int id) {
        findById(id).ifPresent(file -> {
            try {
                Files.deleteIfExists(Path.of(file.getPath()));
                fileRepository.deleteById(id);
            } catch (IOException exception) {
                throw new IllegalStateException("Could not delete file", exception);
            }
        });
    }

    private Optional<FileDto> readFile(File file) {
        try {
            return Optional.of(new FileDto(file.getName(), Files.readAllBytes(Path.of(file.getPath()))));
        } catch (IOException exception) {
            return Optional.empty();
        }
    }

    private String getSourceName(String sourceName) {
        if (sourceName == null || sourceName.isBlank()) {
            return "file";
        }
        try {
            var fileName = Path.of(sourceName).getFileName();
            if (fileName == null || fileName.toString().isBlank()) {
                return "file";
            }
            return fileName.toString();
        } catch (InvalidPathException exception) {
            return "file";
        }
    }
}
