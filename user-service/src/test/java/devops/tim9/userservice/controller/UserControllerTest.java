package devops.tim9.userservice.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.annotation.DirtiesContext.ClassMode;

import devops.tim9.userservice.config.JwtAuthenticationRequest;
import devops.tim9.userservice.config.UserTokenState;
import devops.tim9.userservice.dto.UserDto;
import devops.tim9.userservice.model.User;
import devops.tim9.userservice.repository.UserRepository;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DirtiesContext
@EmbeddedKafka(partitions = 1, controlledShutdown = false, brokerProperties = { "listeners=PLAINTEXT://localhost:9093",
		"port=9093" })
public class UserControllerTest {

	@Autowired
	private TestRestTemplate testRestTemplate;

	@Autowired
	private UserRepository userRepository;

	@Test
	public void registerUser_test_happy() {
		UserDto userDto = new UserDto("John", "Doe", "john@email.com", "0000", "M", "1.1.2001.", "johnny2",
				"johnny.web", "biography", false, true, true, "123");
		ResponseEntity<String> responseEntity = testRestTemplate.postForEntity("/users/register-user", userDto,
				String.class);
		User user = userRepository.findByUsername("johnny2");
		assertEquals("John", user.getName());
		assertEquals("Doe", user.getSurname());
		assertEquals("john@email.com", user.getEmail());
		assertEquals("johnny2", user.getUsername());
		assertEquals("{\"status\":\"success\",\"message\":\"You have successfully registrated.\"}",
				responseEntity.getBody());

	}

	@Test
	public void registerAdmin_test_happy() {
		UserDto userDto = new UserDto("John", "Doe", "john@email.com", "0000", "M", "1.1.2001.", "johnnyAdmin2",
				"johnny.web", "biography", false, true, true, "123");
		ResponseEntity<String> responseEntity = testRestTemplate.postForEntity("/users/register-admin", userDto,
				String.class);
		User user = userRepository.findByUsername("johnnyAdmin2");
		assertEquals("John", user.getName());
		assertEquals("Doe", user.getSurname());
		assertEquals("john@email.com", user.getEmail());
		assertEquals("johnnyAdmin2", user.getUsername());
		assertEquals("{\"status\":\"success\",\"message\":\"You have successfully registrated.\"}",
				responseEntity.getBody());

	}

	@Test
	public void registerAgent_test_happy() {
		UserDto userDto = new UserDto("John", "Doe", "john@email.com", "0000", "M", "1.1.2001.", "johnnyAgent2",
				"johnny.web", "biography", false, true, true, "123");
		ResponseEntity<String> responseEntity = testRestTemplate.postForEntity("/users/register-agent", userDto,
				String.class);
		User user = userRepository.findByUsername("johnnyAgent2");
		assertEquals("John", user.getName());
		assertEquals("Doe", user.getSurname());
		assertEquals("john@email.com", user.getEmail());
		assertEquals("johnnyAgent2", user.getUsername());
		assertEquals(false, user.getIsAccepted());
		assertEquals("{\"status\":\"success\",\"message\":\"You have successfully registrated.\"}",
				responseEntity.getBody());

	}

