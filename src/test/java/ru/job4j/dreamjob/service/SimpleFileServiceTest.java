package ru.job4j.dreamjob.service;

import java.nio.file.Path;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import ru.job4j.dreamjob.dto.FileDto;
import ru.job4j.dreamjob.repository.MemoryFileRepository;

import static org.assertj.core.api.Assertions.assertThat;

class SimpleFileServiceTest {

    @Test
    void whenSaveFileThenCanReadItById(@TempDir Path tempDir) {
        var service = service(tempDir);
        var content = new byte[] {1, 2, 3};

        var savedFile = service.save(new FileDto("photo.png", content));

        assertThat(savedFile.getId()).isPositive();
        assertThat(savedFile.getName()).isEqualTo("photo.png");
        assertThat(Path.of(savedFile.getPath())).exists();
        assertThat(service.getFileById(savedFile.getId()))
                .get()
                .satisfies(file -> {
                    assertThat(file.getName()).isEqualTo("photo.png");
                    assertThat(file.getContent()).containsExactly(content);
                });
    }

    @Test
    void whenSaveFileWithPathLikeNameThenOnlyBaseNameIsStored(@TempDir Path tempDir) {
        var service = service(tempDir);

        var savedFile = service.save(new FileDto("nested/source/resume.pdf", new byte[] {4, 5}));

        assertThat(savedFile.getName()).isEqualTo("resume.pdf");
        assertThat(Path.of(savedFile.getPath())).exists();
        assertThat(service.getFileById(savedFile.getId()))
                .get()
                .extracting(FileDto::getName)
                .isEqualTo("resume.pdf");
    }

    @ParameterizedTest
    @MethodSource("unsafeFileNames")
    void whenSaveFileWithUnsafeNameThenDefaultNameIsUsed(String sourceName, @TempDir Path tempDir) {
        var service = service(tempDir);
        var content = new byte[] {6, 7};

        var savedFile = service.save(new FileDto(sourceName, content));

        assertThat(savedFile.getName()).isEqualTo("file");
        assertThat(Path.of(savedFile.getPath())).exists();
        assertThat(service.getFileById(savedFile.getId()))
                .get()
                .satisfies(file -> {
                    assertThat(file.getName()).isEqualTo("file");
                    assertThat(file.getContent()).containsExactly(content);
                });
    }

    private static Stream<String> unsafeFileNames() {
        return Stream.of(null, "", "   ", "bad\0name.png");
    }

    private static SimpleFileService service(Path tempDir) {
        return new SimpleFileService(new MemoryFileRepository(), tempDir.toString());
    }
}
