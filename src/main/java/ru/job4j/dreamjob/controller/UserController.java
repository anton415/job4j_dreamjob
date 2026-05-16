package ru.job4j.dreamjob.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import net.jcip.annotations.ThreadSafe;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import ru.job4j.dreamjob.model.User;
import ru.job4j.dreamjob.service.UserService;

@ThreadSafe
@Controller
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String getLoginPage(Model model, HttpSession session) {
        SessionUser.addToModel(model, session);
        return "users/login";
    }

    @PostMapping("/login")
    public String loginUser(@ModelAttribute User user, Model model, HttpServletRequest request) {
        var userOptional = userService.findByEmailAndPassword(user.getEmail(), user.getPassword());
        if (userOptional.isEmpty()) {
            SessionUser.addToModel(model, request.getSession(false));
            model.addAttribute("message", "Почта или пароль введены неверно");
            return "users/login";
        }
        request.getSession().setAttribute(SessionUser.USER_ATTRIBUTE, userOptional.get());
        return "redirect:/";
    }

    @GetMapping("/users/register")
    public String getRegistrationPage(Model model, HttpSession session) {
        SessionUser.addToModel(model, session);
        return "users/register";
    }

    @PostMapping("/users/register")
    public String register(@ModelAttribute User user, Model model, HttpSession session) {
        var savedUser = userService.save(user);
        if (savedUser.isEmpty()) {
            SessionUser.addToModel(model, session);
            model.addAttribute("message", "Пользователь с такой почтой уже существует");
            return "users/register";
        }
        return "redirect:/";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}
