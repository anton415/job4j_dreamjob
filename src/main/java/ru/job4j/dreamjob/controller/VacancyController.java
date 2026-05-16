package ru.job4j.dreamjob.controller;

import java.io.IOException;

import jakarta.servlet.http.HttpSession;
import net.jcip.annotations.ThreadSafe;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import ru.job4j.dreamjob.dto.FileDto;
import ru.job4j.dreamjob.model.Vacancy;
import ru.job4j.dreamjob.service.CityService;
import ru.job4j.dreamjob.service.VacancyService;

@ThreadSafe
@Controller
@RequestMapping("/vacancies")
public class VacancyController {
    private final VacancyService vacancyService;
    private final CityService cityService;

    public VacancyController(VacancyService vacancyService, CityService cityService) {
        this.vacancyService = vacancyService;
        this.cityService = cityService;
    }

    @GetMapping
    public String getAll(Model model, HttpSession session) {
        SessionUser.addToModel(model, session);
        model.addAttribute("vacancies", vacancyService.findAll());
        return "vacancies/list";
    }

    @GetMapping("/create")
    public String getCreationPage(Model model, HttpSession session) {
        SessionUser.addToModel(model, session);
        model.addAttribute("cities", cityService.findAll());
        return "vacancies/create";
    }

    @PostMapping("/create")
    public String create(@ModelAttribute Vacancy vacancy,
                         @RequestParam(name = "file", required = false) MultipartFile file)
            throws IOException {
        vacancyService.save(vacancy, toFileDto(file));
        return "redirect:/vacancies";
    }

    @GetMapping("/{id}")
    public String getById(Model model, @PathVariable int id, HttpSession session) {
        SessionUser.addToModel(model, session);
        var vacancyOptional = vacancyService.findById(id);
        if (vacancyOptional.isEmpty()) {
            model.addAttribute("message", "Вакансия с указанным идентификатором не найдена");
            return "errors/404";
        }
        model.addAttribute("vacancy", vacancyOptional.get());
        model.addAttribute("cities", cityService.findAll());
        return "vacancies/one";
    }

    @PostMapping("/update")
    public String update(@ModelAttribute Vacancy vacancy,
                         @RequestParam(name = "file", required = false) MultipartFile file,
                         Model model,
                         HttpSession session)
            throws IOException {
        var isUpdated = vacancyService.update(vacancy, toFileDto(file));
        if (!isUpdated) {
            SessionUser.addToModel(model, session);
            model.addAttribute("message", "Вакансия с указанным идентификатором не найдена");
            return "errors/404";
        }
        return "redirect:/vacancies";
    }

    @GetMapping("/delete/{id}")
    public String delete(Model model, @PathVariable int id, HttpSession session) {
        var isDeleted = vacancyService.deleteById(id);
        if (!isDeleted) {
            SessionUser.addToModel(model, session);
            model.addAttribute("message", "Вакансия с указанным идентификатором не найдена");
            return "errors/404";
        }
        return "redirect:/vacancies";
    }

    private FileDto toFileDto(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }
        return new FileDto(file.getOriginalFilename(), file.getBytes());
    }
}
