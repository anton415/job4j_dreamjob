package ru.job4j.dreamjob.controller;

import jakarta.servlet.http.HttpSession;
import net.jcip.annotations.ThreadSafe;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@ThreadSafe
@Controller
public class IndexController {
    @GetMapping({"/", "/index"})
    public String getIndex(Model model, HttpSession session) {
        SessionUser.addToModel(model, session);
        return "index";
    }
}
