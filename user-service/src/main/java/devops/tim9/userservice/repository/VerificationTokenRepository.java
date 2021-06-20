package devops.tim9.userservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import devops.tim9.userservice.model.VerificationToken;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {

	VerificationToken findByToken(String token);
}