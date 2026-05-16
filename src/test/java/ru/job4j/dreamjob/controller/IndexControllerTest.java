package ru.job4j.dreamjob.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceView;

import ru.job4j.dreamjob.model.User;

import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

class IndexControllerTest {
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new IndexController())
                .setSingleView(new InternalResourceView("/WEB-INF/views/index.html"))
                .build();
    }

    @Test
    void whenSessionHasUserThenIndexModelHasSameUser() throws Exception {
        var user = new User(1, "mail@test.com", "Anton", "password");
        var session = new MockHttpSession();
        session.setAttribute(SessionUser.USER_ATTRIBUTE, user);

        mockMvc.perform(get("/").session(session))
                .andExpect(status().isOk())
                .andExpect(model().attribute(SessionUser.USER_ATTRIBUTE, user))
                .andExpect(view().name("index"));
    }

    @Test
    void whenSessionHasNoUserThenIndexModelHasGuest() throws Exception {
        mockMvc.perform(get("/index"))
                .andExpect(status().isOk())
                .andExpect(model().attribute(SessionUser.USER_ATTRIBUTE, hasProperty("id", is(0))))
                .andExpect(model().attribute(SessionUser.USER_ATTRIBUTE, hasProperty("name", is("Гость"))))
                .andExpect(view().name("index"));
    }
}
