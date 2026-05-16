package ru.job4j.dreamjob.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import ru.job4j.dreamjob.dto.FileDto;
import ru.job4j.dreamjob.filter.SessionFilter;
import ru.job4j.dreamjob.model.Candidate;
import ru.job4j.dreamjob.model.City;
import ru.job4j.dreamjob.service.CandidateService;
import ru.job4j.dreamjob.service.CityService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@ExtendWith(MockitoExtension.class)
class CandidateControllerTest {
    private MockMvc mockMvc;

    @Mock
    private CandidateService candidateService;

    @Mock
    private CityService cityService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new CandidateController(candidateService, cityService))
                .addFilters(new SessionFilter())
                .build();
    }

    @Test
    void whenRequestCandidateListPageThenGetPageWithCandidates() throws Exception {
        var candidates = List.of(
                new Candidate(1, "Anton", "Java", LocalDateTime.now()),
                new Candidate(2, "Petr", "QA", LocalDateTime.now()));
        when(candidateService.findAll()).thenReturn(candidates);

        mockMvc.perform(get("/candidates"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("candidates", candidates))
                .andExpect(view().name("candidates/list"));

        verify(candidateService).findAll();
    }

    @Test
    void whenRequestCandidateCreationPageThenGetPageWithCities() throws Exception {
        var cities = List.of(new City(1, "Москва"), new City(2, "Магадан"));
        when(cityService.findAll()).thenReturn(cities);

        mockMvc.perform(get("/candidates/create"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("cities", cities))
                .andExpect(view().name("candidates/create"));

        verify(cityService).findAll();
    }

    @Test
    void whenPostCandidateWithFileThenSameDataAndRedirectToCandidatesPage() throws Exception {
        var file = new MockMultipartFile(
                "file", "photo.png", MediaType.IMAGE_PNG_VALUE, new byte[] {1, 2, 3});
        when(candidateService.save(any(Candidate.class), any(FileDto.class))).thenReturn(new Candidate());

        mockMvc.perform(multipart("/candidates/create")
                        .file(file)
                        .param("name", "Anton")
                        .param("description", "Backend")
                        .param("cityId", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/candidates"));

        var candidateCaptor = ArgumentCaptor.forClass(Candidate.class);
        var fileDtoCaptor = ArgumentCaptor.forClass(FileDto.class);
        verify(candidateService).save(candidateCaptor.capture(), fileDtoCaptor.capture());
        assertCandidate(candidateCaptor.getValue());
        assertFileDto(fileDtoCaptor.getValue());
    }

    @Test
    void whenRequestCandidatePageThenGetPageWithCandidateAndCities() throws Exception {
        var candidate = new Candidate(1, "Anton", "Backend", LocalDateTime.now(), 2, 3);
        var cities = List.of(new City(1, "Москва"), new City(2, "Магадан"));
        when(candidateService.findById(1)).thenReturn(Optional.of(candidate));
        when(cityService.findAll()).thenReturn(cities);

        mockMvc.perform(get("/candidates/1"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("candidate", candidate))
                .andExpect(model().attribute("cities", cities))
                .andExpect(view().name("candidates/one"));

        verify(candidateService).findById(1);
        verify(cityService).findAll();
    }

    @Test
    void whenRequestUnknownCandidateThenGetErrorPageWithMessage() throws Exception {
        when(candidateService.findById(99)).thenReturn(Optional.empty());

        mockMvc.perform(get("/candidates/99"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("message", "Кандидат с указанным идентификатором не найден"))
                .andExpect(view().name("errors/404"));

        verify(candidateService).findById(99);
    }

    @Test
    void whenPostUpdatedCandidateWithFileThenRedirectToCandidatesPage() throws Exception {
        var file = new MockMultipartFile(
                "file", "photo.png", MediaType.IMAGE_PNG_VALUE, new byte[] {1, 2, 3});
        when(candidateService.update(any(Candidate.class), any(FileDto.class))).thenReturn(true);

        mockMvc.perform(multipart("/candidates/update")
                        .file(file)
                        .param("id", "1")
                        .param("name", "Anton")
                        .param("description", "Backend")
                        .param("cityId", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/candidates"));

        var candidateCaptor = ArgumentCaptor.forClass(Candidate.class);
        var fileDtoCaptor = ArgumentCaptor.forClass(FileDto.class);
        verify(candidateService).update(candidateCaptor.capture(), fileDtoCaptor.capture());
        assertThat(candidateCaptor.getValue().getId()).isEqualTo(1);
        assertCandidate(candidateCaptor.getValue());
        assertFileDto(fileDtoCaptor.getValue());
    }

    @Test
    void whenPostUnknownCandidateThenGetErrorPageWithMessage() throws Exception {
        when(candidateService.update(any(Candidate.class), any())).thenReturn(false);

        mockMvc.perform(multipart("/candidates/update")
                        .param("id", "99")
                        .param("name", "Anton")
                        .param("description", "Backend")
                        .param("cityId", "2"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("message", "Кандидат с указанным идентификатором не найден"))
                .andExpect(view().name("errors/404"));

        verify(candidateService).update(any(Candidate.class), any());
    }

    @Test
    void whenDeleteExistingCandidateThenRedirectToCandidatesPage() throws Exception {
        when(candidateService.deleteById(1)).thenReturn(true);

        mockMvc.perform(get("/candidates/delete/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/candidates"));

        verify(candidateService).deleteById(1);
    }

    @Test
    void whenDeleteUnknownCandidateThenGetErrorPageWithMessage() throws Exception {
        when(candidateService.deleteById(99)).thenReturn(false);

        mockMvc.perform(get("/candidates/delete/99"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("message", "Кандидат с указанным идентификатором не найден"))
                .andExpect(view().name("errors/404"));

        verify(candidateService).deleteById(99);
    }

    private static void assertCandidate(Candidate candidate) {
        assertThat(candidate.getName()).isEqualTo("Anton");
        assertThat(candidate.getDescription()).isEqualTo("Backend");
        assertThat(candidate.getCityId()).isEqualTo(2);
    }

    private static void assertFileDto(FileDto fileDto) {
        assertThat(fileDto.getName()).isEqualTo("photo.png");
        assertThat(fileDto.getContent()).containsExactly(1, 2, 3);
    }
}
