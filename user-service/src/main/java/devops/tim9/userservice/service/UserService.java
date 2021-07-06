package devops.tim9.userservice.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import devops.tim9.userservice.domain.AcceptAgentEvent;
import devops.tim9.userservice.domain.DisableUserEvent;
import devops.tim9.userservice.domain.UserEvent;
import devops.tim9.userservice.dto.UserDto;
import devops.tim9.userservice.model.Authority;
import devops.tim9.userservice.model.Role;
import devops.tim9.userservice.model.User;
import devops.tim9.userservice.producer.UserEventProducer;
import devops.tim9.userservice.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetailsService;

@Service
public class UserService implements UserDetailsService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final ObjectMapper objectMapper;

	@Autowired
	UserEventProducer userEventProducer;

	public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, ObjectMapper objectMapper) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.objectMapper = objectMapper;
	}

	public User getLoggedIn() {
		User user = (User) userRepository
				.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
		return user;
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
		User user = new User(userDto, true);
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
		User user = new User(userDto, true);
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

	public User update(UserDto userDto) {
		User user = (User) userRepository
				.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
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
			if (userDto.getPassword() != "") {
				user.setPassword(this.passwordEncoder.encode(userDto.getPassword()));
			}

			UserEvent userEvent2 = new UserEvent(null, user, "update");
			try {
				userEventProducer.sendUserEvent(userEvent2);
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			this.userRepository.save(user);
		}
		return user;
	}

	public User registerAgent(UserDto userDto) throws Exception {
		System.out.println("Registrating agent");
		System.out.println(userDto.getUsername());
		if (this.usernameTaken(userDto.getUsername())) {
			throw new IllegalArgumentException("Username is already taken.");
		}
		User user = (User) userRepository
				.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
		User user2 = null;
		if (user != null && user.getAuthoitiesList().get(0).getRole().equals(Role.ROLE_ADMIN)) {
			user2 = new User(userDto, true);
		}else {
			user2=new User(userDto, false);
		}
		user2.setPassword(this.passwordEncoder.encode(userDto.getPassword()));
		List<Authority> authorities = new ArrayList<>();
		authorities.add(new Authority(Role.ROLE_AGENT));
		user2.setAuthorities(authorities);
		this.create(user2);

		UserEvent userEvent2 = new UserEvent(null, user2, "registerUser");
		try {
			userEventProducer.sendUserEvent(userEvent2);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
	
	@KafkaListener(topics = {"accept-agent-event"})
	public void onMessage(ConsumerRecord<Integer, String> consumerRecord) {
		String value = consumerRecord.value();
		try {
			AcceptAgentEvent acceptAgentEvent = objectMapper.readValue(value, AcceptAgentEvent.class);
			String username = acceptAgentEvent.getUsername();
			User user = userRepository.findByUsername(username);
			if(user!=null) {
				user.setIsAccepted(true);
				userRepository.save(user);
			}
			
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	

	}
	
	@KafkaListener(topics = {"disable-user-event"})
	public void onMessage2(ConsumerRecord<Integer, String> consumerRecord) {
		String value = consumerRecord.value();
		try {
			DisableUserEvent disableUserEvent = objectMapper.readValue(value, DisableUserEvent.class);
			String username = disableUserEvent.getUsername();
			User user = userRepository.findByUsername(username);
			if(user!=null) {
				user.setIsDisabled(true);
				userRepository.save(user);
			}
			
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	

	}

}
