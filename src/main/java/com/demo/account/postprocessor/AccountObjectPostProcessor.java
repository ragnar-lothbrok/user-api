package com.demo.account.postprocessor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.demo.account.constants.ErrorCodeConstants;
import com.demo.account.constants.UserAPIConstants;
import com.demo.account.dao.AccountDao;
import com.demo.account.model.Account;
import com.demo.account.model.Errors;
import com.demo.account.service.SocialAccountFactory;
import com.demo.exception.handlers.CustomMD5PasswordEncoder;
import com.demo.validations.utility.ValidationUtility;

@Component
public class AccountObjectPostProcessor {

	final static Logger logger = LoggerFactory.getLogger(AccountObjectPostProcessor.class);

	@Autowired
	private SocialAccountFactory socialAccountFactory;

	@Autowired
	PasswordEncoder passwordEncoder;

	@Autowired
	AccountDao accountDao;

	public Account getValidatedObject(Account account) throws ValidationException {
		if (account.getSsoProvider().equals("exclusively")) {
			return validateObject(account);
		} else {
			try {
				return socialAccountFactory.getAccountObjectFromWebRequest(account.getPassword(),
						account.getSsoProvider(),false);
			} catch (Exception e) {
				throw new ValidationException(e);
			}
		}
	}

	/**
	 * Method will be validating if User is already exists in System
	 * 
	 * @param account
	 * @return
	 * @throws ValidationException
	 */
	private Account validateObject(Account account) throws ValidationException {
		ValidationUtility.validateAccount(account);
		Account existingAccount = accountDao.findAccountByEmailId(account.getEmailId());
		if (existingAccount != null && existingAccount.getEmailId() != null
				&& existingAccount.getEmailId().equals(account.getEmailId())) {
			logger.error("Email Id already exists in system : " + existingAccount.getEmailId());
			ValidationException accountValidationException = new ValidationException(new Exception());
			ValidationUtility.setErrors(accountValidationException,new Errors(ErrorCodeConstants.EMAIL_DUPLICATE, "Email Id already exists."));
			throw accountValidationException;
		}
		savePasswordWithSalt(account);
		return account;
	}

	/**
	 * This method will generate salt and password and save in Account object.
	 * 
	 * @param account
	 */
	public void savePasswordWithSalt(Account account) {
		String salt = ((CustomMD5PasswordEncoder) passwordEncoder).getSalt(UserAPIConstants.PASSWORD_STRENGTH);
		account.setPassword(((CustomMD5PasswordEncoder) passwordEncoder).encode(account.getPassword(),salt) + ":" + salt);
	}

}
