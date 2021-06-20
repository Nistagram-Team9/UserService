package devops.tim9.userservice.service;

import org.springframework.stereotype.Service;

import devops.tim9.userservice.model.VerificationToken;
import devops.tim9.userservice.repository.VerificationTokenRepository;

@Service
public class VerificationTokenService {

	private final VerificationTokenRepository verificationTokenRepository;

	public VerificationTokenService(VerificationTokenRepository verificationTokenRepository) {
		this.verificationTokenRepository = verificationTokenRepository;
	}

	public void saveToken(VerificationToken token) {
		verificationTokenRepository.save(token);
	}
	
	public VerificationToken findByToken(String token) {
		return verificationTokenRepository.findByToken(token);
	}

}