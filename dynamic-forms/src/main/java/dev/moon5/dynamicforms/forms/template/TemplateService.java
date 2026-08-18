package dev.moon5.dynamicforms.forms.template;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import dev.moon5.dynamicforms.forms.category.Category;
import dev.moon5.dynamicforms.forms.category.CategoryService;

@Service
public class TemplateService {

	private final TemplateMapper templateMapper;
	private final CategoryService categoryService;

	public TemplateService(TemplateMapper templateMapper, CategoryService categoryService) {
		this.templateMapper = templateMapper;
		this.categoryService = categoryService;
	}

	public List<Template> getAll() {
		return templateMapper.findAll();
	}

	public Template getById(Long id) {
		return templateMapper.findById(id);
	}

	public List<Category> getCategoriesForSelect() {
		return categoryService.getTreeOrdered();
	}

	public Map<Long, String> getCategoryNameMap() {
		return categoryService.getTreeOrdered().stream()
			.collect(Collectors.toMap(Category::getId, Category::getName));
	}

	public void create(Long categoryId, String name, String contentHtml, Long createdBy) {
		Template template = new Template();
		template.setCategoryId(categoryId);
		template.setName(name);
		template.setContentHtml(contentHtml);
		template.setCreatedBy(createdBy);
		template.setCreatedAt(LocalDateTime.now());
		templateMapper.insert(template);
	}

	public void update(Long id, Long categoryId, String name, String contentHtml, Long updatedBy) {
		Template template = templateMapper.findById(id);
		if (template == null) {
			throw new IllegalArgumentException("Template does not exist.");
		}
		template.setCategoryId(categoryId);
		template.setName(name);
		template.setContentHtml(contentHtml);
		template.setUpdatedBy(updatedBy);
		template.setUpdatedAt(LocalDateTime.now());
		templateMapper.update(template);
	}

	public void delete(Long id) {
		templateMapper.deleteById(id);
	}
}
