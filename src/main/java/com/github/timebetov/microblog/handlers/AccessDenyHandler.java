package com.github.timebetov.microblog.handlers;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;

public class AccessDenyHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException ex) throws IOException, ServletException {

        String message = (ex != null && ex.getMessage() != null)
                ? ex.getMessage()
                : HttpStatus.FORBIDDEN.getReasonPhrase();

        response.setHeader("Microblog denied-reason", "Authorization failed");
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType("application/json;charset=UTF-8");

        // Should construct JSON response
        String responseMsg = "{\"error\":\"" + message + "\"}";

        response.getWriter().write(responseMsg);
    }
}
