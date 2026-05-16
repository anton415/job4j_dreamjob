package ru.job4j.dreamjob.controller;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import ru.job4j.dreamjob.dto.FileDto;
import ru.job4j.dreamjob.service.FileService;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class FileControllerTest {
    private MockMvc mockMvc;

    @Mock
    private FileService fileService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new FileController(fileService))
                .build();
    }

    @Test
    void whenGetExistingFileThenGetContent() throws Exception {
        var content = new byte[] {1, 2, 3};
        when(fileService.getFileById(1)).thenReturn(Optional.of(new FileDto("photo.png", content)));

        mockMvc.perform(get("/files/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG))
                .andExpect(content().bytes(content));

        verify(fileService).getFileById(1);
    }

    @Test
    void whenGetUnknownFileThenGetNotFoundStatus() throws Exception {
        when(fileService.getFileById(99)).thenReturn(Optional.empty());

        mockMvc.perform(get("/files/99"))
                .andExpect(status().isNotFound());

        verify(fileService).getFileById(99);
    }
}
