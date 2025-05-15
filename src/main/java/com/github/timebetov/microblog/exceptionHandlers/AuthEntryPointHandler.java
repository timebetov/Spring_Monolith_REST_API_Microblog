package com.github.timebetov.microblog.exceptionHandlers;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;

public class AuthEntryPointHandler implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException, ServletException {

        String message = (authException != null && authException.getMessage() != null)
                ? authException.getMessage()
                : HttpStatus.UNAUTHORIZED.getReasonPhrase();

        response.setHeader("Microblog error-reason", "Authentication failed");
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json;charset=UTF-8");

        // Should construct JSON response
        String responseMsg = "{\"error\":\"" + message + "\"}";

        response.getWriter().write(responseMsg);
    }
}
