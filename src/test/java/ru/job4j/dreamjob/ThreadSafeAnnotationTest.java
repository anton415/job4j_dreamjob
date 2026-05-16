package ru.job4j.dreamjob;

import net.jcip.annotations.ThreadSafe;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import ru.job4j.dreamjob.controller.CandidateController;
import ru.job4j.dreamjob.controller.IndexController;
import ru.job4j.dreamjob.controller.UserController;
import ru.job4j.dreamjob.controller.VacancyController;
import ru.job4j.dreamjob.filter.AuthorizationFilter;
import ru.job4j.dreamjob.filter.SessionFilter;
import ru.job4j.dreamjob.repository.MemoryCandidateRepository;
import ru.job4j.dreamjob.repository.MemoryCityRepository;
import ru.job4j.dreamjob.repository.MemoryVacancyRepository;
import ru.job4j.dreamjob.repository.Sql2oCityRepository;
import ru.job4j.dreamjob.repository.Sql2oCandidateRepository;
import ru.job4j.dreamjob.repository.Sql2oFileRepository;
import ru.job4j.dreamjob.repository.Sql2oUserRepository;
import ru.job4j.dreamjob.repository.Sql2oVacancyRepository;
import ru.job4j.dreamjob.service.SimpleCandidateService;
import ru.job4j.dreamjob.service.SimpleCityService;
import ru.job4j.dreamjob.service.SimpleUserService;
import ru.job4j.dreamjob.service.SimpleVacancyService;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class ThreadSafeAnnotationTest {

    @ParameterizedTest
    @MethodSource("threadSafeClasses")
    void whenClassIsImplementationThenItShouldBeAnnotated(Class<?> implementation) {
        assertThat(implementation).hasAnnotation(ThreadSafe.class);
    }

    private static Stream<Class<?>> threadSafeClasses() {
        return Stream.of(
                Main.class,
                CandidateController.class,
                VacancyController.class,
                UserController.class,
                IndexController.class,
                AuthorizationFilter.class,
                SessionFilter.class,
                SimpleCandidateService.class,
                SimpleCityService.class,
                SimpleVacancyService.class,
                SimpleUserService.class,
                MemoryCandidateRepository.class,
                MemoryCityRepository.class,
                MemoryVacancyRepository.class,
                Sql2oCityRepository.class,
                Sql2oCandidateRepository.class,
                Sql2oFileRepository.class,
                Sql2oVacancyRepository.class,
                Sql2oUserRepository.class
        );
    }
}
