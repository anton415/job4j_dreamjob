package ru.job4j.dreamjob.filter;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;

import ru.job4j.dreamjob.controller.SessionUser;
import ru.job4j.dreamjob.model.User;

import static org.assertj.core.api.Assertions.assertThat;

class SessionFilterTest {
    private final SessionFilter filter = new SessionFilter();

    @Test
    void whenSessionHasUserThenRequestHasSameUser() throws Exception {
        var user = new User(1, "mail@test.com", "Anton", "password");
        var session = new MockHttpSession();
        session.setAttribute(SessionUser.USER_ATTRIBUTE, user);
        var request = new MockHttpServletRequest("GET", "/");
        request.setSession(session);
        var response = new MockHttpServletResponse();
        var chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(request.getAttribute(SessionUser.USER_ATTRIBUTE)).isEqualTo(user);
        assertThat(chain.getRequest()).isSameAs(request);
    }

    @Test
    void whenSessionHasNoUserThenRequestHasGuest() throws Exception {
        var request = new MockHttpServletRequest("GET", "/");
        var response = new MockHttpServletResponse();
        var chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(request.getAttribute(SessionUser.USER_ATTRIBUTE))
                .hasFieldOrPropertyWithValue("id", 0)
                .hasFieldOrPropertyWithValue("name", "Гость");
        assertThat(chain.getRequest()).isSameAs(request);
    }
}
