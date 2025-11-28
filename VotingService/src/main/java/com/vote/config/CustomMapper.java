package com.vote.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CustomMapper {
	@Bean
	ModelMapper mapper() {
		ModelMapper mapper = new ModelMapper();
	    mapper.getConfiguration().setSkipNullEnabled(true); // Skip null fields during mapping
	    return mapper;
	}
}