package ru.job4j.dreamjob.filter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;

import ru.job4j.dreamjob.controller.SessionUser;
import ru.job4j.dreamjob.model.User;

import static org.assertj.core.api.Assertions.assertThat;

class AuthorizationFilterTest {
    private final AuthorizationFilter filter = new AuthorizationFilter();

    @ParameterizedTest
    @ValueSource(strings = {
            "/",
            "/index",
            "/login",
            "/users/register",
            "/error",
            "/favicon.ico",
            "/css/bootstrap.min.css",
            "/js/bootstrap.bundle.min.js"
    })
    void whenUriIsAlwaysPermittedThenReturnsTrue(String uri) {
        assertThat(filter.isAlwaysPermitted(uri)).isTrue();
    }

    @Test
    void whenRequestToProtectedUriWithoutUserThenRedirectsToLogin() throws Exception {
        var request = new MockHttpServletRequest("GET", "/vacancies");
        var response = new MockHttpServletResponse();
        var chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(response.getRedirectedUrl()).isEqualTo("/login");
        assertThat(chain.getRequest()).isNull();
    }

    @Test
    void whenRequestToProtectedUriWithUserThenPassesRequest() throws Exception {
        var user = new User(1, "mail@test.com", "Anton", "password");
        var session = new MockHttpSession();
        session.setAttribute(SessionUser.USER_ATTRIBUTE, user);
        var request = new MockHttpServletRequest("GET", "/vacancies");
        request.setSession(session);
        var response = new MockHttpServletResponse();
        var chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(response.getRedirectedUrl()).isNull();
        assertThat(chain.getRequest()).isSameAs(request);
    }

    @Test
    void whenRequestToPermittedUriWithoutUserThenPassesRequest() throws Exception {
        var request = new MockHttpServletRequest("GET", "/login");
        var response = new MockHttpServletResponse();
        var chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(response.getRedirectedUrl()).isNull();
        assertThat(chain.getRequest()).isSameAs(request);
    }
}
