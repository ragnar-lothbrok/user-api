package com.demo.exception.handlers;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import org.springframework.security.crypto.password.PasswordEncoder;

import com.demo.account.constants.UserAPIConstants;

/**
 * Custom Password encoder MD5 is added to support magento customer migration.
 * 
 * @author raghunandangupta
 *
 */
public class CustomMD5PasswordEncoder implements PasswordEncoder {

	public String encode(CharSequence rawPassword) {
		return null;
	}
	
	public String encode(CharSequence rawPassword,String salt) {
		String generatedPassword = null;
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(salt.getBytes());

			byte[] bytes = md.digest(rawPassword.toString().getBytes());
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < bytes.length; i++) {
				sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
			}
			generatedPassword = sb.toString();
		} catch (NoSuchAlgorithmException e) {

		}
		return generatedPassword;
	}

	public boolean matches(CharSequence rawPassword, String encodedPassword) {
		boolean isMatched = false;
		if(encodedPassword != null){
			String[] values = encodedPassword.split(UserAPIConstants.PASSWORD_SALT_SEPERATOR);
			String encodedRawPassword = encode(rawPassword, values[1].trim());
			isMatched = values[0].trim().equals(encodedRawPassword.toString());
		}
		System.out.println("Paword Matched : "+isMatched);
	    return isMatched;
	}

	// will generate random salt
	public String getSalt(Integer len) {
		StringBuilder permutation = new StringBuilder("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789");
		Random random = new Random();
		StringBuilder sb = new StringBuilder(len);
		for (int i = 0; i < len; i++)
			sb.append(permutation.charAt(random.nextInt(permutation.length())));
		return sb.toString();
	}

}
