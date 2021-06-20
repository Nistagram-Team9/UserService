package devops.tim9.userservice.domain;

import devops.tim9.userservice.config.JwtAuthenticationRequest;
import devops.tim9.userservice.config.JwtAuthenticationRequestToSend;
import devops.tim9.userservice.model.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class LoginEvent {

	private Integer loginEventId;
	private JwtAuthenticationRequestToSend authenticationRequest;
}