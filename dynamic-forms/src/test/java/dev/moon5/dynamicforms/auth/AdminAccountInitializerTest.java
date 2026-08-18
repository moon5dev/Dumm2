package dev.moon5.dynamicforms.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import at.favre.lib.crypto.bcrypt.BCrypt;

class AdminAccountInitializerTest {

	@Test
	void seedsAdminAccountWhenNoneExists() throws Exception {
		UserMapper userMapper = mock(UserMapper.class);
		when(userMapper.findByLoginId("admin")).thenReturn(null);
		AdminAccountInitializer initializer =
			new AdminAccountInitializer(userMapper, "admin", "changeme123");

		initializer.run(null);

		verify(userMapper, times(1)).insert(any(User.class));
	}

	@Test
	void insertedPasswordIsBcryptHashedAndVerifiable() throws Exception {
		UserMapper userMapper = mock(UserMapper.class);
		when(userMapper.findByLoginId("admin")).thenReturn(null);
		AdminAccountInitializer initializer =
			new AdminAccountInitializer(userMapper, "admin", "changeme123");

		initializer.run(null);

		ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
		verify(userMapper).insert(captor.capture());
		User inserted = captor.getValue();

		assertThat(inserted.getLoginId()).isEqualTo("admin");
		assertThat(inserted.getPasswordHash()).isNotEqualTo("changeme123");
		boolean verified = BCrypt.verifyer()
			.verify("changeme123".toCharArray(), inserted.getPasswordHash())
			.verified;
		assertThat(verified).isTrue();
	}

	@Test
	void doesNotSeedWhenAdminAlreadyExists() throws Exception {
		UserMapper userMapper = mock(UserMapper.class);
		User existing = new User();
		existing.setLoginId("admin");
		when(userMapper.findByLoginId("admin")).thenReturn(existing);
		AdminAccountInitializer initializer =
			new AdminAccountInitializer(userMapper, "admin", "changeme123");

		initializer.run(null);

		verify(userMapper, never()).insert(any(User.class));
	}
}
