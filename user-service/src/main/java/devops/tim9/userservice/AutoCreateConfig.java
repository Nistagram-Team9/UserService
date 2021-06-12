package devops.tim9.userservice;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

@Configuration
public class AutoCreateConfig {

	@Bean
	public NewTopic events() {
		return TopicBuilder.name("events").partitions(3).replicas(3).build();
		
	}
}
