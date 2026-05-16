package ru.job4j.dreamjob.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.ui.Model;
import ru.job4j.dreamjob.model.User;

final class SessionUser {
    static final String USER_ATTRIBUTE = "user";

    private SessionUser() {
    }

    static void addToModel(Model model, HttpSession session) {
        model.addAttribute(USER_ATTRIBUTE, get(session));
    }

    private static User get(HttpSession session) {
        var sessionUser = session == null ? null : session.getAttribute(USER_ATTRIBUTE);
        if (sessionUser instanceof User user) {
            return user;
        }
        return new User(0, "", "Гость", "");
    }
}
