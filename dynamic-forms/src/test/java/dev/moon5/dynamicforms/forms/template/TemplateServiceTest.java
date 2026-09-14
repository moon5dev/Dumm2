package dev.moon5.dynamicforms.forms.template;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import dev.moon5.dynamicforms.forms.category.CategoryMapper;
import dev.moon5.dynamicforms.forms.category.CategoryService;

class TemplateServiceTest {

	@Test
	void createSetsFieldsAndInsertsTemplate() {
		TemplateMapper templateMapper = mock(TemplateMapper.class);
		CategoryService categoryService = newCategoryService(templateMapper);
		TemplateService service = new TemplateService(templateMapper, categoryService);

		service.create(1L, "Inspection Report", "<div>Content</div>", 10L);

		ArgumentCaptor<Template> captor = ArgumentCaptor.forClass(Template.class);
		verify(templateMapper).insert(captor.capture());
		Template inserted = captor.getValue();
		assertThat(inserted.getCategoryId()).isEqualTo(1L);
		assertThat(inserted.getName()).isEqualTo("Inspection Report");
		assertThat(inserted.getContentHtml()).isEqualTo("<div>Content</div>");
		assertThat(inserted.getCreatedBy()).isEqualTo(10L);
		assertThat(inserted.getCreatedAt()).isNotNull();
	}

	@Test
	void updateRejectsUnknownTemplate() {
		TemplateMapper templateMapper = mock(TemplateMapper.class);
		when(templateMapper.findById(99L)).thenReturn(null);
		CategoryService categoryService = newCategoryService(templateMapper);
		TemplateService service = new TemplateService(templateMapper, categoryService);

		assertThatThrownBy(() -> service.update(99L, 1L, "Name", "<div/>", 10L))
			.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void copyRejectsUnknownTemplate() {
		TemplateMapper templateMapper = mock(TemplateMapper.class);
		when(templateMapper.findById(99L)).thenReturn(null);
		CategoryService categoryService = newCategoryService(templateMapper);
		TemplateService service = new TemplateService(templateMapper, categoryService);

		assertThatThrownBy(() -> service.copy(99L, 10L))
			.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void copyCreatesNewTemplateFromSourceContent() {
		TemplateMapper templateMapper = mock(TemplateMapper.class);
		Template source = new Template();
		source.setId(5L);
		source.setCategoryId(2L);
		source.setName("Inspection Report");
		source.setContentHtml("<div>Source</div>");
		source.setDraftContentHtml("<div>User draft</div>");
		source.setDraftSavedAt(java.time.LocalDateTime.now());
		when(templateMapper.findById(5L)).thenReturn(source);
		CategoryService categoryService = newCategoryService(templateMapper);
		TemplateService service = new TemplateService(templateMapper, categoryService);

		Template copied = service.copy(5L, 20L);

		ArgumentCaptor<Template> captor = ArgumentCaptor.forClass(Template.class);
		verify(templateMapper).insert(captor.capture());
		Template inserted = captor.getValue();
		assertThat(copied).isSameAs(inserted);
		assertThat(inserted.getCategoryId()).isEqualTo(2L);
		assertThat(inserted.getName()).isEqualTo("Inspection Report - Copy");
		assertThat(inserted.getContentHtml()).isEqualTo("<div>Source</div>");
		assertThat(inserted.getDraftContentHtml()).isNull();
		assertThat(inserted.getDraftSavedAt()).isNull();
		assertThat(inserted.getCreatedBy()).isEqualTo(20L);
		assertThat(inserted.getCreatedAt()).isNotNull();
	}

	@Test
	void updateSetsUpdatedByAndUpdatedAt() {
		TemplateMapper templateMapper = mock(TemplateMapper.class);
		Template existing = new Template();
		existing.setId(5L);
		when(templateMapper.findById(5L)).thenReturn(existing);
		CategoryService categoryService = newCategoryService(templateMapper);
		TemplateService service = new TemplateService(templateMapper, categoryService);

		service.update(5L, 2L, "Updated Name", "<div>Updated</div>", 20L);

		ArgumentCaptor<Template> captor = ArgumentCaptor.forClass(Template.class);
		verify(templateMapper).update(captor.capture());
		Template updated = captor.getValue();
		assertThat(updated.getCategoryId()).isEqualTo(2L);
		assertThat(updated.getName()).isEqualTo("Updated Name");
		assertThat(updated.getUpdatedBy()).isEqualTo(20L);
		assertThat(updated.getUpdatedAt()).isNotNull();
	}

	private CategoryService newCategoryService(TemplateMapper templateMapper) {
		return new CategoryService(mock(CategoryMapper.class), templateMapper, 2);
	}
}
