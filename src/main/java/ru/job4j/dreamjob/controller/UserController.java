package ru.job4j.dreamjob.controller;

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
    public String getLoginPage() {
        return "users/login";
    }

    @PostMapping("/login")
    public String loginUser(@ModelAttribute User user, Model model) {
        var userOptional = userService.findByEmailAndPassword(user.getEmail(), user.getPassword());
        if (userOptional.isEmpty()) {
            model.addAttribute("message", "Почта или пароль введены неверно");
            return "users/login";
        }
        return "redirect:/";
    }

    @GetMapping("/users/register")
    public String getRegistrationPage() {
        return "users/register";
    }

    @PostMapping("/users/register")
    public String register(@ModelAttribute User user, Model model) {
        var savedUser = userService.save(user);
        if (savedUser.isEmpty()) {
            model.addAttribute("message", "Пользователь с такой почтой уже существует");
            return "users/register";
        }
        return "redirect:/";
    }
}
