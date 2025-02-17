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
		
		logger.info("Trying to validate the token: {}", request.getHeader("Authorization"));
		try {
            String authHeader = request.getHeader("Authorization");
            String token = null;
            String username = null;

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7).trim();
                username = jwtUtil.extractUsername(token);
            }

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                
                logger.info("Extracted Role from UserDetails: {}", userDetails.getAuthorities());

                if (jwtUtil.validateToken(token, userDetails.getUsername()) && tokenStore.isTokenValid(username, token)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    
                    logger.info("SecurityContext Authentication: {}", SecurityContextHolder.getContext().getAuthentication());
                } else {
                	response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid session. Please login again.");
                    return;
                }
            }
            logger.info("successfully validated the token");
        } catch (Exception e){
            logger.error("Auth error:", e);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication error");
            return;
        }
        
		try {
			filterChain.doFilter(request, response);
			logger.info("filter chain passed");
		} catch (Exception e) {
			logger.error("FilterChain error:", e);
		}
		
	}
	
}
