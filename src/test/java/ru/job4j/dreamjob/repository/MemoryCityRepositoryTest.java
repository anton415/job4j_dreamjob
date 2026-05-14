package ru.job4j.dreamjob.repository;

import org.junit.jupiter.api.Test;

import ru.job4j.dreamjob.model.City;

import static org.assertj.core.api.Assertions.assertThat;

class MemoryCityRepositoryTest {

    @Test
    void whenFindAllThenReturnDefaultCities() {
        var repository = new MemoryCityRepository();

        assertThat(repository.findAll())
                .containsExactly(
                        new City(1, "Москва"),
                        new City(2, "Магадан"),
                        new City(3, "Дедовск")
                );
    }

    @Test
    void whenReturnedCityChangedOutsideThenRepositoryKeepsSnapshot() {
        var repository = new MemoryCityRepository();
        var city = repository.findAll().iterator().next();

        city.setName("Changed");

        assertThat(repository.findAll())
                .extracting(City::getName)
                .containsExactly("Москва", "Магадан", "Дедовск");
    }
}
