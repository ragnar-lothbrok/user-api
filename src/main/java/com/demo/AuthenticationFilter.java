package com.demo;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

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
import org.springframework.social.facebook.api.User;
import org.springframework.social.facebook.api.impl.FacebookTemplate;
import org.springframework.web.filter.GenericFilterBean;

import com.demo.account.constants.UserAPIConstants;
import com.demo.account.dao.AccountDao;
import com.demo.account.dto.SocialUserDetail;
import com.demo.account.model.Account;
import com.demo.account.service.PersonalizationSocialAccountFactory;
import com.demo.validations.utility.Base64EncodeDecodeUtility;

public class AuthenticationFilter extends GenericFilterBean {

	private final static Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);

	private AccountDao accountDao;
	private AuthenticationManager authenticationManager;
	private String tokenHeaderName;
	private String socialTokenName;
	private PersonalizationSocialAccountFactory personalizationSocialAccountFactory;
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
		Map<String, Object> facebookDataMap = null;
		if (UserAPIConstants.FACEBOOK.equalsIgnoreCase(socialTokenType)) {
			Callable<Map<String, Object>> getFacebookTemplateDetails = () -> {
				try {
					Map<String, Object> map = new LinkedHashMap<String, Object>();
					FacebookTemplate template = new FacebookTemplate(socialTokenValue);
					User userProfile = template.userOperations().getUserProfile();
					map.put("template", template);
					map.put("profile", userProfile);
					return map;
				} catch (Throwable e) {
					logger.error("Error occured while calling getMyWidetsForTopicIds", e);
				}
				return null;
			};
			Future<Map<String, Object>> facebookTemplateFuture = threadPoolTaskExecutor.submit(getFacebookTemplateDetails);
			resultOfAuthentication = tryToAuthenticateWithToken(token);
			try {
				facebookDataMap = facebookTemplateFuture.get(3, TimeUnit.MINUTES);
			} catch (Exception exception) {
				logger.error("Exception occured while fetching facebook template : ", exception);
			}
		} else {
			resultOfAuthentication = tryToAuthenticateWithToken(token);
		}
		try {
			if (resultOfAuthentication != null && resultOfAuthentication.getPrincipal() != null && resultOfAuthentication.isAuthenticated()
					&& resultOfAuthentication.getAuthorities() != null && resultOfAuthentication.getAuthorities().size() > 0) {
				Account account = accountDao.findOne(Long.parseLong(resultOfAuthentication.getPrincipal().toString()));
				if (UserAPIConstants.FACEBOOK.equalsIgnoreCase(socialTokenType) && socialTokenValue != null) {
					SocialUserDetail socialUserDetail = new SocialUserDetail();
					socialUserDetail.setAccessToken(socialTokenValue);
					socialUserDetail.setSocialType(socialTokenType);
					// Can be either mail or Phone Number
					socialUserDetail.setId(Long.parseLong(resultOfAuthentication.getPrincipal().toString()));
					logger.info("Details from social started inside filter : " + socialUserDetail);
					account = personalizationSocialAccountFactory.getDetailsFromSocialAccount(socialUserDetail, account, facebookDataMap);
				} else if (gender != null && (gender.equalsIgnoreCase("female") || gender.equalsIgnoreCase("male")) && account != null) {
					try {
						account.setGender(gender);
						account = accountDao.save(account);
					} catch (Exception exception) {
						logger.error("Exception occured while saving gender : ", exception);
					}
				}
			}
		} catch (Exception exception) {
			logger.error("Exception occured : ", exception);
		}
		SecurityContextHolder.getContext().setAuthentication(resultOfAuthentication);
	}

	private Authentication tryToAuthenticateWithToken(String token) {
		PreAuthenticatedAuthenticationToken requestAuthentication = new PreAuthenticatedAuthenticationToken(token, null);
		return tryToAuthenticate(requestAuthentication);
	}

	private Authentication tryToAuthenticate(Authentication requestAuthentication) {
		Authentication responseAuthentication = authenticationManager.authenticate(requestAuthentication);
		if (responseAuthentication == null || !responseAuthentication.isAuthenticated()) {
			throw new InternalAuthenticationServiceException(
					"Unable to authenticate Domain User for provided credentials");
		}
		logger.debug("User successfully authenticated");
		return responseAuthentication;
	}

	public AuthenticationFilter(AccountDao accountDao, AuthenticationManager authenticationManager,
			String tokenHeaderName, String socialTokenName, String socialTokenType,
			PersonalizationSocialAccountFactory personalizationSocialAccountFactory
			,ThreadPoolTaskExecutor threadPoolTaskExecutor) {
		super();
		this.accountDao = accountDao;
		this.authenticationManager = authenticationManager;
		this.tokenHeaderName = tokenHeaderName;
		this.socialTokenName = socialTokenName;
		this.personalizationSocialAccountFactory = personalizationSocialAccountFactory;
		this.socialTokenType = socialTokenType;
		this.threadPoolTaskExecutor = threadPoolTaskExecutor;
	}
}
