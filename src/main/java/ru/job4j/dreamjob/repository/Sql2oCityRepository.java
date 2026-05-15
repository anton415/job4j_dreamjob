package ru.job4j.dreamjob.repository;

import java.util.Collection;

import net.jcip.annotations.ThreadSafe;

import org.sql2o.Sql2o;
import org.springframework.stereotype.Repository;

import ru.job4j.dreamjob.model.City;

@ThreadSafe
@Repository
public class Sql2oCityRepository implements CityRepository {
    private final Sql2o sql2o;

    public Sql2oCityRepository(Sql2o sql2o) {
        this.sql2o = sql2o;
    }

    @Override
    public Collection<City> findAll() {
        try (var connection = sql2o.open()) {
            return connection.createQuery("SELECT id, name FROM cities ORDER BY id")
                    .executeAndFetch(City.class);
        }
    }
}
