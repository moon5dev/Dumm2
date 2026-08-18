package dev.moon5.dynamicforms.auth;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {

	User findByLoginId(@Param("loginId") String loginId);

	void insert(User user);
}
