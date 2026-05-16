package ru.job4j.dreamjob.service;

import java.util.Optional;

import net.jcip.annotations.ThreadSafe;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import ru.job4j.dreamjob.model.User;
import ru.job4j.dreamjob.repository.UserRepository;

@ThreadSafe
@Service
public class SimpleUserService implements UserService {
    private final UserRepository userRepository;

    public SimpleUserService(@Qualifier("sql2oUserRepository") UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Optional<User> save(User user) {
        return userRepository.save(user);
    }

    @Override
    public Optional<User> findByEmailAndPassword(String email, String password) {
        return userRepository.findByEmailAndPassword(email, password);
    }
}
