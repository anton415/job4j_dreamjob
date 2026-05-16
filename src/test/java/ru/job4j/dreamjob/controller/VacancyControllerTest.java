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
import ru.job4j.dreamjob.model.City;
import ru.job4j.dreamjob.model.Vacancy;
import ru.job4j.dreamjob.service.CityService;
import ru.job4j.dreamjob.service.VacancyService;

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
class VacancyControllerTest {
    private MockMvc mockMvc;

    @Mock
    private VacancyService vacancyService;

    @Mock
    private CityService cityService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new VacancyController(vacancyService, cityService))
                .addFilters(new SessionFilter())
                .build();
    }

    @Test
    void whenRequestVacancyListPageThenGetPageWithVacancies() throws Exception {
        var vacancies = List.of(
                new Vacancy(1, "Java", "Middle Java", LocalDateTime.now()),
                new Vacancy(2, "QA", "Manual QA", LocalDateTime.now()));
        when(vacancyService.findAll()).thenReturn(vacancies);

        mockMvc.perform(get("/vacancies"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("vacancies", vacancies))
                .andExpect(view().name("vacancies/list"));

        verify(vacancyService).findAll();
    }

    @Test
    void whenRequestVacancyCreationPageThenGetPageWithCities() throws Exception {
        var cities = List.of(new City(1, "Москва"), new City(2, "Магадан"));
        when(cityService.findAll()).thenReturn(cities);

        mockMvc.perform(get("/vacancies/create"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("cities", cities))
                .andExpect(view().name("vacancies/create"));

        verify(cityService).findAll();
    }

    @Test
    void whenPostVacancyWithFileThenSameDataAndRedirectToVacanciesPage() throws Exception {
        var file = new MockMultipartFile(
                "file", "logo.png", MediaType.IMAGE_PNG_VALUE, new byte[] {1, 2, 3});
        when(vacancyService.save(any(Vacancy.class), any(FileDto.class))).thenReturn(new Vacancy());

        mockMvc.perform(multipart("/vacancies/create")
                        .file(file)
                        .param("title", "Java")
                        .param("description", "Backend")
                        .param("cityId", "2")
                        .param("visible", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/vacancies"));

        var vacancyCaptor = ArgumentCaptor.forClass(Vacancy.class);
        var fileDtoCaptor = ArgumentCaptor.forClass(FileDto.class);
        verify(vacancyService).save(vacancyCaptor.capture(), fileDtoCaptor.capture());
        assertVacancy(vacancyCaptor.getValue());
        assertFileDto(fileDtoCaptor.getValue());
    }

    @Test
    void whenRequestVacancyPageThenGetPageWithVacancyAndCities() throws Exception {
        var vacancy = new Vacancy(1, "Java", "Backend", LocalDateTime.now(), true, 2, 3);
        var cities = List.of(new City(1, "Москва"), new City(2, "Магадан"));
        when(vacancyService.findById(1)).thenReturn(Optional.of(vacancy));
        when(cityService.findAll()).thenReturn(cities);

        mockMvc.perform(get("/vacancies/1"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("vacancy", vacancy))
                .andExpect(model().attribute("cities", cities))
                .andExpect(view().name("vacancies/one"));

        verify(vacancyService).findById(1);
        verify(cityService).findAll();
    }

    @Test
    void whenSomeExceptionThrownThenGetErrorPageWithMessage() throws Exception {
        when(vacancyService.findById(99)).thenReturn(Optional.empty());

        mockMvc.perform(get("/vacancies/99"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("message", "Вакансия с указанным идентификатором не найдена"))
                .andExpect(view().name("errors/404"));

        verify(vacancyService).findById(99);
    }

    @Test
    void whenPostUpdatedVacancyWithFileThenRedirectToVacanciesPage() throws Exception {
        var file = new MockMultipartFile(
                "file", "logo.png", MediaType.IMAGE_PNG_VALUE, new byte[] {1, 2, 3});
        when(vacancyService.update(any(Vacancy.class), any(FileDto.class))).thenReturn(true);

        mockMvc.perform(multipart("/vacancies/update")
                        .file(file)
                        .param("id", "1")
                        .param("title", "Java")
                        .param("description", "Backend")
                        .param("cityId", "2")
                        .param("visible", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/vacancies"));

        var vacancyCaptor = ArgumentCaptor.forClass(Vacancy.class);
        var fileDtoCaptor = ArgumentCaptor.forClass(FileDto.class);
        verify(vacancyService).update(vacancyCaptor.capture(), fileDtoCaptor.capture());
        assertThat(vacancyCaptor.getValue().getId()).isEqualTo(1);
        assertVacancy(vacancyCaptor.getValue());
        assertFileDto(fileDtoCaptor.getValue());
    }

    @Test
    void whenPostUnknownVacancyThenGetErrorPageWithMessage() throws Exception {
        when(vacancyService.update(any(Vacancy.class), any())).thenReturn(false);

        mockMvc.perform(multipart("/vacancies/update")
                        .param("id", "99")
                        .param("title", "Java")
                        .param("description", "Backend")
                        .param("cityId", "2")
                        .param("visible", "true"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("message", "Вакансия с указанным идентификатором не найдена"))
                .andExpect(view().name("errors/404"));

        verify(vacancyService).update(any(Vacancy.class), any());
    }

    @Test
    void whenDeleteExistingVacancyThenRedirectToVacanciesPage() throws Exception {
        when(vacancyService.deleteById(1)).thenReturn(true);

        mockMvc.perform(get("/vacancies/delete/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/vacancies"));

        verify(vacancyService).deleteById(1);
    }

    @Test
    void whenDeleteUnknownVacancyThenGetErrorPageWithMessage() throws Exception {
        when(vacancyService.deleteById(99)).thenReturn(false);

        mockMvc.perform(get("/vacancies/delete/99"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("message", "Вакансия с указанным идентификатором не найдена"))
                .andExpect(view().name("errors/404"));

        verify(vacancyService).deleteById(99);
    }

    private static void assertVacancy(Vacancy vacancy) {
        assertThat(vacancy.getTitle()).isEqualTo("Java");
        assertThat(vacancy.getDescription()).isEqualTo("Backend");
        assertThat(vacancy.getCityId()).isEqualTo(2);
        assertThat(vacancy.getVisible()).isTrue();
    }

    private static void assertFileDto(FileDto fileDto) {
        assertThat(fileDto.getName()).isEqualTo("logo.png");
        assertThat(fileDto.getContent()).containsExactly(1, 2, 3);
    }
}
