package devops.tim9.userservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import javax.transaction.Transactional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;

import devops.tim9.userservice.dto.UserDto;
import devops.tim9.userservice.model.User;
import devops.tim9.userservice.repository.UserRepository;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DirtiesContext
@EmbeddedKafka(partitions = 1, controlledShutdown = false, brokerProperties = { "listeners=PLAINTEXT://localhost:9093",
		"port=9093" })
@Transactional
public class UserServiceTest {
	
	@Autowired
	UserService userService;
	
	@Autowired
	UserRepository userRepository;
	
	@Autowired
	TestRestTemplate testRestTemplate;
	
	@Autowired
	private AuthenticationManager authenticationManager;
	
	@Before
	public void setUp() {
		userRepository.save(new User(new UserDto("John", "Doe", "john@email.com", "0000", "M", "1.1.2001.", "johnny",
				"johnny.web", "biography", false, true, true, "123"),true));
		userRepository.save(new User(new UserDto("John", "Doe", "john@email.com", "0000", "M", "1.1.2001.", "johnny2",
				"johnny.web", "biography", false, true, true, "123"),true));
	}
	
	@Test
	@Rollback(true)
	public void registerUser_test_happy() throws Exception {
		User user = userService.registerUser(new UserDto("John", "Doe", "john@email.com", "0000", "M", "1.1.2001.", "johnny9",
				"johnny.web", "biography", false, true, true, "123"));
		assertEquals("John",user.getName());
		assertEquals("Doe", user.getSurname());
		assertEquals("johnny9",user.getUsername());
		
	}
	
	@Test(expected = IllegalArgumentException.class)
	@Rollback(true)
	public void registerUser_test_sad() throws Exception {
		userService.registerUser(new UserDto("John", "Doe", "john@email.com", "0000", "M", "1.1.2001.", "johnny2",
				"johnny.web", "biography", false, true, true, "123"));
		userService.registerUser(new UserDto("John", "Doe", "john@email.com", "0000", "M", "1.1.2001.", "johnny2",
				"johnny.web", "biography", false, true, true, "123"));
		
	}
	
	@Test
	@Rollback(true)
	public void updateUser_test_happy() throws Exception {
		userService.registerUser(new UserDto("John", "Doe", "john@email.com", "0000", "M", "1.1.2001.", "johnnyUser3",
				"johnny.web", "biography", false, true, true, "123"));
		Authentication authentication = Mockito.mock(Authentication.class);
		authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
				"johnnyUser3", "123"));
		SecurityContext securityContext = Mockito.mock(SecurityContext.class);
		Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
		SecurityContextHolder.setContext(securityContext);
		UserDto userDto2 = new UserDto("Johnny", "Doee", "john@email.com", "0000", "M", "1.1.2001.", "johnnyAdmin3",
				"johnny.web", "biography", false, true, true, "123");
		User user = userService.update(userDto2);
		assertEquals("Johnny", user.getName());
	}
	
	@Test
	@Rollback(true)
	public void getAll_test_happy() {
		List<User> users = userService.getAll();
		assertEquals(2, users.size());
	}	
	
	@Test
	@Rollback(true)
	public void findById_test_happy() {
		User user = userRepository.findByUsername("johnny");
		User user2 = userService.findById(user.getId());
		assertEquals("Doe",user2.surname);
	}
	
	@Test
	@Rollback(true)
	public void usernameTaken_test_happy() {
		assertEquals(false, userService.usernameTaken("jane"));
	}
	
	@Test
	@Rollback(true)
	public void usernameTaken_test_sad() {
		assertEquals(true, userService.usernameTaken("johnny"));
	}
	
	

}
