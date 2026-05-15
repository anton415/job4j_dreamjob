package ru.job4j.dreamjob.service;

import java.util.Collection;

import net.jcip.annotations.ThreadSafe;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import ru.job4j.dreamjob.model.City;
import ru.job4j.dreamjob.repository.CityRepository;

@ThreadSafe
@Service
public class SimpleCityService implements CityService {
    private final CityRepository cityRepository;

    public SimpleCityService(@Qualifier("sql2oCityRepository") CityRepository sql2oCityRepository) {
        this.cityRepository = sql2oCityRepository;
    }

    @Override
    public Collection<City> findAll() {
        return cityRepository.findAll();
    }
}
