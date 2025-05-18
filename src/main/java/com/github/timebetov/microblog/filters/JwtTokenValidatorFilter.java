package com.github.timebetov.microblog.filters;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.timebetov.microblog.configs.AppConstants;
import com.github.timebetov.microblog.dtos.ErrorResponseDTO;
import com.github.timebetov.microblog.utils.JwtUtils;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtTokenValidatorFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        try {
            String jwt = request.getHeader(AppConstants.JWT_HEADER);
            if (null != jwt) {

                String username = jwtUtils.extractUsername(jwt);
                String roles = jwtUtils.extractRole(jwt);

                Authentication auth = new UsernamePasswordAuthenticationToken(username, null,
                        AuthorityUtils.commaSeparatedStringToAuthorityList(roles));

                SecurityContextHolder.getContext().setAuthentication(auth);
            }

            filterChain.doFilter(request, response);
        } catch (JwtException | IllegalArgumentException e) {
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
        return List.of("/auth/login", "/auth/authenticate").contains(request.getServletPath());
    }
}
