package com.auth.filter;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.auth.services.CustomUserDetailsService;
import com.auth.services.TokenStore;
import com.auth.util.JwtUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
	private final JwtUtil jwtUtil;
	private final CustomUserDetailsService userDetailsService;
	private final TokenStore tokenStore;
	
	private Logger logger = LoggerFactory.getLogger(JwtFilter.class);
	
	
	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
	    String path = request.getRequestURI();
	    return path.equals("/votingapi/auth/login")
	        || path.equals("/votingapi/auth/createUser")
	        || path.equals("/votingapi/auth/validateUser")
	        || path.startsWith("/votingapi/auth/createOtp");
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
		
		logger.info("trying to validate token: {}", request.getHeader("Authorization"));
		
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        String username = jwtUtil.extractUsername(token);

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            
            logger.info("Extracted Role from UserDetails: {}", userDetails.getAuthorities());

            if (jwtUtil.validateToken(token, userDetails.getUsername()) && tokenStore.isValidToken(username, token)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
                
                logger.info("SecurityContext Authentication: {}", SecurityContextHolder.getContext().getAuthentication());
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Session expired or invalid token");
                return;
            }
        }
        
        logger.info("Successfully validated the token !");

        try {
        	filterChain.doFilter(request, response);
        	logger.info("passsed filter chain");
        } catch (Exception e) {
			logger.info("Filter chain error");
		}
    }
	
}
