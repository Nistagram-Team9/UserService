package devops.team9.userservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class })
public class UserServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(UserServiceApplication.class, args);
		System.out.println("Hell00aaoa");
		System.out.println("Hell00aaoa11");
		System.out.println("Hell00aaoa11");

	}

}
