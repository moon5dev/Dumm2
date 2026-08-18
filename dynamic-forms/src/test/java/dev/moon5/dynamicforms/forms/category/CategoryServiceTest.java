package dev.moon5.dynamicforms.forms.category;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class CategoryServiceTest {

	@Test
	void treeOrderedPutsEachChildRightAfterItsParent() {
		CategoryMapper mapper = mock(CategoryMapper.class);

		Category rootA = category(1L, null, "A", 0, 2);
		Category rootB = category(2L, null, "B", 0, 1);
		Category childOfA = category(3L, 1L, "A-1", 1, 1);
		Category childOfB = category(4L, 2L, "B-1", 1, 1);
		when(mapper.findAll()).thenReturn(List.of(rootA, rootB, childOfA, childOfB));

		CategoryService service = new CategoryService(mapper, 3);
		List<Category> ordered = service.getTreeOrdered();

		assertThat(ordered).extracting(Category::getName)
			.containsExactly("B", "B-1", "A", "A-1");
	}

	private Category category(Long id, Long parentId, String name, int depth, int sortOrder) {
		Category c = new Category();
		c.setId(id);
		c.setParentId(parentId);
		c.setName(name);
		c.setDepth(depth);
		c.setSortOrder(sortOrder);
		return c;
	}

	@Test
	void rootCategoryGetsDepthZero() {
		CategoryMapper mapper = mock(CategoryMapper.class);
		CategoryService service = new CategoryService(mapper, 2);

		service.create(null, "Top", 1);

		ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
		verify(mapper).insert(captor.capture());
		assertThat(captor.getValue().getDepth()).isZero();
		assertThat(captor.getValue().getParentId()).isNull();
	}

	@Test
	void childCategoryGetsParentDepthPlusOne() {
		CategoryMapper mapper = mock(CategoryMapper.class);
		Category parent = new Category();
		parent.setId(1L);
		parent.setDepth(0);
		when(mapper.findById(1L)).thenReturn(parent);
		CategoryService service = new CategoryService(mapper, 2);

		service.create(1L, "Mid", 1);

		ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
		verify(mapper).insert(captor.capture());
		assertThat(captor.getValue().getDepth()).isEqualTo(1);
	}

	@Test
	void rejectsCreateBeyondMaxDepth() {
		CategoryMapper mapper = mock(CategoryMapper.class);
		Category parent = new Category();
		parent.setId(1L);
		parent.setDepth(1);
		when(mapper.findById(1L)).thenReturn(parent);
		CategoryService service = new CategoryService(mapper, 2);

		assertThatThrownBy(() -> service.create(1L, "Sub", 1))
			.isInstanceOf(IllegalStateException.class);
		verify(mapper, never()).insert(any(Category.class));
	}

	@Test
	void deleteRejectedWhenChildrenExist() {
		CategoryMapper mapper = mock(CategoryMapper.class);
		when(mapper.existsChildren(1L)).thenReturn(true);
		CategoryService service = new CategoryService(mapper, 2);

		assertThatThrownBy(() -> service.delete(1L))
			.isInstanceOf(IllegalStateException.class);
		verify(mapper, never()).deleteById(any());
	}

	@Test
	void deleteAllowedWhenNoChildren() {
		CategoryMapper mapper = mock(CategoryMapper.class);
		when(mapper.existsChildren(1L)).thenReturn(false);
		CategoryService service = new CategoryService(mapper, 2);

		service.delete(1L);

		verify(mapper).deleteById(1L);
	}
}
