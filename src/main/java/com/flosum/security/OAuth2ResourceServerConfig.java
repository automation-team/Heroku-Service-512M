package com.flosum.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.error.OAuth2AccessDeniedHandler;

@Configuration
@EnableResourceServer
public class OAuth2ResourceServerConfig extends ResourceServerConfigurerAdapter {

	private static final String RESOURCE_ID = "SPRING_REST_API";

	@Override
	public void configure(ResourceServerSecurityConfigurer resources) {
		resources.resourceId(RESOURCE_ID).stateless(false);
	}

	@Override
	public void configure(HttpSecurity http) throws Exception {
		http.anonymous().disable().requestMatchers()
				.antMatchers("/operation/**")
				.antMatchers("/repo/**")
				.antMatchers("/branches/**")
				.antMatchers("/components/**")
				.antMatchers("/commits/**")
				.antMatchers("/history/**")
				.antMatchers("/data/**")
				.antMatchers("/package/**")
				.antMatchers("/info/**")
				.and()
				.authorizeRequests()
				.antMatchers("/status/**").access("hasRole('ADMIN')")
				.antMatchers("/repo/**").access("hasRole('ADMIN')")
				.antMatchers("/branches/**").access("hasRole('ADMIN')")
				.antMatchers("/data/**").access("hasRole('ADMIN')")
				.antMatchers("/package/**").access("hasRole('ADMIN')")
				.antMatchers("/commits/**").access("hasRole('ADMIN')")
				.antMatchers("/components/**").access("hasRole('ADMIN')")
				.antMatchers("/history/**").access("hasRole('ADMIN')")
				.antMatchers("/operation/**").access("hasRole('ADMIN')")
				.antMatchers("/info/**").access("hasRole('ADMIN')")
				.and().exceptionHandling()
				.accessDeniedHandler(new OAuth2AccessDeniedHandler());
	}

}