package com.demo.account.constants;

public interface ErrorCodeConstants {

	public final String SOME_ERROR = "global_001";
	
	public final String EMAIL_INVALID = "signup_001";
	public final String EMAIL_DUPLICATE = "signup_002";
	public final String SINGUP_PASSWORD = "signup_003";
	public final String SIGUP_PHONE = "signup_004";
	public final String SIGNUP_DOB = "signup_005";
	public final String INVALID_SIGNUP = "signup_006";

	public final String UPDATE_ACCOUNT_INVALID = "update_account_001";
	public final String UPDATE_ACCOUNT_PHONE_INVALID = "update_account_002";
	public final String UPDATE_ACCOUNT_DOB_INVALID = "update_account_003";
	public final String UPDATE_ACCOUNT_NOT_EXISTS = "update_account_004";
	public final String UPDATE_ACCOUNT_ID_NOT_EXISTS = "update_account_005";
	public final String UPDATE_ACCOUNT_ID_EMAIL_MISMATCH = "update_account_006";
	
	public final String RESET_PASSWORD_INVALID = "reset_password_001";
	public final String RESET_PASSWORD_FIELD_INVALID = "reset_password_002";
	public final String RESET_PASSWORD_LINK_EXPIRED = "reset_password_003";
	public final String RESET_PASSWORD_EMAIL_INVALID = "reset_password_004";
	
	public final String FORGOT_PASSWORD_INVALID = "forgot_password_001";
	public final String FORGOT_PASSWORD_FIELD_INVALID = "forgot_password_002";
	public final String FORGOT_PASSWORD_CURRENT_PASSWORD_MISMATCH = "forgot_password_003";

}
