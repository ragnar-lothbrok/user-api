package com.demo;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.web.filter.GenericFilterBean;

import com.demo.account.dao.AccountDao;
import com.demo.validations.utility.Base64EncodeDecodeUtility;

public class AuthenticationFilter extends GenericFilterBean {

	private final static Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);

	private AuthenticationManager authenticationManager;
	private String tokenHeaderName;
	private String socialTokenName;
	private String socialTokenType;
	ThreadPoolTaskExecutor threadPoolTaskExecutor;

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest httpRequest = asHttp(request);
		HttpServletResponse httpResponse = asHttp(response);

		String username = null;
		String password = null;
		String token = httpRequest.getHeader(tokenHeaderName);
		String socialTokenValue = httpRequest.getHeader(socialTokenName);
		String socialType = httpRequest.getHeader(socialTokenType);
		String gender = httpRequest.getHeader("GENDER");

		logger.info(tokenHeaderName + " " + token + " -- " + socialTokenName + " " + socialTokenValue + " -- " + socialTokenType + " " + socialType
				+ " -- Gender " + gender);

		String authorization = httpRequest.getHeader("Authorization");
		if (authorization != null && authorization.toString().indexOf("Basic") != -1) {
			String base64Credentials = authorization.toString().substring("Basic".length()).trim();
			username = Base64EncodeDecodeUtility.decode(base64Credentials)[0];
			password = Base64EncodeDecodeUtility.decode(base64Credentials)[1];
		}

		try {
			if (token != null) {
				logger.debug("Trying to authenticate user by X-Auth-Token method. Token: {}", token);
				processTokenAuthentication(gender, token, (HttpServletRequest) request, (HttpServletResponse) response, socialTokenValue, socialType);
			} else if (username != null && password != null) {
				logger.debug("Trying to authenticate user {} by X-Auth-Username method", username);
				processUsernamePasswordAuthentication(httpResponse, username, password);
			}

			logger.debug("AuthenticationFilter is passing request down the filter chain");
		} catch (InternalAuthenticationServiceException internalAuthenticationServiceException) {
			SecurityContextHolder.clearContext();
			logger.error("Internal authentication service exception", internalAuthenticationServiceException.getMessage());
		} catch (AuthenticationException authenticationException) {
			SecurityContextHolder.clearContext();
			logger.error("authenticationException service exception", authenticationException.getMessage());
		} catch (Exception exception) {
			SecurityContextHolder.clearContext();
			logger.error("Exception service exception", exception.getMessage());
		}
		chain.doFilter(request, response);
	}

	private HttpServletRequest asHttp(ServletRequest request) {
		return (HttpServletRequest) request;
	}

	private HttpServletResponse asHttp(ServletResponse response) {
		return (HttpServletResponse) response;
	}

	private void processUsernamePasswordAuthentication(HttpServletResponse httpResponse, String username, String password) throws IOException {
		Authentication resultOfAuthentication = tryToAuthenticateWithUsernameAndPassword(username, password);
		SecurityContextHolder.getContext().setAuthentication(resultOfAuthentication);
	}

	private Authentication tryToAuthenticateWithUsernameAndPassword(String username, String password) {
		UsernamePasswordAuthenticationToken requestAuthentication = new UsernamePasswordAuthenticationToken(username, password);
		return tryToAuthenticate(requestAuthentication);
	}

	private void processTokenAuthentication(String gender, String token, HttpServletRequest request, HttpServletResponse response,
			final String socialTokenValue, String socialTokenType) {
		Authentication resultOfAuthentication = null;
		resultOfAuthentication = tryToAuthenticateWithToken(token);
		SecurityContextHolder.getContext().setAuthentication(resultOfAuthentication);
	}

	private Authentication tryToAuthenticateWithToken(String token) {
		PreAuthenticatedAuthenticationToken requestAuthentication = new PreAuthenticatedAuthenticationToken(token, null);
		return tryToAuthenticate(requestAuthentication);
	}

	private Authentication tryToAuthenticate(Authentication requestAuthentication) {
		Authentication responseAuthentication = authenticationManager.authenticate(requestAuthentication);
		if (responseAuthentication == null || !responseAuthentication.isAuthenticated()) {
			throw new InternalAuthenticationServiceException("Unable to authenticate Domain User for provided credentials");
		}
		logger.debug("User successfully authenticated");
		return responseAuthentication;
	}

	public AuthenticationFilter(AccountDao accountDao, AuthenticationManager authenticationManager, String tokenHeaderName, String socialTokenName,
			String socialTokenType, ThreadPoolTaskExecutor threadPoolTaskExecutor) {
		super();
		this.authenticationManager = authenticationManager;
		this.tokenHeaderName = tokenHeaderName;
		this.socialTokenName = socialTokenName;
		this.socialTokenType = socialTokenType;
		this.threadPoolTaskExecutor = threadPoolTaskExecutor;
	}
}
