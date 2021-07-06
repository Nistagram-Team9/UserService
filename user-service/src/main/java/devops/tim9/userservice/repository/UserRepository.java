package devops.tim9.userservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import devops.tim9.userservice.model.User;

public interface UserRepository extends JpaRepository<User, Integer> {

	User findByUsername(String username);

	@Query(value = "select * from user inner join verficationtoken using (id)", nativeQuery = true)
	User findByToken(String token);

}
