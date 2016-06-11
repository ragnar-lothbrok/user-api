package com.demo.account.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.springframework.social.facebook.api.User;
import org.springframework.social.google.api.plus.Person;

import com.demo.account.constants.UserAPIConstants;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
@Table(
	uniqueConstraints = { @UniqueConstraint(
		columnNames = { "emailId", "phoneNumber" }) })
public class Account implements Serializable {

	@Transient
	@JsonIgnore
	private static final long serialVersionUID = -146386294680936829L;

	public Account(User userProfile, String password2) {
		this.setEmailId(userProfile.getEmail());
		this.setFirstName(userProfile.getFirstName());
		this.setLastName(userProfile.getLastName());
		this.setGender(userProfile.getGender());
		this.setPassword(password2);
		// this.setPhoneNumber(userProfile.get);
		try {
			if (userProfile.getBirthday() != null) {
				this.setDob(UserAPIConstants.SQL_DATE_FORMAT.format(UserAPIConstants.FACEBOOK_DATE_FORMAT.parse(userProfile.getBirthday())));
			}
		} catch (Exception exception) {

		}
		this.setSsoProvider("Facebook");
	}

	public Account() {
		
	}

	public Account(Person person) {
		this.setEmailId(person.getAccountEmail());
		this.setFirstName(person.getGivenName());
		this.setLastName(person.getFamilyName());
		this.setDob(person.getBirthday() == null ? null : UserAPIConstants.SQL_DATE_FORMAT.format(new Date(person.getBirthday().getTime())));
		this.setGender(person.getGender());
		this.setSsoProvider("Google");
		this.setProfileImage(person.getImageUrl());
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getEmailId() {
		return emailId;
	}

	public void setEmailId(String emailId) {
		this.emailId = emailId;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getSsoProvider() {
		return ssoProvider;
	}

	public void setSsoProvider(String ssoProvider) {
		this.ssoProvider = ssoProvider;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getDob() {
		return dob;
	}

	public void setDob(String dob) {
		this.dob = dob;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public short getIsActive() {
		return isActive;
	}

	public void setIsActive(short isActive) {
		this.isActive = isActive;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getCreateDate() {
		return createDate;
	}

	public void setCreateDate(String createDate) {
		this.createDate = createDate;
	}

	public String getProfileImage() {
		return profileImage;
	}

	public void setProfileImage(String profileImage) {
		this.profileImage = profileImage;
	}

	public String getImsId() {
		return imsId;
	}

	public void setImsId(String imsId) {
		this.imsId = imsId;
	}

	@Id
	@GeneratedValue
	private Long id;

	private String emailId;

	private String firstName;
	private String lastName;
	private String gender;
	private String ssoProvider;

	@JsonIgnore
	@Column(
		name = "password_hash",
		nullable = false)
	private String password;
	private String dob;
	private String phoneNumber;

	private short isActive = 1;
	private String country;

	@JsonIgnore
	private String createDate;

	private String profileImage;
	private String imsId;

}
