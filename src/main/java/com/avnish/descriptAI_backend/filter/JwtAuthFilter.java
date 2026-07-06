package com.avnish.descriptAI_backend.filter;

import com.avnish.descriptAI_backend.service.JwtService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import io.jsonwebtoken.security.SignatureException;

import java.io.IOException;


/**
 * @author Avnish
 * @date 07-06-2026
 */


@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException{
        final String authHeader = request.getHeader("Authorization");
        // skip when no bearer token or null
        if(authHeader == null || !authHeader.startsWith("Bearer ")){
            filterChain.doFilter(request, response);
            return;
        }
        log.debug("this is the full auth header string: " + authHeader);
        final String jwt = authHeader.substring(7);
    try {
        final String username = jwtService.extractUsername(jwt);


        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // if username is valid let compare it from db username
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if (jwtService.isTokenValid(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

                log.debug("this is authtoken : " + authToken);
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
    } catch (ExpiredJwtException | MalformedJwtException | SignatureException | UnsupportedJwtException e) {
            // Invalid/expired token -> just don't authenticate. Let Spring Security's
            // normal authorizeHttpRequests rules reject it as unauthenticated (401/403),
            // instead of letting the exception bubble up into a 500.
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

}