	@Test
	public void registerAgentAdmin_test_happy() {
		UserDto userDto = new UserDto("John", "Doe", "john@email.com", "0000", "M", "1.1.2001.", "johnnyAdmin3",
				"johnny.web", "biography", false, true, true, "123");
		testRestTemplate.postForEntity("/users/register-admin", userDto, String.class);
		ResponseEntity<UserTokenState> result = testRestTemplate.postForEntity("/users/login",
				new JwtAuthenticationRequest("johnnyAdmin3", "123"), UserTokenState.class);
		String token = result.getBody().getAccessToken();
		UserDto userDto2 = new UserDto("John", "Doe", "john@email.com", "0000", "M", "1.1.2001.", "johnnyAgent3",
				"johnny.web", "biography", false, true, true, "123");
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + token);
		HttpEntity<UserDto> httpEntity = new HttpEntity<UserDto>(userDto2, headers);
		ResponseEntity<String> responseEntity2 = testRestTemplate.exchange("/users/register-agent", HttpMethod.POST,
				httpEntity, String.class);
		User user = userRepository.findByUsername("johnnyAgent3");
		assertEquals("John", user.getName());
		assertEquals("Doe", user.getSurname());
		assertEquals("john@email.com", user.getEmail());
		assertEquals("johnnyAgent3", user.getUsername());
		assertEquals(true, user.getIsAccepted());
		assertEquals("{\"status\":\"success\",\"message\":\"You have successfully registrated.\"}",
				responseEntity2.getBody());

	}

	@Test
	public void getLoggedIn_test_happy() {
		UserDto userDto = new UserDto("John", "Doe", "john@email.com", "0000", "M", "1.1.2001.", "johnnyAdmin4",
				"johnny.web", "biography", false, true, true, "123");
		testRestTemplate.postForEntity("/users/register-admin", userDto, String.class);
		ResponseEntity<UserTokenState> result = testRestTemplate.postForEntity("/users/login",
				new JwtAuthenticationRequest("johnnyAdmin4", "123"), UserTokenState.class);
		String token = result.getBody().getAccessToken();
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + token);
		HttpEntity<Long> httpEntity = new HttpEntity<Long>(headers);
		ResponseEntity<User> responseEntity2 = testRestTemplate.exchange("/users/get-logged", HttpMethod.GET,
				httpEntity, User.class);
		User user = responseEntity2.getBody();
		assertEquals("John", user.getName());
		assertEquals("Doe", user.getSurname());
		assertEquals("john@email.com", user.getEmail());
		assertEquals("johnnyAdmin4", user.getUsername());
		assertEquals(true, user.getIsAccepted());
	}

	@Test
	public void delete_test_happy() {
		User user = userRepository.findByUsername("johnnyAdmin2");
		HttpEntity<Long> httpEntity = new HttpEntity<Long>(new HttpHeaders());
		ResponseEntity<String> responseEntity = testRestTemplate.exchange("/users/" + user.getId(), HttpMethod.DELETE,
				httpEntity, String.class);
		assertEquals("User successfully deleted.", responseEntity.getBody());
		User user2 = userRepository.findByUsername("johnnyAdmin2");
		assertNull(user2);
	}

	@Test
	public void update_test_happy() {
		UserDto userDto = new UserDto("John", "Doe", "john@email.com", "0000", "M", "1.1.2001.", "johnny5",
				"johnny.web", "biography", false, true, true, "123");
		testRestTemplate.postForEntity("/users/register-user", userDto, String.class);
		ResponseEntity<UserTokenState> result = testRestTemplate.postForEntity("/users/login",
				new JwtAuthenticationRequest("johnny5", "123"), UserTokenState.class);
		String token = result.getBody().getAccessToken();
		User user = userRepository.findByUsername("johnny5");
		UserDto userDto2 = new UserDto("Johnny", "Doe", "john@email.com", "0000", "M", "1.1.2001.", "johnny6",
				"johnny.web", "biography", false, true, true, "123");
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + token);
		HttpEntity<UserDto> httpEntity = new HttpEntity<UserDto>(userDto2, headers);
		ResponseEntity<String> responseEntity2 = testRestTemplate.exchange("/users", HttpMethod.PUT, httpEntity,
				String.class);
		assertEquals("{\"status\":\"Success\",\"message\":\"User succesfully updated.\"}", responseEntity2.getBody());

	}

	@Test
	public void update_test_sad() {
		UserDto userDto2 = new UserDto("Johnny", "Doe", "john@email.com", "0000", "M", "1.1.2001.", "johnny6",
				"johnny.web", "biography", false, true, true, "123");
		HttpHeaders headers = new HttpHeaders();
		HttpEntity<UserDto> httpEntity = new HttpEntity<UserDto>(userDto2, headers);
		ResponseEntity<String> responseEntity2 = testRestTemplate.exchange("/users", HttpMethod.PUT, httpEntity,
				String.class);
		assertEquals("{\"status\":\"Error\",\"message\":\"Error while updating user.\"}", responseEntity2.getBody());

	}

}
