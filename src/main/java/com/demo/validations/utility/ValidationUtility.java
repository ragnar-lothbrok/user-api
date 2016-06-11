package com.demo.validations.utility;

import java.util.ArrayList;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;

import com.demo.account.constants.ErrorCodeConstants;
import com.demo.account.constants.UserAPIConstants;
import com.demo.account.model.Account;
import com.demo.account.model.Errors;
import com.demo.account.postprocessor.ValidationException;

/**
 * This class will be used to validate different Models.
 * 
 * @author raghunandangupta
 *
 */
public class ValidationUtility {

	final static Logger logger = LoggerFactory.getLogger(ValidationUtility.class);

	/**
	 * Validate all the mandatory fields.
	 * 
	 * @param account
	 * @throws ValidationException
	 */
	public static void validateAccount(Account account) throws ValidationException {
		ValidationException accountValidationException = new ValidationException(new Exception());
		try {
			if (account == null) {
				setErrors(accountValidationException, new Errors(ErrorCodeConstants.INVALID_SIGNUP, "Please provide valid Signup details."));
			} else {
				if (!iSValidEmail(account.getEmailId())) {
					setErrors(accountValidationException, new Errors(ErrorCodeConstants.EMAIL_INVALID, "Please provide valid email address."));
				}

				if (!(StringUtils.isNotEmpty(account.getPassword()) && StringUtils.isNotBlank(account.getPassword()))) {
					setErrors(accountValidationException, new Errors(ErrorCodeConstants.SINGUP_PASSWORD, "Please provide valid Password."));
				}
				if (StringUtils.isNotEmpty(account.getPhoneNumber()) && StringUtils.isNotBlank(account.getPhoneNumber())
						&& !StringUtils.isNumeric(account.getPhoneNumber())) {
					setErrors(accountValidationException, new Errors(ErrorCodeConstants.SIGUP_PHONE, "Please provide valid Phone Number."));
				}
				if (account.getDob() != null) {
					try {
						UserAPIConstants.SQL_DATE_FORMAT.parse(account.getDob());
					} catch (Exception exception) {
						setErrors(accountValidationException, new Errors(ErrorCodeConstants.SIGNUP_DOB, "Please provide valid Date of birth."));
						logger.error("Exception occured while formatting date : ", exception);
					}
				}
			}

		} catch (Exception exception) {
			logger.error("Exception occured : ", exception);
			setErrors(accountValidationException, new Errors(ErrorCodeConstants.SOME_ERROR, "Some error occured."));
			throw accountValidationException;
		}
		if (accountValidationException.getErrorDescription().size() > 0) {
			throw accountValidationException;
		}
		account.setCreateDate(UserAPIConstants.SQL_TIMESTAMP_FORMAT.format(new Date()));
		account.setIsActive((short) 1);
	}

	public static void setErrors(ValidationException accountValidationException, Errors error) {
		if (accountValidationException.getErrorDescription().get("error") == null) {
			accountValidationException.getErrorDescription().put("error", new ArrayList<Errors>());
		}
		accountValidationException.getErrorDescription().get("error").add(error);
	}

	/**
	 * This method will validate if Email is valid or not.
	 * 
	 * @param emailId
	 * @return
	 */
	public static boolean iSValidEmail(String emailId) {
		if (StringUtils.isNotEmpty(emailId) && StringUtils.isNotBlank(emailId)) {
			EmailValidator emailValidator = EmailValidator.getInstance();
			return emailValidator.isValid(emailId);
		} else {
			return false;
		}
	}

	/**
	 * This will validate Update Profile Details.
	 * 
	 * @param account
	 * @param existingAccount
	 * @throws ValidationException
	 */
	public static void validateAccountUpdate(Account account, Account existingAccount) throws ValidationException {
		ValidationException accountValidationException = new ValidationException(new Exception());
		try {
			if (account == null) {
				setErrors(accountValidationException, new Errors(ErrorCodeConstants.UPDATE_ACCOUNT_INVALID, "Please provide valid Account details."));
			} else {
				if (existingAccount == null) {
					setErrors(accountValidationException, new Errors(ErrorCodeConstants.UPDATE_ACCOUNT_NOT_EXISTS, "Account doesn't exists."));
				}
				if (account.getId() == null || account.getId() == 0) {
					account.setId(existingAccount.getId());
				}
				if (account.getId().longValue() != existingAccount.getId().longValue()) {
					setErrors(accountValidationException,
							new Errors(ErrorCodeConstants.UPDATE_ACCOUNT_ID_EMAIL_MISMATCH, "Please provide valid Account Id."));
				}
				if (StringUtils.isNotEmpty(account.getPhoneNumber()) && StringUtils.isNotBlank(account.getPhoneNumber())
						&& !StringUtils.isNumeric(account.getPhoneNumber())) {
					setErrors(accountValidationException,
							new Errors(ErrorCodeConstants.UPDATE_ACCOUNT_PHONE_INVALID, "Please provide valid Phone Number."));
				}
				if (account.getDob() != null && StringUtils.isNotEmpty(account.getPhoneNumber())) {
					try {
						UserAPIConstants.SQL_DATE_FORMAT.parse(account.getDob());
					} catch (Exception exception) {
						setErrors(accountValidationException,
								new Errors(ErrorCodeConstants.UPDATE_ACCOUNT_DOB_INVALID, "Please provide valid Date of birth."));
						logger.error("Exception occured while formatting date : " + exception.getMessage());
					}
				}
			}

		} catch (Exception exception) {
			logger.error("Exception occured : ", exception);
			setErrors(accountValidationException, new Errors(ErrorCodeConstants.SOME_ERROR, "Some error occured."));
			throw accountValidationException;
		}
		if (accountValidationException.getErrorDescription().size() > 0) {
			throw accountValidationException;
		}
	}

	public static Authentication getAuthenticationFromRedis(HttpServletRequest request, RedisTemplate<String, Object> redisTemplate) {
		Authentication auth = null;
		try {
			if (request.getParameter("access_token") != null) {
				Object obj = redisTemplate.opsForValue().get(request.getParameter("access_token"));
				if (obj != null && obj instanceof SecurityContext) {
					auth = ((SecurityContext) obj).getAuthentication();
				}
			}
		} catch (Exception exception) {
			exception.printStackTrace();
			logger.error("Exception occured while fetching data from redis : " + request.getParameter("access_token"), exception);
		}
		return auth;
	}
}
