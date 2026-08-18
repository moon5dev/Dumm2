package dev.moon5.dynamicforms.auth;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.Statement;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceUtils;

@MybatisTest
class UserMapperTest {

	@Autowired
	private UserMapper userMapper;

	@Autowired
	private DataSource dataSource;

	@BeforeEach
	void setUp() throws Exception {
		Connection conn = DataSourceUtils.getConnection(dataSource);
		try (Statement stmt = conn.createStatement()) {
			stmt.execute("INSERT INTO users (login_id, password_hash, created_at) "
				+ "VALUES ('admin', 'dummy-hash', CURRENT_TIMESTAMP)");
		} finally {
			DataSourceUtils.releaseConnection(conn, dataSource);
		}
	}

	@Test
	void findByLoginIdReturnsMatchingUser() {
		User found = userMapper.findByLoginId("admin");

		assertThat(found).isNotNull();
		assertThat(found.getPasswordHash()).isEqualTo("dummy-hash");
	}

	@Test
	void findByLoginIdReturnsNullWhenNotFound() {
		User found = userMapper.findByLoginId("nobody");

		assertThat(found).isNull();
	}
}
