package dev.moon5.dynamicforms.forms.category;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CategoryService {

	private final CategoryMapper categoryMapper;
	private final int maxDepth;

	public CategoryService(
			CategoryMapper categoryMapper,
			@Value("${dynamic-forms.ui.category-max-depth}") int maxDepth) {
		this.categoryMapper = categoryMapper;
		this.maxDepth = maxDepth;
	}

	public List<Category> getTreeOrdered() {
		List<Category> all = categoryMapper.findAll();
		Map<Long, List<Category>> byParentId = all.stream()
			.filter(c -> c.getParentId() != null)
			.collect(Collectors.groupingBy(Category::getParentId));
		byParentId.values().forEach(children ->
			children.sort(Comparator.comparing(Category::getSortOrder, Comparator.nullsLast(Comparator.naturalOrder()))));

		List<Category> roots = all.stream()
			.filter(c -> c.getParentId() == null)
			.sorted(Comparator.comparing(Category::getSortOrder, Comparator.nullsLast(Comparator.naturalOrder())))
			.toList();

		List<Category> ordered = new ArrayList<>();
		for (Category root : roots) {
			appendWithChildren(root, byParentId, ordered);
		}
		return ordered;
	}

	private void appendWithChildren(Category category, Map<Long, List<Category>> byParentId, List<Category> ordered) {
		ordered.add(category);
		for (Category child : byParentId.getOrDefault(category.getId(), List.of())) {
			appendWithChildren(child, byParentId, ordered);
		}
	}

	public Category getById(Long id) {
		return categoryMapper.findById(id);
	}

	public void create(Long parentId, String name, Integer sortOrder) {
		int depth = 0;
		if (parentId != null) {
			Category parent = categoryMapper.findById(parentId);
			if (parent == null) {
				throw new IllegalArgumentException("Parent category does not exist.");
			}
			depth = parent.getDepth() + 1;
		}
		if (depth >= maxDepth) {
			throw new IllegalStateException("Cannot exceed max depth (" + maxDepth + ").");
		}

		Category category = new Category();
		category.setParentId(parentId);
		category.setName(name);
		category.setDepth(depth);
		category.setSortOrder(sortOrder);
		category.setCreatedAt(LocalDateTime.now());
		categoryMapper.insert(category);
	}

	public void update(Long id, String name, Integer sortOrder) {
		Category category = categoryMapper.findById(id);
		if (category == null) {
			throw new IllegalArgumentException("Category does not exist.");
		}
		category.setName(name);
		category.setSortOrder(sortOrder);
		category.setUpdatedAt(LocalDateTime.now());
		categoryMapper.update(category);
	}

	public void delete(Long id) {
		if (categoryMapper.existsChildren(id)) {
			throw new IllegalStateException("Cannot delete: this category has child categories. Delete the children first.");
		}
		categoryMapper.deleteById(id);
	}
}
