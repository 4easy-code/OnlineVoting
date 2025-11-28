package com.vote.config;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import com.vote.filter.JwtFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
	private final JwtFilter jwtFilter;
	
    private Logger logger = LoggerFactory.getLogger(SecurityConfig.class);
	
	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		logger.info("In security Config file, filter chain");
		
		http.csrf(c -> c.disable())
			.cors(cors -> cors.configurationSource(request -> {
	            CorsConfiguration config = new CorsConfiguration();
	            config.setAllowedOrigins(List.of("http://localhost:3000"));
	            config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
	            config.setAllowedHeaders(List.of("*"));
	            config.setAllowCredentials(true);
	            return config;
	        }))
			.authorizeHttpRequests(
					request -> request.requestMatchers(
							"/swagger-ui.html", 
							"/v3/api-docs", 
							"/v3/api-docs/**", 
							"/swagger-ui/**",
							"/result/publishResult"
							)
					.permitAll()
	            .requestMatchers("/party/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_USER")
	            .requestMatchers("/vote/**", "/voter/**", "/result/**").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")
				.anyRequest().authenticated()
			)
			.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
		
		return http.build();
	}

}
