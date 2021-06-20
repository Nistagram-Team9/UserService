package devops.tim9.userservice.controller;

import java.io.IOException;
import java.util.List;

import javax.naming.AuthenticationException;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import devops.tim9.userservice.config.JwtAuthenticationRequest;
import devops.tim9.userservice.config.UserTokenState;
import devops.tim9.userservice.config.WebSecurityConfig;
import devops.tim9.userservice.dto.UserDto;
import devops.tim9.userservice.model.User;
import devops.tim9.userservice.service.UserService;

@RestController
@RequestMapping(value = "/users", produces = MediaType.APPLICATION_JSON_VALUE)
public class UserController {

	private UserService userService;
	private WebSecurityConfig webSecurityConfig;

	public UserController(UserService userService, WebSecurityConfig webSecurityConfig) {
		this.userService = userService;
		this.webSecurityConfig = webSecurityConfig;
	}

	@PostMapping("/register-user")
	public ResponseEntity<String> registerUser(@RequestBody UserDto userDto) {
		try {
			User user = userService.registerUser(userDto);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new ResponseEntity<>("You have successfully registrated.", HttpStatus.CREATED);
	}
	
	@PostMapping("/register-admin")
	public ResponseEntity<String> registerAdmin(@RequestBody UserDto userDto) {
		try {
			User user = userService.registerAdmin(userDto);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new ResponseEntity<>("You have successfully registrated.", HttpStatus.CREATED);
	}
	
	@GetMapping(value = "/{id}")
	public ResponseEntity<User> get(@PathVariable Integer id) {
		User user = userService.findById(id);
		if (user != null) {
			return new ResponseEntity<>(user, HttpStatus.OK);
		} else {
			return ResponseEntity.badRequest().body(new User());
		}
	}
	
	@GetMapping
	public ResponseEntity<List<User>> getAll() {
		List<User> users = userService.getAll();
		return new ResponseEntity<>(users, HttpStatus.OK);
	}
	
	@PutMapping(value = "/{id}")
	public ResponseEntity<String> update(@PathVariable Integer id, @RequestBody UserDto userDto) {
		User user = userService.update(id, userDto);
		if (user != null) {
			return new ResponseEntity<>("User succesfully updated.", HttpStatus.OK);
		} else {
			return ResponseEntity.badRequest().body("Error while updating user.");
		}
	}
	
	@DeleteMapping(value = "/{id}")
	public ResponseEntity<String> delete(@PathVariable Integer id) {
		User user = userService.delete(id);
		if (user != null) {
			return new ResponseEntity<>("User successfully deleted.", HttpStatus.OK);
		} else {
			return ResponseEntity.badRequest().body("Error while deleting user.");
		}
	}
	
	@PostMapping(value = "/login")
	public ResponseEntity<?> createAuthenticationToken(@RequestBody JwtAuthenticationRequest authenticationRequest,
		HttpServletResponse response) throws AuthenticationException, IOException, Exception {

		UserTokenState userTokenState = webSecurityConfig.login(authenticationRequest);
		if (userTokenState == null) {
			return new ResponseEntity<>("Wrong username or password", HttpStatus.NOT_FOUND);
		} else {
			return new ResponseEntity<>(userTokenState, HttpStatus.OK);
		}
	}
	
	
}
