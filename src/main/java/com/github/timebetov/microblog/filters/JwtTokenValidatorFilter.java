package com.github.timebetov.microblog.filters;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.timebetov.microblog.configs.AppConstants;
import com.github.timebetov.microblog.dtos.ErrorResponseDTO;
import com.github.timebetov.microblog.services.impl.TokenBlacklistService;
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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
    private final UserDetailsService userDetailsService;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        try {
            String token = request.getHeader(AppConstants.JWT_HEADER);

            if (null == token) {
                throw new BadRequestException("Missing JWT header");
            }

            if (!token.startsWith("Bearer ")) {
                throw new BadRequestException("Invalid Authorization Token");
            }

            String jwt = token.substring("Bearer ".length());

            if (tokenBlacklistService.isBlacklisted(jwt)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            String username = jwtUtils.extractUsername(jwt);

            if (username == null || SecurityContextHolder.getContext().getAuthentication() != null) {
                throw new BadRequestException("Invalid JWT");
            }

            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if (!jwtUtils.isTokenValid(jwt, userDetails)) {
                throw new BadRequestException("Token is not valid anymore");
            }

            Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(auth);

        } catch (JwtException | IllegalArgumentException | BadRequestException | UsernameNotFoundException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

            ErrorResponseDTO errorDto = ErrorResponseDTO.builder()
                    .apiPath(request.getRequestURI())
                    .errorCode(HttpStatus.UNAUTHORIZED)
                    .errorTime(LocalDateTime.now())
                    .errorMessage(e.getMessage())
                    .build();

            objectMapper.writeValue(response.getWriter(), errorDto);
            return;
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getServletPath();

        return path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.equals("/auth/login")
                || path.equals("/auth/authenticate")
                || path.equals("/auth/register");
    }
}
