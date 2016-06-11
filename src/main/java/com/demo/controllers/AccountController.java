package com.demo.controllers;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.demo.account.dao.AccountDao;
import com.demo.account.model.Account;
import com.demo.account.postprocessor.AccountObjectPostProcessor;
import com.demo.account.postprocessor.ValidationException;
import com.demo.validations.utility.ValidationUtility;

@RestController
@RequestMapping("/")
public class AccountController {

	final static Logger logger = LoggerFactory.getLogger(AccountController.class);

	@Autowired
	private AccountDao accountDao;

	@Autowired
	private AccountObjectPostProcessor accountObjectPostProcessor;

	@Autowired
	PasswordEncoder passwordEncoder;

	@Autowired
	RedisTemplate<String, Object> redisTemplate;

	@RequestMapping(
		value = { "/signup" },
		method = { RequestMethod.POST },
		consumes = { MediaType.APPLICATION_JSON_VALUE })
	public Account signUp(@RequestBody Account account, HttpServletRequest request) throws ValidationException {
		try {
			if (account != null) {
				account.setPassword(account.getPassword() == null ? null : account.getPassword().trim());
			}
//			account.setPassword("secret");
			Account postProcessedObject = accountObjectPostProcessor.getValidatedObject(account);
			accountDao.save(postProcessedObject);
			account = accountDao.findAccountByEmailId(account.getEmailId());
			account.setPassword(null);
			logger.info("Saved Account Details : " + account);
		} catch (ValidationException e) {
			logger.error("Exception Occured while signUp : ", e);
			throw e;
		}
		return account;
	}

	/**
	 * Method will return User Details present in database.
	 * 
	 * @return
	 */
	@RequestMapping(
		value = "/userDetails",
		method = RequestMethod.GET,
		produces = { MediaType.APPLICATION_JSON_VALUE })
	public Map<String, Object> getUserDetails(HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> responseMap = new HashMap<String, Object>();
		try {
			Account account = null;
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			if (auth == null) {
				auth = ValidationUtility.getAuthenticationFromRedis(request, redisTemplate);
			}
			logger.info("Authentication : " + auth);
			if (auth != null && auth.getPrincipal() != null) {
				account = accountDao.findOne(Long.parseLong(auth.getPrincipal().toString()));
				account.setPassword(null);
				responseMap.put("status", Boolean.TRUE);
				responseMap.put("userDetails", account);
			} else {
				responseMap.put("status", Boolean.FALSE);
			}
		} catch (Exception exception) {
			logger.error("Exception Occured while getUserDetails : ", exception);
			responseMap.put("status", Boolean.FALSE);
		}
		logger.info("Get User details : " + responseMap);
		return responseMap;
	}

}