package ru.job4j.dreamjob.controller;

import net.jcip.annotations.ThreadSafe;

import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import ru.job4j.dreamjob.service.FileService;

@ThreadSafe
@Controller
@RequestMapping("/files")
public class FileController {
    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<byte[]> getById(@PathVariable int id) {
        return fileService.getFileById(id)
                .map(file -> ResponseEntity.ok()
                        .contentType(MediaTypeFactory.getMediaType(file.getName())
                                .orElse(MediaType.APPLICATION_OCTET_STREAM))
                        .body(file.getContent()))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
