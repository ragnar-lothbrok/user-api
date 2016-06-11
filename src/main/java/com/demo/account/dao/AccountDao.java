package com.demo.account.dao;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.demo.account.model.Account;

@Repository
public interface AccountDao extends CrudRepository<Account, Long> {

	/**
	 * This method will return an Account which is associated with the given
	 * Email.
	 * 
	 * @param emailId
	 * @return Account
	 */
	Account findAccountByEmailId(String emailId);

	Account findAccountByPhoneNumber(String phoneNumber);

	@Query("select account from Account account where emailId=:emailId or phoneNumber=:phoneNumber")
	List<Account> findAccountByEmailIdOrMobileNumber(@Param("emailId") String emailId,
			@Param("phoneNumber") String phoneNumber);

	@Query("select emailId,firstName,lastName,gender,createDate from Account account where id > 0")
	List<Account> findAllAccount(Pageable pageRequest);

}
