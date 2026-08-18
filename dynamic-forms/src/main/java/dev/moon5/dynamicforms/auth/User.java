package dev.moon5.dynamicforms.auth;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class User {

	private Long id;
	private String loginId;
	private String passwordHash;
	private LocalDateTime createdAt;
}
