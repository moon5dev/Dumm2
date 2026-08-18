package dev.moon5.dynamicforms.auth;

import org.springframework.stereotype.Service;

import at.favre.lib.crypto.bcrypt.BCrypt;

@Service
public class AuthService {

	private final UserMapper userMapper;

	public AuthService(UserMapper userMapper) {
		this.userMapper = userMapper;
	}

	public User authenticate(String loginId, String rawPassword) {
		User user = userMapper.findByLoginId(loginId);
		if (user == null) {
			return null;
		}

		boolean verified = BCrypt.verifyer()
			.verify(rawPassword.toCharArray(), user.getPasswordHash())
			.verified;

		return verified ? user : null;
	}
}
