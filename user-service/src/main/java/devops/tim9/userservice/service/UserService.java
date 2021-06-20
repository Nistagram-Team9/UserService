package devops.tim9.userservice.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;

import devops.tim9.userservice.domain.UserEvent;
import devops.tim9.userservice.dto.UserDto;
import devops.tim9.userservice.model.Authority;
import devops.tim9.userservice.model.Role;
import devops.tim9.userservice.model.User;
import devops.tim9.userservice.producer.UserEventProducer;
import devops.tim9.userservice.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetailsService;


@Service
public class UserService implements UserDetailsService{
	
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	
	@Autowired
	UserEventProducer userEventProducer;
	
	public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}
	
	

	public User findById(Integer id) {
		return userRepository.findById(id).orElse(null);
	}

	public User registerUser(UserDto userDto) throws Exception {
		System.out.println("users password");
		System.out.println(userDto.getPassword());
		if (this.usernameTaken(userDto.getUsername())) {
			throw new IllegalArgumentException("Username is already taken.");
		}
		User user = new User(userDto);
		user.setPassword(this.passwordEncoder.encode(userDto.getPassword()));
		System.out.println(user.getPassword());
		List<Authority> authorities = new ArrayList<>();
		authorities.add(new Authority(Role.ROLE_USER));
		user.setAuthorities(authorities);
		this.create(user);
		
		
		UserEvent userEvent2 = new UserEvent(null, user, "registerUser");
		try {
			userEventProducer.sendUserEvent(userEvent2);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return user;
	}
	
	public User registerAdmin(UserDto userDto) throws Exception {
		if (this.usernameTaken(userDto.getUsername())) {
			throw new IllegalArgumentException("Username is already taken.");
		}
		User user = new User(userDto);
		user.setPassword(this.passwordEncoder.encode(userDto.getPassword()));
		List<Authority> authorities = new ArrayList<>();
		authorities.add(new Authority(Role.ROLE_ADMIN));
		user.setAuthorities(authorities);
		this.create(user);
		
		UserEvent userEvent2 = new UserEvent(null, user, "registerAdmin");
		try {
			userEventProducer.sendUserEvent(userEvent2);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return user;
	}
	
	public User update(Integer id, UserDto userDto) {
		User user = this.findById(id);
		if (user != null) {
			user.name = userDto.getName();
			user.surname = userDto.getSurname();
			user.email = userDto.getEmail();
			user.phoneNumber = userDto.getPhoneNumber();
			user.sex = userDto.getSex();
			user.birthDate = userDto.getBirthDate();
			user.username = userDto.getUsername();
			user.website = userDto.getWebsite();
			user.biography = userDto.getBiography();
			user.isPrivate = userDto.getIsPrivate();
			user.canBeTagged = userDto.getCanBeTagged();
			user.isActive = userDto.getIsActive();
			user.setPassword(this.passwordEncoder.encode(userDto.getPassword()));

			UserEvent userEvent2 = new UserEvent(null, user, "update");
			try {
				userEventProducer.sendUserEvent(userEvent2);
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return user;
	}


	public boolean usernameTaken(String username) {
		User user = userRepository.findByUsername(username);
		return user != null;
	}

	public List<User> getAll() {
		return userRepository.findAll();
	}

	public User create(User user) {
		return userRepository.save(user);
	}

	public User delete(Integer id) {
		Optional<User> user = userRepository.findById(id);
		userRepository.delete(user.get());
		UserEvent userEvent2 = new UserEvent(null, user.get(), "delete");
		try {
			userEventProducer.sendUserEvent(userEvent2);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return user.get();
	}
	
	public User findUserByToken(String token) {
		return userRepository.findByToken(token);
	}

	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		return userRepository.findByUsername(username);
	}


}


