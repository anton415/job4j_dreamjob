package ru.job4j.dreamjob.controller;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.mock.web.MockHttpSession;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import ru.job4j.dreamjob.filter.SessionFilter;
import ru.job4j.dreamjob.model.User;
import ru.job4j.dreamjob.service.UserService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {
    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new UserController(userService))
                .addFilters(new SessionFilter())
                .build();
    }

    @Test
    void whenGetLoginPageThenReturnsLoginView() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(request().attribute("user", hasProperty("name", is("Гость"))))
                .andExpect(view().name("users/login"));
    }

    @Test
    void whenLoginWithKnownUserThenRedirectsToIndex() throws Exception {
        var user = new User(1, "mail@test.com", "Anton", "password");
        when(userService.findByEmailAndPassword("mail@test.com", "password"))
                .thenReturn(Optional.of(user));

        mockMvc.perform(post("/login")
                        .param("email", "mail@test.com")
                        .param("password", "password"))
                .andExpect(status().is3xxRedirection())
                .andExpect(request().sessionAttribute(SessionUser.USER_ATTRIBUTE, user))
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
                .andExpect(request().attribute("user", hasProperty("name", is("Гость"))))
                .andExpect(model().attribute("message", "Почта или пароль введены неверно"));

        verify(userService).findByEmailAndPassword("mail@test.com", "wrong");
    }

    @Test
    void whenLogoutThenSessionIsInvalidated() throws Exception {
        var session = new MockHttpSession();
        session.setAttribute(SessionUser.USER_ATTRIBUTE, new User(1, "mail@test.com", "Anton", "password"));

        mockMvc.perform(get("/logout").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        assertThat(session.isInvalid()).isTrue();
    }

    @Test
    void whenGetRegistrationPageThenReturnsRegisterView() throws Exception {
        mockMvc.perform(get("/users/register"))
                .andExpect(status().isOk())
                .andExpect(request().attribute("user", hasProperty("name", is("Гость"))))
                .andExpect(view().name("users/register"));
    }

    @Test
    void whenRegisterNewUserThenRedirectsToIndex() throws Exception {
        var savedUser = new User(1, "mail@test.com", "Anton", "password");
        when(userService.save(any(User.class))).thenReturn(Optional.of(savedUser));

        mockMvc.perform(post("/users/register")
                        .param("email", "mail@test.com")
                        .param("name", "Anton")
                        .param("password", "password"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        var userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userService).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getEmail()).isEqualTo("mail@test.com");
        assertThat(userCaptor.getValue().getName()).isEqualTo("Anton");
        assertThat(userCaptor.getValue().getPassword()).isEqualTo("password");
    }

    @Test
    void whenRegisterExistingUserThenReturnsRegisterViewWithMessage() throws Exception {
        when(userService.save(any(User.class))).thenReturn(Optional.empty());

        mockMvc.perform(post("/users/register")
                        .param("email", "mail@test.com")
                        .param("name", "Anton")
                        .param("password", "password"))
                .andExpect(status().isOk())
                .andExpect(view().name("users/register"))
                .andExpect(request().attribute("user", hasProperty("name", is("Гость"))))
                .andExpect(model().attribute("message", "Пользователь с такой почтой уже существует"));

        verify(userService).save(any(User.class));
    }
}
