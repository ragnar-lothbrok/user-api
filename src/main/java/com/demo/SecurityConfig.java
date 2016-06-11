package com.demo;

import java.security.KeyPair;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.ManagementServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import com.demo.account.dao.AccountDao;
import com.demo.account.service.ExclusivelyUserDetailsService;
import com.demo.exception.handlers.CustomMD5PasswordEncoder;

@Configuration
@Order(ManagementServerProperties.ACCESS_OVERRIDE_ORDER)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	@Value("${url.getUserInformation.headername}")
	private String headerName;

	@Value("${url.social.token.name}")
	private String socialTokenName;

	@Value("${url.social.token.type}")
	private String socialTokenType;
	
	@Autowired
	ThreadPoolTaskExecutor threadPoolTaskExecutor;

	@Autowired
	AccountDao accountDao;
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests().antMatchers("/secure/**").authenticated().anyRequest().hasRole("AUTHENTICATED_USER")
				.and().logout().logoutUrl("/logout/**").invalidateHttpSession(true).and().headers().frameOptions()
				.xssProtection().httpStrictTransportSecurity().and().anonymous().disable().sessionManagement()
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS);

		http.addFilterAfter(new AuthenticationFilter(accountDao, authenticationManager(), headerName, socialTokenName,
				socialTokenType,threadPoolTaskExecutor), BasicAuthenticationFilter.class);
	}

	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
		auth.authenticationProvider(domainUsernamePasswordAuthenticationProvider())
				.authenticationProvider(tokenAuthenticationProvider());
	}

	@Bean
	public AuthenticationProvider domainUsernamePasswordAuthenticationProvider() {
		return new DomainUsernamePasswordAuthenticationProvider();
	}

	@Bean
	public AuthenticationProvider tokenAuthenticationProvider() {
		return new TokenAuthenticationProvider();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new CustomMD5PasswordEncoder();
	}

	@Bean
	@Override
	protected UserDetailsService userDetailsService() {
		return new ExclusivelyUserDetailsService();

	}

	@Override
	public void configure(WebSecurity web) throws Exception {
		web.ignoring()
				.antMatchers("/signup/**", "/resetPassword/**", "/reset/**", "/webjars/**", "/v2/**", "/deleteUser/**",
						"/getAllAccountDetails/**", "/metrics/**", "/health/**", "/userDetails/**")
				.and().ignoring().antMatchers("/login/**").and().ignoring().antMatchers("/logout/**");
	}

	@Configuration
	@EnableAuthorizationServer
	protected static class OAuth2Config extends AuthorizationServerConfigurerAdapter {

		@Autowired
		private AuthenticationManager authenticationManager;
		
		@Autowired
		CustomInMemoryAuthorizationCodeServices customInMemoryAuthorizationCodeServices;


		@Bean
		public JwtAccessTokenConverter jwtAccessTokenConverter() {
			JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
			KeyPair keyPair = new KeyStoreKeyFactory(new ClassPathResource("keystore.jks"), "foobar".toCharArray())
					.getKeyPair("test");
			converter.setKeyPair(keyPair);
			return converter;
		}

		@Override
		public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
			clients.inMemory().withClient("acme").secret("acmesecret")
					.authorizedGrantTypes("authorization_code", "refresh_token", "password").scopes("openid")
					.accessTokenValiditySeconds(31536000).authorities("ROLE_AUTHENTICATED_USER").autoApprove(true);
		}

		@Override
		public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
			endpoints.authenticationManager(authenticationManager).accessTokenConverter(jwtAccessTokenConverter());
			endpoints.authorizationCodeServices(customInMemoryAuthorizationCodeServices);
		}

		@Override
		public void configure(AuthorizationServerSecurityConfigurer oauthServer) throws Exception {
			oauthServer.tokenKeyAccess("permitAll()").checkTokenAccess("isAuthenticated()");
		}
	}
}
