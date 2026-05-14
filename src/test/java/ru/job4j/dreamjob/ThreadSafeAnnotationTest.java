package ru.job4j.dreamjob;

import net.jcip.annotations.ThreadSafe;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import ru.job4j.dreamjob.controller.CandidateController;
import ru.job4j.dreamjob.controller.IndexController;
import ru.job4j.dreamjob.controller.VacancyController;
import ru.job4j.dreamjob.repository.MemoryCandidateRepository;
import ru.job4j.dreamjob.repository.MemoryCityRepository;
import ru.job4j.dreamjob.repository.MemoryVacancyRepository;
import ru.job4j.dreamjob.service.SimpleCandidateService;
import ru.job4j.dreamjob.service.SimpleCityService;
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
                IndexController.class,
                SimpleCandidateService.class,
                SimpleCityService.class,
                SimpleVacancyService.class,
                MemoryCandidateRepository.class,
                MemoryCityRepository.class,
                MemoryVacancyRepository.class
        );
    }
}
