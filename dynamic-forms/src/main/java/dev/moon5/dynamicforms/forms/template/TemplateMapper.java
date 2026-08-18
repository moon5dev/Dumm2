package dev.moon5.dynamicforms.forms.template;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TemplateMapper {

	void insert(Template template);

	Template findById(@Param("id") Long id);

	List<Template> findByCategoryId(@Param("categoryId") Long categoryId);

	List<Template> findAll();

	void update(Template template);

	void deleteById(@Param("id") Long id);
}
