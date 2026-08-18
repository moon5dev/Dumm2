package dev.moon5.dynamicforms.forms.category;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CategoryMapper {

	void insert(Category category);

	Category findById(@Param("id") Long id);

	List<Category> findAll();

	void update(Category category);

	void deleteById(@Param("id") Long id);

	boolean existsChildren(@Param("parentId") Long parentId);
}
