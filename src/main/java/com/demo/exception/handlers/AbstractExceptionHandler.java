package com.demo.exception.handlers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import com.demo.account.model.Errors;
import com.demo.account.postprocessor.ValidationException;

/**
 * This class will catch the AccountValidationExcepiton and send that in Json
 * format.
 * 
 * @author raghunandangupta
 *
 */
@ControllerAdvice
public class AbstractExceptionHandler {

	private final static Logger LOGGER = LoggerFactory.getLogger(AbstractExceptionHandler.class);

	/**
	 * Method will fetch Error descriptions and send them in json format.
	 * 
	 * @param ex
	 * @return
	 */
	@ExceptionHandler(ValidationException.class)
	@ResponseBody
	public ResponseEntity<Map<String, List<Errors>>> processAccountValidationException(ValidationException ex) {
		LOGGER.error("Exception occured : " , ex);
		return new ResponseEntity<Map<String, List<Errors>>>(ex.getErrorDescription(), HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(BadCredentialsException.class)
	@ResponseBody
	public ResponseEntity<Map<String, String>> processLoginValidationException(BadCredentialsException ex) {
		LOGGER.error("Exception occured : " , ex);
		Map<String, String> errorMap = new HashMap<String, String>();
		errorMap.put("error", "Please provide valid credentials");
		return new ResponseEntity<Map<String, String>>(errorMap, HttpStatus.UNAUTHORIZED);
	}

	@ExceptionHandler(AuthenticationException.class)
	@ResponseBody
	public ResponseEntity<Map<String, String>> processLoginValidationException(AuthenticationException ex) {
		LOGGER.error("Exception occured : " , ex);
		Map<String, String> errorMap = new HashMap<String, String>();
		errorMap.put("error", "Please provide valid credentials");
		return new ResponseEntity<Map<String, String>>(errorMap, HttpStatus.UNAUTHORIZED);
	}

}
