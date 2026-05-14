package ru.job4j.dreamjob.repository;

import org.junit.jupiter.api.Test;

import ru.job4j.dreamjob.model.Vacancy;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class MemoryVacancyRepositoryTest {
    private static final int THREADS_COUNT = 8;

    @Test
    void whenSaveVacanciesConcurrentlyThenIdsAreUnique() throws Exception {
        var repository = new MemoryVacancyRepository();
        var vacancies = saveVacancies(repository, 100);

        assertThat(vacancies)
                .extracting(Vacancy::getId)
                .doesNotHaveDuplicates();
        assertThat(repository.findAll()).hasSize(107);
    }

    @Test
    void whenUpdateOrDeleteUnknownVacancyThenFalse() {
        var repository = new MemoryVacancyRepository();
        var unknownVacancy = new Vacancy(100, "Unknown", "None", LocalDateTime.now());

        assertThat(repository.update(unknownVacancy)).isFalse();
        assertThat(repository.deleteById(unknownVacancy.getId())).isFalse();
        assertThat(repository.findById(unknownVacancy.getId())).isEmpty();
    }

    @Test
    void whenSavedVacancyChangedOutsideThenRepositoryKeepsSnapshot() {
        var repository = new MemoryVacancyRepository();
        var vacancy = repository.save(vacancy(0));

        vacancy.setTitle("Changed");
        vacancy.setVisible(true);
        vacancy.setCityId(3);

        assertThat(repository.findById(vacancy.getId()))
                .get()
                .extracting(Vacancy::getTitle)
                .isEqualTo("Vacancy 0");
        assertThat(repository.findById(vacancy.getId()))
                .get()
                .extracting(Vacancy::getVisible)
                .isEqualTo(false);
        assertThat(repository.findById(vacancy.getId()))
                .get()
                .extracting(Vacancy::getCityId)
                .isEqualTo(0);
    }

    @Test
    void whenUpdateVacancyThenVisibleAndCityIdAreUpdated() {
        var repository = new MemoryVacancyRepository();
        var vacancy = repository.save(vacancy(0));
        var updatedVacancy = new Vacancy(
                vacancy.getId(),
                "Updated",
                "Updated description",
                LocalDateTime.now(),
                true,
                2);

        assertThat(repository.update(updatedVacancy)).isTrue();
        assertThat(repository.findById(vacancy.getId()))
                .get()
                .extracting(Vacancy::getVisible)
                .isEqualTo(true);
        assertThat(repository.findById(vacancy.getId()))
                .get()
                .extracting(Vacancy::getCityId)
                .isEqualTo(2);
    }

    private static List<Vacancy> saveVacancies(MemoryVacancyRepository repository, int count)
            throws InterruptedException, ExecutionException {
        var executor = Executors.newFixedThreadPool(THREADS_COUNT);
        var start = new CountDownLatch(1);
        try {
            var futures = IntStream.range(0, count)
                    .mapToObj(index -> executor.submit(() -> {
                        start.await();
                        return repository.save(vacancy(index));
                    }))
                    .toList();
            start.countDown();
            return collectVacancies(futures);
        } finally {
            executor.shutdownNow();
        }
    }

    private static List<Vacancy> collectVacancies(List<java.util.concurrent.Future<Vacancy>> futures)
            throws InterruptedException, ExecutionException {
        var vacancies = new ArrayList<Vacancy>();
        for (var future : futures) {
            vacancies.add(future.get());
        }
        return vacancies;
    }

    private static Vacancy vacancy(int index) {
        return new Vacancy(
                0,
                "Vacancy " + index,
                "Description " + index,
                LocalDateTime.of(2026, 1, 1, 0, 0));
    }
}
