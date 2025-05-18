package com.github.timebetov.microblog.filters;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.timebetov.microblog.configs.AppConstants;
import com.github.timebetov.microblog.dtos.ErrorResponseDTO;
import com.github.timebetov.microblog.models.User;
import com.github.timebetov.microblog.utils.JwtUtils;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtTokenGeneratorFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();

            if (null != auth) {

                String token = jwtUtils.generateJwtToken((User) auth.getPrincipal());

                response.setHeader(AppConstants.JWT_HEADER, token);
            }
            filterChain.doFilter(request, response);
        } catch (Exception e) {

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

            ErrorResponseDTO errorDto = ErrorResponseDTO.builder()
                    .apiPath(request.getRequestURI())
                    .errorCode(HttpStatus.UNAUTHORIZED)
                    .errorTime(LocalDateTime.now())
                    .errorMessage(e.getMessage())
                    .build();

            objectMapper.writeValue(response.getWriter(), errorDto);
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return !request.getServletPath().equals("/auth/authenticate");
    }
}
