package com.demo.account.postprocessor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.demo.account.model.Errors;

public class ValidationException extends Exception {

	private static final long serialVersionUID = 1L;
	private Map<String, List<Errors>> errorDescription = new HashMap<String, List<Errors>>();

	public ValidationException(Exception e) {
		super(e);
	}

	public ValidationException(String errorMessage) {
		super(errorMessage);
	}

	public Map<String, List<Errors>> getErrorDescription() {
		if(this.errorDescription == null){
			List<Errors> errorList = new ArrayList<Errors>();
			this.errorDescription = new HashMap<String, List<Errors>>();
			this.errorDescription.put("error", errorList);
			
		}
		return this.errorDescription;
	}

	public void setErrorDescription(Map<String, List<Errors>> errorDescription) {
		this.errorDescription = errorDescription;
	}

}
