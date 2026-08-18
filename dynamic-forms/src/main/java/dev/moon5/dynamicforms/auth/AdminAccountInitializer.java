package dev.moon5.dynamicforms.auth;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import at.favre.lib.crypto.bcrypt.BCrypt;

@Component
public class AdminAccountInitializer implements ApplicationRunner {

	private final UserMapper userMapper;
	private final String loginId;
	private final String rawPassword;

	public AdminAccountInitializer(
			UserMapper userMapper,
			@Value("${dynamic-forms.admin.login-id}") String loginId,
			@Value("${dynamic-forms.admin.password}") String rawPassword) {
		this.userMapper = userMapper;
		this.loginId = loginId;
		this.rawPassword = rawPassword;
	}

	@Override
	public void run(ApplicationArguments args) {
		if (userMapper.findByLoginId(loginId) != null) {
			return;
		}

		User user = new User();
		user.setLoginId(loginId);
		user.setPasswordHash(BCrypt.withDefaults().hashToString(12, rawPassword.toCharArray()));
		user.setCreatedAt(LocalDateTime.now());
		userMapper.insert(user);
	}
}
