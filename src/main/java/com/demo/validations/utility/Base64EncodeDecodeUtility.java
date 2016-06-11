package com.demo.validations.utility;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Base64EncodeDecodeUtility {
	
	
	private final static Logger logger = LoggerFactory.getLogger(Base64EncodeDecodeUtility.class);

	public static String encode(String username, String password) throws UnsupportedEncodingException {
		try{
			byte[] encodedValue = null;
			encodedValue = Base64.getEncoder().encode((username + ":" + password).getBytes(Charset.forName("UTF-8")));
			return new String(encodedValue);
		}catch(Exception exception){
			logger.error("Exceptin occured : "+exception.getMessage());
		}
		return null;
	}

	public static String[] decode(String encodedValue) {
		try{
			String decodedValue = new String(Base64.getDecoder().decode(encodedValue));
			return decodedValue.split(":");
		}catch(Exception exception){
			logger.error("Exceptin occured : "+exception.getMessage());
		}
		return null;
	}
}
