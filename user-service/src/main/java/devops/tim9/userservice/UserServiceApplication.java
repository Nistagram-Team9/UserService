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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;

import devops.tim9.userservice.domain.Book;
import devops.tim9.userservice.domain.LibraryEvent;
import devops.tim9.userservice.producer.LibraryEventProducer;

@EnableFeignClients
@SpringBootApplication
@RestController
public class UserServiceApplication {
	
	@Autowired
	LibraryEventProducer libraryEventProducer;

	public static void main(String[] args) {
		SpringApplication.run(UserServiceApplication.class, args);
	}
	
	@GetMapping(value = "event")
	public ResponseEntity<?> produceEvent() {
		LibraryEvent libraryEvent2 = new LibraryEvent(null, new Book("some book"));
		try {
			libraryEventProducer.sendLibraryEvent(libraryEvent2);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ResponseEntity.status(HttpStatus.CREATED).body("created");
	}
	@Bean
	public NewTopic topic() {
	    return TopicBuilder.name("library-events")
	            .partitions(10)
	            .replicas(1)
	            .build();
	}


}


