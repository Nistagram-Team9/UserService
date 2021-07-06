package devops.tim9.userservice.config;


import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import com.fasterxml.jackson.core.JsonProcessingException;

import devops.tim9.userservice.domain.LoginEvent;
import devops.tim9.userservice.domain.UserEvent;
import devops.tim9.userservice.model.Role;
import devops.tim9.userservice.model.User;
import devops.tim9.userservice.model.VerificationToken;
import devops.tim9.userservice.producer.UserEventProducer;
import devops.tim9.userservice.service.UserService;
import devops.tim9.userservice.service.VerificationTokenService;


@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@AllArgsConstructor
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	private TokenHelper tokenHelper;
	private PasswordEncoder passwordEncoder;
	private UserService userService;
	private VerificationTokenService verificationTokenService;

	@Autowired
	UserEventProducer userEventProducer;
	
	@Bean
	@Override
	public AuthenticationManager authenticationManagerBean() throws Exception {
		return super.authenticationManagerBean();
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {

		http
				// communication between client and server is stateless
				.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
				.authorizeRequests()
				.antMatchers("/auth/login").permitAll()
				.antMatchers("/auth/registerAdmin").permitAll()
				.antMatchers("/users/**").permitAll()

				//antMatchers("/auth/registerLibrarian").permitAll()
				//.antMatchers("/books/**").permitAll()
				//.antMatchers("/bookCopies/**").permitAll()
				//.antMatchers("/users/**").permitAll()
				//.antMatchers("/bookRent/**").permitAll()
				// every request needs to be authorized
				.anyRequest().authenticated().and()
				// add filter before every request
				.addFilterBefore(new TokenAuthenticationFilter(tokenHelper, userService),
						BasicAuthenticationFilter.class);
		http.csrf().disable();

	}

	public void configure(WebSecurity web) throws Exception {
		// Token Filter will ignore these paths
		web.ignoring().antMatchers(HttpMethod.POST, "/auth/login", "/h2/**");
		web.ignoring().antMatchers(HttpMethod.GET, "/", "/login", "/h2/**", "/webjars/**", "/*.html", "/favicon.ico",
				"/**/*.html", "/**/*.css", "/**/*.js");


	}


	public UserTokenState login(JwtAuthenticationRequest authenticationRequest) throws Exception {
		final Authentication authentication;
		try {
			authentication = authenticationManagerBean().authenticate(new UsernamePasswordAuthenticationToken(
					authenticationRequest.getUsername(), authenticationRequest.getPassword()));
		} catch (BadCredentialsException e) {
			return null;
		}
		User user = (User) authentication.getPrincipal();
		SecurityContextHolder.getContext().setAuthentication(authentication);
		String jwt = tokenHelper.generateToken(user.getUsername());
		int expiresIn = tokenHelper.getExpiredIn();
		Role role = null;
		if (user.getAuthoitiesList().get(0).getRole().equals(Role.ROLE_ADMIN)) {
			role = Role.ROLE_ADMIN;
		} 
		else if (user.getAuthoitiesList().get(0).getRole().equals(Role.ROLE_AGENT)){
			role = Role.ROLE_AGENT;
		} else {
			role = Role.ROLE_USER;
		}

		VerificationToken verificationToken = new VerificationToken();
		verificationToken.setToken(jwt);
		verificationToken.setUser(user);
		verificationTokenService.saveToken(verificationToken);
		JwtAuthenticationRequestToSend authenticationRequestToSend = new JwtAuthenticationRequestToSend(authenticationRequest.getUsername(), authenticationRequest.getPassword(), jwt);
		LoginEvent loginEvent = new LoginEvent(null, authenticationRequestToSend);
		authenticationRequestToSend.getToken();
		try {
			userEventProducer.sendLoginEvent(loginEvent);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return new UserTokenState(jwt, expiresIn, role);
	}

	public User changePassword(String oldPassword, String newPassword) throws Exception {
		Authentication currentUser = SecurityContextHolder.getContext().getAuthentication();
		String username = currentUser.getName();
		authenticationManagerBean().authenticate(new UsernamePasswordAuthenticationToken(username, oldPassword));
		User user = (User) userService.loadUserByUsername(username);
		user.setPassword(passwordEncoder.encode(newPassword));
		userService.create(user);
		return user;
	}


}
