package com.meitalk.auth.config;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @SneakyThrows
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        //        get jwt token for header
        String token = jwtTokenProvider.resolveToken((HttpServletRequest) request);
        //        token validation check
        if (token != null && jwtTokenProvider.validateToken(jwtTokenProvider.tokenBearer(token), request)) {
            //            if token validation success, get user info
            Authentication authentication = jwtTokenProvider.getAuthentication(jwtTokenProvider.tokenBearer(token));
            log.info("authentication : {}", authentication);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        filterChain.doFilter(request, response);
    }
}
