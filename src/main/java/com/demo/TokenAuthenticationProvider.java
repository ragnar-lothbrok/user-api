package com.demo;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import com.demo.account.model.Account;
import com.demo.account.postprocessor.AccountObjectPostProcessor;
import com.demo.account.service.TPUserDetailService;

public class TokenAuthenticationProvider implements AuthenticationProvider {

	@Autowired
	AccountObjectPostProcessor accountObjectPostProcessor;

	@Autowired
	TPUserDetailService sDUserDetailService;

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		String token = (String) authentication.getPrincipal();
		if (token == null || token.isEmpty()) {
			throw new BadCredentialsException("Invalid token");
		}
		Collection<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
		authorities.add(new SimpleGrantedAuthority("ROLE_AUTHENTICATED_USER"));
		Account account = sDUserDetailService.getDetailsBySDToken(token);
		if (account != null && (account.getEmailId() != null || account.getPhoneNumber() != null)) {
			return new PreAuthenticatedAuthenticationToken(account.getId(), null, authorities);
		}
		return null;
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return authentication.equals(PreAuthenticatedAuthenticationToken.class);
	}
}
