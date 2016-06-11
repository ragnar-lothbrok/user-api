package com.demo.account.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import com.demo.account.dao.AccountDao;
import com.demo.account.model.Account;

@Service
public class TPUserDetailService {

	final static Logger logger = LoggerFactory.getLogger(TPUserDetailService.class);

	@Autowired
	AccountDao accountDao;

	public Account getDetailsBySDToken(String sdToken) throws BadCredentialsException {
		Account account = null;
		if ((account = accountDao.findAccountByEmailId("test@gmail.com")) == null) {
			account = new Account();
			account.setId(1111l);
			account.setEmailId("test@gmail.com");
			account.setPassword("reset@4321");
			accountDao.save(account);
		}
		return account;
	}

}
