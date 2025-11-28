package com.vote.filter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.vote.feign.TokenStoreClient;
import com.vote.services.JwtSecretService;
import com.vote.util.JwtUtil;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
	private final JwtUtil jwtUtil;
	private final TokenStoreClient tokenStoreClient;
	private final JwtSecretService jwtSecretService; // just for checking
	
	private Logger logger = LoggerFactory.getLogger(JwtFilter.class);
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		
		logger.info("Vote {}", jwtSecretService.getCurrentSecret());
		logger.info("Vote {}", jwtSecretService.getPreviousSecret());
		
		logger.info("Trying to validate the token: {}", request.getHeader("Authorization"));
		try {
            String authHeader = request.getHeader("Authorization");
            String token = null;
            String username = null;
            
//            logger.info("Authorization Header: {}", request.getHeader("Authorization"));
//            logger.info("Token from query parameter: {}", request.getParameter("token"));


            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7).trim();
                username = jwtUtil.extractUsername(token);
            } else if(request.getParameter("token") != null && token == null) {
            	token = request.getParameter("token"); // get token from parameter also, for SSE connection
            	username = jwtUtil.extractUsername(token);
            }
            
            
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                
//                logger.info("Is token validated by JWTUtil: {}", jwtUtil.validateToken(token, username));
//                logger.info("Is token validated by Tokenstore: {}", (tokenStoreClient.validateToken(token)));
            	
                if (jwtUtil.validateToken(token, username)) {
                	Claims claims = jwtUtil.extractAllClaims(token);
                	if(claims == null) {
                		throw new JwtException("Token verification failed: Unable to extract claims.");
                	}
                	String tokenType = claims.get("tokenType", String.class);
                	
//                	logger.info("Username: {}  -- got in Voting Service", username);
                	if("ACCESS".equals(tokenType) && !tokenStoreClient.validateToken(token)) {
//                		logger.warn("Token rejected: Exceeded session limit or logged out.");
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.getWriter().write("Session invalid. Please log in again.");
                	} else if ("REFRESH".equals(tokenType)) {
                        // Allow refresh token ONLY for the refresh end point
                        if (!request.getRequestURI().equals("/refresh/createAccessToken")) {
//                            logger.warn("Refresh token cannot be used for authentication outside refresh endpoint");
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.getWriter().write("Refresh tokens cannot be used to access protected resources.");
                            return;
                        }
                    } else {
                    	String role = claims.get("role", String.class);
                    	List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(role));
                    	
//                    	logger.info("Claims: {}", claims);
//                    	logger.info("roles: {}", role);
                    	
                    	UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    			username,
                    			null,
                    			authorities
                    			);
                    	authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    	SecurityContextHolder.getContext().setAuthentication(authToken);
                    	
                    	logger.info("SecurityContext Authentication: {}", SecurityContextHolder.getContext().getAuthentication());
                    }
                } else {
                	response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("Session expired or invalid token");
                    return;
                }
            }
//            logger.info("successfully validated the token");
        } catch (ExpiredJwtException e) {
            logger.error("Token expired: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Token has expired. Please log in again.");
            return;
        } catch (JwtException e) {  // Generic exception for any JWT-related error
            logger.error("Invalid token: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid token. Please log in again.");
            return;
        } catch (Exception e) {
            logger.error("Token validation error: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("An error occurred while processing authentication.");
            return;
        }
		
		filterChain.doFilter(request, response);
	}
}