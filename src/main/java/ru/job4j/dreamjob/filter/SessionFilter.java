package ru.job4j.dreamjob.filter;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import net.jcip.annotations.ThreadSafe;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import ru.job4j.dreamjob.controller.SessionUser;

@ThreadSafe
@Component
@Order(2)
public class SessionFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        var httpRequest = (HttpServletRequest) request;
        httpRequest.setAttribute(SessionUser.USER_ATTRIBUTE, SessionUser.get(httpRequest.getSession(false)));
        chain.doFilter(request, response);
    }
}
