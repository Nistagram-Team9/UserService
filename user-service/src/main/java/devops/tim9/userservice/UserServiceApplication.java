package devops.tim9.userservice;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;

import devops.tim9.userservice.domain.Book;
import devops.tim9.userservice.domain.UserEvent;
import devops.tim9.userservice.producer.UserEventProducer;

@EnableFeignClients
@SpringBootApplication
@RestController
public class UserServiceApplication {


	public static void main(String[] args) {
		SpringApplication.run(UserServiceApplication.class, args);
	}
	
	@Bean
	public NewTopic topic() {
	    return TopicBuilder.name("user-events")
	            .partitions(10)
	            .replicas(1)
	            .build();
	}
	

	@Bean
	public NewTopic topicLogin() {
	    return TopicBuilder.name("login-events")
	            .partitions(10)
	            .replicas(1)
	            .build();
	}
	
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}



}


