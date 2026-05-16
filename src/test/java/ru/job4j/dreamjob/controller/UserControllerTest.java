package ru.job4j.dreamjob.controller;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import ru.job4j.dreamjob.model.User;
import ru.job4j.dreamjob.service.UserService;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {
    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new UserController(userService)).build();
    }

    @Test
    void whenGetLoginPageThenReturnsLoginView() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("users/login"));
    }

    @Test
    void whenLoginWithKnownUserThenRedirectsToIndex() throws Exception {
        when(userService.findByEmailAndPassword("mail@test.com", "password"))
                .thenReturn(Optional.of(new User(1, "mail@test.com", "Anton", "password")));

        mockMvc.perform(post("/login")
                        .param("email", "mail@test.com")
                        .param("password", "password"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        verify(userService).findByEmailAndPassword("mail@test.com", "password");
    }

    @Test
    void whenLoginWithUnknownUserThenReturnsLoginViewWithMessage() throws Exception {
        when(userService.findByEmailAndPassword("mail@test.com", "wrong"))
                .thenReturn(Optional.empty());

        mockMvc.perform(post("/login")
                        .param("email", "mail@test.com")
                        .param("password", "wrong"))
                .andExpect(status().isOk())
                .andExpect(view().name("users/login"))
                .andExpect(model().attribute("message", "Почта или пароль введены неверно"));

        verify(userService).findByEmailAndPassword("mail@test.com", "wrong");
    }
}
