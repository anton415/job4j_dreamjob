package ru.job4j.dreamjob.controller;

import jakarta.servlet.http.HttpSession;
import ru.job4j.dreamjob.model.User;

public final class SessionUser {
    public static final String USER_ATTRIBUTE = "user";
    private static final String GUEST_NAME = "Гость";

    private SessionUser() {
    }

    public static User get(HttpSession session) {
        var sessionUser = session == null ? null : session.getAttribute(USER_ATTRIBUTE);
        if (sessionUser instanceof User user) {
            return user;
        }
        return new User(0, "", GUEST_NAME, "");
    }

    public static boolean isLoggedIn(HttpSession session) {
        return session != null && session.getAttribute(USER_ATTRIBUTE) instanceof User;
    }
}
