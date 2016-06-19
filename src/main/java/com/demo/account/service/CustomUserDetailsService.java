package com.demo.account.service;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.demo.account.constants.UserAPIConstants;
import com.demo.account.dao.AccountDao;
import com.demo.account.model.Account;

public class CustomUserDetailsService implements UserDetailsService {

	final static Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);

	@Autowired
	private AccountDao accountDao;

	@Autowired
	private SocialAccountFactory socialAccountFactory;

	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Account account;
		try {
			account = getAccountObjectFromUserName(username);
		} catch (Exception e) {
			throw new UsernameNotFoundException("Exception in getting userdetails", e);
		}

		if (account == null) {
			throw new UsernameNotFoundException("Account not registered");
		}
		GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_AUTHENTICATED_USER");
		UserDetails userDetails = (UserDetails) new User(account.getId()+"", account.getPassword(), Arrays.asList(authority));
		logger.info("Details : " + userDetails);
		return userDetails;
	}

	private Account getAccountObjectFromUserName(String username) throws Exception {
		String prefix = StringUtils.split(username, UserAPIConstants.SEPERATOR)[0];
		if (prefix.equals(UserAPIConstants.FACEBOOK_USERNAME_PREFIX)) {
			return socialAccountFactory.getAccountObjectFromWebRequest(StringUtils.split(username, UserAPIConstants.SEPERATOR)[1],
					UserAPIConstants.FACEBOOK, false);

		} else if (prefix.equals(UserAPIConstants.GOOGLE_USERNAME_PREFIX)) {
			if (StringUtils.split(username, UserAPIConstants.SEPERATOR).length == 3) {
				return socialAccountFactory.getAccountObjectFromWebRequest(StringUtils.split(username, UserAPIConstants.SEPERATOR)[1],
						UserAPIConstants.GOOGLE, true);
			} else {
				return socialAccountFactory.getAccountObjectFromWebRequest(StringUtils.split(username, UserAPIConstants.SEPERATOR)[1],
						UserAPIConstants.GOOGLE, false);
			}
		}
		Account account = accountDao.findAccountByEmailId(username);
		System.out.println(account != null ? account.toString() : null);
		return account;
	}

}
