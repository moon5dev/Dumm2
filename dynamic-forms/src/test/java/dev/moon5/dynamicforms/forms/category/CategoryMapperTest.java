package dev.moon5.dynamicforms.forms.category;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;

@MybatisTest
class CategoryMapperTest {

	@Autowired
	private CategoryMapper categoryMapper;

	@Test
	void insertAndFindById() {
		Category root = newCategory(null, "Top", 0, 1);
		categoryMapper.insert(root);

		assertThat(root.getId()).isNotNull();

		Category found = categoryMapper.findById(root.getId());
		assertThat(found.getName()).isEqualTo("Top");
		assertThat(found.getParentId()).isNull();
		assertThat(found.getDepth()).isEqualTo(0);
	}

	@Test
	void selfReferenceParentChild() {
		Category parent = newCategory(null, "Top", 0, 1);
		categoryMapper.insert(parent);

		Category child = newCategory(parent.getId(), "Mid", 1, 1);
		categoryMapper.insert(child);

		Category found = categoryMapper.findById(child.getId());
		assertThat(found.getParentId()).isEqualTo(parent.getId());
	}

	@Test
	void findAllOrdersByDepthThenSortOrder() {
		Category root = newCategory(null, "Top", 0, 1);
		categoryMapper.insert(root);
		Category childB = newCategory(root.getId(), "MidB", 1, 2);
		categoryMapper.insert(childB);
		Category childA = newCategory(root.getId(), "MidA", 1, 1);
		categoryMapper.insert(childA);

		List<Category> all = categoryMapper.findAll();

		assertThat(all).extracting(Category::getName)
			.containsExactly("Top", "MidA", "MidB");
	}

	@Test
	void existsChildrenReflectsActualChildren() {
		Category parent = newCategory(null, "Top", 0, 1);
		categoryMapper.insert(parent);

		assertThat(categoryMapper.existsChildren(parent.getId())).isFalse();

		Category child = newCategory(parent.getId(), "Mid", 1, 1);
		categoryMapper.insert(child);

		assertThat(categoryMapper.existsChildren(parent.getId())).isTrue();
	}

	@Test
	void updateChangesNameAndSortOrder() {
		Category category = newCategory(null, "Original", 0, 1);
		categoryMapper.insert(category);

		category.setName("Changed");
		category.setSortOrder(2);
		category.setUpdatedAt(LocalDateTime.now());
		categoryMapper.update(category);

		Category found = categoryMapper.findById(category.getId());
		assertThat(found.getName()).isEqualTo("Changed");
		assertThat(found.getSortOrder()).isEqualTo(2);
	}

	@Test
	void deleteByIdRemovesRow() {
		Category category = newCategory(null, "ToDelete", 0, 1);
		categoryMapper.insert(category);

		categoryMapper.deleteById(category.getId());

		assertThat(categoryMapper.findById(category.getId())).isNull();
	}

	private Category newCategory(Long parentId, String name, int depth, int sortOrder) {
		Category category = new Category();
		category.setParentId(parentId);
		category.setName(name);
		category.setDepth(depth);
		category.setSortOrder(sortOrder);
		category.setCreatedAt(LocalDateTime.now());
		return category;
	}
}
