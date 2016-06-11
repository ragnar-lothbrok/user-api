package com.demo.account.service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.util.Date;

import javax.annotation.PostConstruct;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.social.facebook.api.GraphApi;
import org.springframework.social.facebook.api.ImageType;
import org.springframework.social.facebook.api.User;
import org.springframework.social.facebook.api.impl.FacebookTemplate;
import org.springframework.social.google.api.Google;
import org.springframework.social.google.api.impl.GoogleTemplate;
import org.springframework.social.google.api.plus.Person;
import org.springframework.social.google.connect.GoogleConnectionFactory;
import org.springframework.social.support.URIBuilder;
import org.springframework.stereotype.Component;

import com.demo.account.constants.ErrorCodeConstants;
import com.demo.account.constants.UserAPIConstants;
import com.demo.account.dao.AccountDao;
import com.demo.account.dto.FacebookImage;
import com.demo.account.model.Account;
import com.demo.account.model.Errors;
import com.demo.account.postprocessor.AccountObjectPostProcessor;
import com.demo.account.postprocessor.ValidationException;
import com.demo.validations.utility.ValidationUtility;

@Component
public class SocialAccountFactory {

	final static Logger logger = LoggerFactory.getLogger(SocialAccountFactory.class);

	@Autowired
	private GoogleConnectionFactory googleConnectionFactory;

	@Autowired
	private AccountDao accountDao;

	@Value("${google.redirect.uri}")
	private String redirectionURI;
	
	@Value("${google.redirect.uri.web}")
	private String redirectionURIWeb;
	

	@Autowired
	private AccountObjectPostProcessor accountObjectPostProcessor;

	public Account getAccountObjectFromWebRequest(String code, String ssoProvider,Boolean webRequest) throws Exception {
		Account account = null;
		if (UserAPIConstants.FACEBOOK.equalsIgnoreCase(ssoProvider)) {
			String accessToken = code;
			User userProfile = null;
			FacebookTemplate facebookTemplate = new FacebookTemplate(accessToken);

			if (facebookTemplate != null) {
				userProfile = facebookTemplate.userOperations().getUserProfile();
				if (userProfile != null) {
					String profileImage = fetchProfileImage(userProfile);
					String emailId = userProfile.getEmail();
					logger.info("Email Id : " + emailId);
					// When Email Id is found in User Profile.
					if (emailId != null) {
						Account existingAccount = accountDao.findAccountByEmailId(emailId);
						if (existingAccount == null) {
							account = new Account(userProfile, accessToken);
							accountObjectPostProcessor.savePasswordWithSalt(account);
							account.setCreateDate(UserAPIConstants.SQL_TIMESTAMP_FORMAT.format(new Date()));
							account.setProfileImage(profileImage != null ? profileImage : null);
							accountDao.save(account);
						} else {
							existingAccount.setPassword(code);
							accountObjectPostProcessor.savePasswordWithSalt(existingAccount);
							account = existingAccount;
						}
					} else {
						ValidationException accountValidationException = new ValidationException(new Exception());
						ValidationUtility.setErrors(accountValidationException,
								new Errors(ErrorCodeConstants.EMAIL_DUPLICATE, "Email Id already exists."));
						throw accountValidationException;
					}
				}
			}
		} else if (UserAPIConstants.GOOGLE.equalsIgnoreCase(ssoProvider)) {
			// Fetching access token using Authorization code.
			String accessToken = null;
			Google google = null;
			if(webRequest){
				google = googleConnectionFactory
						.createConnection(
								googleConnectionFactory.getOAuthOperations().exchangeForAccess(code, redirectionURIWeb, null))
						.getApi();
			}else{
				google = googleConnectionFactory
						.createConnection(
								googleConnectionFactory.getOAuthOperations().exchangeForAccess(code, redirectionURI, null))
						.getApi();
			}
			accessToken = google.getAccessToken();

			// After access token we will be fetching details.
			Person person = null;
			GoogleTemplate plusTemplate = new GoogleTemplate(accessToken);
			person = plusTemplate.plusOperations().getGoogleProfile();
			if (person != null) {
				if (person.getAccountEmail() != null) {
					Account existingAccount = accountDao.findAccountByEmailId(person.getAccountEmail());
					if (existingAccount == null) {
						account = new Account(person);
						account.setPassword(code);
						accountObjectPostProcessor.savePasswordWithSalt(account);
						account.setCreateDate(UserAPIConstants.SQL_TIMESTAMP_FORMAT.format(new Date()));
						accountDao.save(account);
					} else {
						existingAccount.setPassword(code);
						accountObjectPostProcessor.savePasswordWithSalt(existingAccount);
						account = existingAccount;
					}
				} else {
					ValidationException accountValidationException = new ValidationException(new Exception());
					ValidationUtility.setErrors(accountValidationException,
							new Errors(ErrorCodeConstants.EMAIL_DUPLICATE, "Please provide valid email address."));
					throw accountValidationException;
				}
			}
		}
		return account;
	}

	/**
	 * Method will fetch profile image
	 * 
	 * @param userProfile
	 * @return
	 */
	private String fetchProfileImage(User userProfile) {
		try {
			URI uri = URIBuilder.fromUri(GraphApi.GRAPH_API_URL + userProfile.getId() + "/picture" + "?type="
					+ ImageType.SMALL.toString().toLowerCase() + "&redirect=false").build();
			HttpClient httpClient = HttpClientBuilder.create().build();
			HttpGet get = new HttpGet(uri.toString());
			HttpResponse response = httpClient.execute(get);
			InputStream inputStream = response.getEntity().getContent();
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
			String line = null;
			while ((line = bufferedReader.readLine()) != null) {
				ObjectMapper objectMapper = new ObjectMapper();
				FacebookImage facebookImage = objectMapper.readValue(line, FacebookImage.class);
				logger.info("Image Details : " + facebookImage);
				return facebookImage.getData().getUrl();
			}
		} catch (Exception exception) {
			logger.error("Exception occured : " , exception);
		}
		return null;
	}

	/**
	 * Code is added to remove video upload limits fields as that was out of
	 * integer range not supported in Spinr facebook social
	 */
	@PostConstruct
	private void init() {
		// hack for the login of facebook.
		try {
			String[] fieldsToMap = { "id", "about", "age_range", "bio", "birthday", "context", "cover", "currency",
					"devices", "education", "email", "favorite_athletes", "favorite_teams", "first_name", "gender",
					"hometown", "inspirational_people", "languages", "last_name", "locale", "location", "name",
					"name_format", "political", "relationship_status", "religion", "sports", "timezone", "updated_time",
					"work" };

			Field field = Class.forName("org.springframework.social.facebook.api.UserOperations")
					.getDeclaredField("PROFILE_FIELDS");
			field.setAccessible(true);

			Field modifiers = field.getClass().getDeclaredField("modifiers");
			modifiers.setAccessible(true);
			modifiers.setInt(field, field.getModifiers() & ~Modifier.FINAL);
			field.set(null, fieldsToMap);

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
