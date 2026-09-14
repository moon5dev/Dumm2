package dev.moon5.dynamicforms.forms.template;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import dev.moon5.dynamicforms.forms.category.Category;
import dev.moon5.dynamicforms.forms.category.CategoryService;

@Service
public class TemplateService {

	private static final int MAX_TEMPLATE_NAME_LENGTH = 200;
	private static final String COPY_SUFFIX = " - Copy";

	private final TemplateMapper templateMapper;
	private final CategoryService categoryService;

	public TemplateService(TemplateMapper templateMapper, CategoryService categoryService) {
		this.templateMapper = templateMapper;
		this.categoryService = categoryService;
	}

	public List<Template> getAll() {
		return templateMapper.findAll();
	}

	public List<Template> getAll(Long categoryId) {
		if (categoryId == null) {
			return getAll();
		}
		List<Long> categoryIds = categoryService.getSelfAndDescendantIds(categoryId);
		return templateMapper.findAll().stream()
			.filter(t -> categoryIds.contains(t.getCategoryId()))
			.toList();
	}

	public Map<Long, Long> getCategoryTemplateCounts() {
		Map<Long, Long> directCounts = templateMapper.findAll().stream()
			.collect(Collectors.groupingBy(Template::getCategoryId, Collectors.counting()));

		Map<Long, Long> rollupCounts = new HashMap<>();
		for (Category category : categoryService.getTreeOrdered()) {
			long total = categoryService.getSelfAndDescendantIds(category.getId()).stream()
				.mapToLong(id -> directCounts.getOrDefault(id, 0L))
				.sum();
			rollupCounts.put(category.getId(), total);
		}
		return rollupCounts;
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

	public Template copy(Long id, Long createdBy) {
		Template source = templateMapper.findById(id);
		if (source == null) {
			throw new IllegalArgumentException("Template does not exist.");
		}

		Template copy = new Template();
		copy.setCategoryId(source.getCategoryId());
		copy.setName(nextCopyName(source.getName()));
		copy.setContentHtml(source.getContentHtml());
		copy.setCreatedBy(createdBy);
		copy.setCreatedAt(LocalDateTime.now());
		templateMapper.insert(copy);
		return copy;
	}

	private String nextCopyName(String sourceName) {
		Set<String> usedNames = templateMapper.findAll().stream()
			.map(Template::getName)
			.collect(Collectors.toSet());

		String baseName = sourceName == null ? "Template" : sourceName;
		String candidate = withCopySuffix(baseName, COPY_SUFFIX);
		if (!usedNames.contains(candidate)) {
			return candidate;
		}

		for (int copyNo = 2; ; copyNo++) {
			candidate = withCopySuffix(baseName, COPY_SUFFIX + " (" + copyNo + ")");
			if (!usedNames.contains(candidate)) {
				return candidate;
			}
		}
	}

	private String withCopySuffix(String baseName, String suffix) {
		int maxBaseLength = MAX_TEMPLATE_NAME_LENGTH - suffix.length();
		String trimmedBase = baseName.length() > maxBaseLength
			? baseName.substring(0, maxBaseLength)
			: baseName;
		return trimmedBase + suffix;
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

	/** The template with its content swapped for a user-saved draft, if one exists. */
	public Template getForFilling(Long id) {
		Template template = templateMapper.findById(id);
		if (template != null && template.getDraftSavedAt() != null && template.getDraftContentHtml() != null) {
			template.setContentHtml(template.getDraftContentHtml());
		}
		return template;
	}

	public void saveDraft(Long id, String draftContentHtml) {
		Template template = new Template();
		template.setId(id);
		template.setDraftContentHtml(draftContentHtml);
		template.setDraftSavedAt(LocalDateTime.now());
		templateMapper.updateDraft(template);
	}
}
