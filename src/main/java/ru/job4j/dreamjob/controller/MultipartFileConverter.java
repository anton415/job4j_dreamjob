package ru.job4j.dreamjob.controller;

import java.io.IOException;

import org.springframework.web.multipart.MultipartFile;

import ru.job4j.dreamjob.dto.FileDto;

final class MultipartFileConverter {
    private MultipartFileConverter() {
    }

    static FileDto toFileDto(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }
        return new FileDto(file.getOriginalFilename(), file.getBytes());
    }
}
