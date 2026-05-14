package ru.job4j.dreamjob.repository;

import java.util.Collection;
import java.util.List;

import net.jcip.annotations.ThreadSafe;

import org.springframework.stereotype.Repository;

import ru.job4j.dreamjob.model.City;

@ThreadSafe
@Repository
public class MemoryCityRepository implements CityRepository {
    private final Collection<City> cities;

    public MemoryCityRepository() {
        cities = List.of(
                new City(1, "Москва"),
                new City(2, "Магадан"),
                new City(3, "Дедовск")
        );
    }

    @Override
    public Collection<City> findAll() {
        return cities.stream()
                .map(this::copy)
                .toList();
    }

    private City copy(City city) {
        return new City(city.getId(), city.getName());
    }
}
