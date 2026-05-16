package ru.job4j.dreamjob.filter;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.jcip.annotations.ThreadSafe;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import ru.job4j.dreamjob.controller.SessionUser;

@ThreadSafe
@Component
@Order(1)
public class AuthorizationFilter implements Filter {
    private static final Set<String> ALWAYS_PERMITTED_URIS = Set.of(
            "/",
            "/index",
            "/login",
            "/users/register",
            "/error",
            "/favicon.ico"
    );
    private static final List<String> ALWAYS_PERMITTED_PREFIXES = List.of(
            "/css/",
            "/js/",
            "/images/",
            "/webjars/"
    );

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        doFilter((HttpServletRequest) request, (HttpServletResponse) response, chain);
    }

    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (isAlwaysPermitted(getUri(request)) || SessionUser.isLoggedIn(request.getSession(false))) {
            chain.doFilter(request, response);
            return;
        }
        response.sendRedirect(request.getContextPath() + "/login");
    }

    public boolean isAlwaysPermitted(String uri) {
        return ALWAYS_PERMITTED_URIS.contains(uri)
                || ALWAYS_PERMITTED_PREFIXES.stream().anyMatch(uri::startsWith);
    }

    private String getUri(HttpServletRequest request) {
        var contextPath = request.getContextPath();
        var requestUri = request.getRequestURI();
        var uri = requestUri.substring(contextPath.length());
        return uri.isEmpty() ? "/" : uri;
    }
}
