package com.demo.account.service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.util.Date;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.social.facebook.api.GraphApi;
import org.springframework.social.facebook.api.ImageType;
import org.springframework.social.facebook.api.User;
import org.springframework.social.google.api.Google;
import org.springframework.social.google.api.impl.GoogleTemplate;
import org.springframework.social.google.api.plus.Person;
import org.springframework.social.google.connect.GoogleConnectionFactory;
import org.springframework.social.support.URIBuilder;
import org.springframework.stereotype.Component;

import com.demo.account.constants.UserAPIConstants;
import com.demo.account.dao.AccountDao;
import com.demo.account.dto.FacebookImage;
import com.demo.account.dto.SocialUserDetail;
import com.demo.account.model.Account;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class PersonalizationSocialAccountFactory {

	final static Logger logger = LoggerFactory.getLogger(PersonalizationSocialAccountFactory.class);

	@Autowired
	private AccountDao accountDao;

	@Value("${google.redirect.uri}")
	private String redirectionURI;

	@Value("${facebook.image.size:LARGE}")
	private String faceBookImageSize;

	@Autowired
	GoogleConnectionFactory googleConnectionFactory;

	@Autowired
	ThreadPoolTaskExecutor threadPoolTaskExecutor;

	public Account getDetailsFromSocialAccount(SocialUserDetail socialUserDetail, Account account, Map<String, Object> facebookDataMap) {
		if (UserAPIConstants.FACEBOOK.equalsIgnoreCase(socialUserDetail.getSocialType())
				|| UserAPIConstants.GOOGLE.equalsIgnoreCase(socialUserDetail.getSocialType())) {
			if (UserAPIConstants.FACEBOOK.equalsIgnoreCase(socialUserDetail.getSocialType())) {
				return getDetailsFromFacebook(socialUserDetail, account, facebookDataMap);
			} else if (UserAPIConstants.GOOGLE.equalsIgnoreCase(socialUserDetail.getSocialType())) {
				getDetailsFromGoogle(socialUserDetail);
			}
		}
		return account;
	}

	public Account getDetailsFromFacebook(SocialUserDetail socialUserDetail, Account existingAccount, Map<String, Object> facebookDataMap) {
		logger.info("Details from facebook started : " + socialUserDetail);
		try {
			if (facebookDataMap != null && facebookDataMap.get("template") != null) {
				User userProfile = (User) facebookDataMap.get("profile");
				if (userProfile != null) {
					String profileImage = fetchProfileImage(userProfile);
					logger.info("#### ProfileImage : " + profileImage);
					String emailId = userProfile.getEmail();
					logger.info("Email Id : " + emailId);
					// When Email Id is found in User Profile.
					if (existingAccount != null) {
						existingAccount.setFirstName(userProfile.getFirstName());
						existingAccount.setLastName(userProfile.getLastName());
						existingAccount.setGender(userProfile.getGender());
						existingAccount.setProfileImage(profileImage != null ? profileImage : null);
						existingAccount.setSsoProvider("Facebook");
						if (userProfile.getBirthday() != null) {
							try {
								existingAccount.setDob(UserAPIConstants.SQL_DATE_FORMAT
										.format(UserAPIConstants.FACEBOOK_DATE_FORMAT.parse(userProfile.getBirthday())));
							} catch (Exception exception) {
								logger.error("Exception occured : ", exception);
							}
						}
						existingAccount = accountDao.save(existingAccount);
					}
				}
			}
		} catch (Exception exception) {
			logger.error("Exception occured : ", exception);
		}
		return existingAccount;
	}

	public Account getDetailsFromGoogle(SocialUserDetail socialUserDetail) {
		Account existingAccount = null;
		try {
			String accessToken = socialUserDetail.getAccessToken();
			Google google = googleConnectionFactory
					.createConnection(googleConnectionFactory.getOAuthOperations().exchangeForAccess(accessToken, redirectionURI, null)).getApi();
			accessToken = google.getAccessToken();

			// After access token we will be fetching details.
			Person person = null;
			GoogleTemplate plusTemplate = new GoogleTemplate(accessToken);
			person = plusTemplate.plusOperations().getGoogleProfile();
			if (person != null) {
				if (person.getAccountEmail() != null) {
					existingAccount = accountDao.findOne(socialUserDetail.getId());
					if (existingAccount != null) {
						// existingAccount.setEmailId(person.getAccountEmail());
						if (person.getFamilyName() != null) {
							existingAccount.setFirstName(person.getDisplayName());
							existingAccount.setLastName(null);
						}
						if (person.getBirthday() != null)
							existingAccount.setDob(person.getBirthday() == null ? null
									: UserAPIConstants.SQL_DATE_FORMAT.format(new Date(person.getBirthday().getTime())));
						if (person.getGender() != null)
							existingAccount.setGender(person.getGender());
						existingAccount.setSsoProvider("Google");
						if (person.getImageUrl() != null)
							existingAccount.setProfileImage(person.getImageUrl());
						accountDao.save(existingAccount);
					}
				}
			}
		} catch (Exception exception) {
			logger.error("Exception occured : ", exception);
		}
		return existingAccount;
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
					+ ImageType.valueOf(faceBookImageSize).toString().toLowerCase() + "&redirect=false").build();
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
			logger.error("Exception occured : ", exception);
		}
		return null;
	}

	/**
	 * Code is added to remove video upload limits fields as that was out of
	 * integer range not supported in Spring facebook social
	 */
	@PostConstruct
	private void init() {
		// hack for the login of facebook.
		try {
			String[] fieldsToMap = { "id", "about", "age_range", "bio", "birthday", "context", "cover", "currency", "devices", "education", "email",
					"favorite_athletes", "favorite_teams", "first_name", "gender", "hometown", "inspirational_people", "installed", "install_type",
					"is_verified", "languages", "last_name", "link", "locale", "location", "meeting_for", "middle_name", "name", "name_format",
					"political", "quotes", "payment_pricepoints", "relationship_status", "religion", "security_settings", "significant_other",
					"sports", "test_group", "timezone", "third_party_id", "updated_time", "verified", "viewer_can_send_gift", "website", "work" };

			Field field = Class.forName("org.springframework.social.facebook.api.UserOperations").getDeclaredField("PROFILE_FIELDS");
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
