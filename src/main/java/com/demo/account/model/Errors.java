package com.demo.account.model;

import java.io.Serializable;

public class Errors implements Serializable{

	private static final long serialVersionUID = 1L;
	private String errorCode;
	private String errorDescription;

	public Errors(String errorCode, String errorDescription) {
		super();
		this.errorCode = errorCode;
		this.errorDescription = errorDescription;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	public String getErrorDescription() {
		return errorDescription;
	}

	public void setErrorDescription(String errorDescription) {
		this.errorDescription = errorDescription;
	}
	
	

}
