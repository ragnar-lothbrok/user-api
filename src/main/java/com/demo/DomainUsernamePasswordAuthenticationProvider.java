package com.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

public class DomainUsernamePasswordAuthenticationProvider implements AuthenticationProvider {

	@Autowired
	UserDetailsService userDetailsService;

	@Autowired
	PasswordEncoder passwordEncoder;

	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		String username = (String) authentication.getPrincipal();
		String password = (String) authentication.getCredentials();
		Authentication auth = null;
		boolean isValid = false;
		if (username != null && password != null) {
			UserDetails userDetails = userDetailsService.loadUserByUsername(username);
			if (userDetails != null) {
				isValid = passwordEncoder.matches(password, userDetails.getPassword());
				if(isValid){
					auth = new PreAuthenticatedAuthenticationToken(userDetails.getUsername(),userDetails.getPassword(),userDetails.getAuthorities());
				}
			}
		}
		if (!isValid) {
			throw new BadCredentialsException("Invalid Domain User Credentials");
		}
		return auth;
	}

	public boolean supports(Class<?> authentication) {
		return authentication.equals(UsernamePasswordAuthenticationToken.class);
	}
}
