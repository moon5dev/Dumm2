package dev.moon5.dynamicforms.forms.template;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceUtils;

import dev.moon5.dynamicforms.forms.category.Category;
import dev.moon5.dynamicforms.forms.category.CategoryMapper;

@MybatisTest
class TemplateMapperTest {

	@Autowired
	private TemplateMapper templateMapper;

	@Autowired
	private CategoryMapper categoryMapper;

	@Autowired
	private DataSource dataSource;

	private Long categoryId;
	private Long userId;

	@BeforeEach
	void setUp() throws Exception {
		Category category = new Category();
		category.setParentId(null);
		category.setName("Inspection Form");
		category.setDepth(0);
		category.setSortOrder(1);
		category.setCreatedAt(LocalDateTime.now());
		categoryMapper.insert(category);
		categoryId = category.getId();

		Connection conn = DataSourceUtils.getConnection(dataSource);
		try (Statement stmt = conn.createStatement()) {
			stmt.execute("INSERT INTO users (login_id, password_hash, created_at) "
				+ "VALUES ('admin', 'dummy-hash', CURRENT_TIMESTAMP)");
			try (var rs = stmt.executeQuery("SELECT id FROM users WHERE login_id = 'admin'")) {
				rs.next();
				userId = rs.getLong(1);
			}
		} finally {
			DataSourceUtils.releaseConnection(conn, dataSource);
		}
	}

	@Test
	void insertAndFindById() {
		Template template = newTemplate("Quality Inspection Report", "<div>Fixed text</div>");
		templateMapper.insert(template);

		assertThat(template.getId()).isNotNull();

		Template found = templateMapper.findById(template.getId());
		assertThat(found.getName()).isEqualTo("Quality Inspection Report");
		assertThat(found.getCategoryId()).isEqualTo(categoryId);
		assertThat(found.getCreatedBy()).isEqualTo(userId);
	}

	@Test
	void contentHtmlRoundTripsThroughClob() {
		String html = "<table class=\"form-table\">"
			+ "<tr><td class=\"dc-region\" data-dc-region=\"text\">Enter here</td></tr>"
			+ "</table>";
		Template template = newTemplate("Template with table", html);
		templateMapper.insert(template);

		Template found = templateMapper.findById(template.getId());
		assertThat(found.getContentHtml()).isEqualTo(html);
	}

	@Test
	void findByCategoryIdReturnsOnlyMatchingTemplates() {
		templateMapper.insert(newTemplate("TemplateA", "<div>A</div>"));
		templateMapper.insert(newTemplate("TemplateB", "<div>B</div>"));

		List<Template> found = templateMapper.findByCategoryId(categoryId);

		assertThat(found).extracting(Template::getName)
			.containsExactlyInAnyOrder("TemplateA", "TemplateB");
	}

	@Test
	void updateChangesContentAndUpdatedBy() {
		Template template = newTemplate("Original", "<div>Original content</div>");
		templateMapper.insert(template);
		Category movedCategory = new Category();
		movedCategory.setParentId(null);
		movedCategory.setName("Moved Category");
		movedCategory.setDepth(0);
		movedCategory.setSortOrder(2);
		movedCategory.setCreatedAt(LocalDateTime.now());
		categoryMapper.insert(movedCategory);

		template.setCategoryId(movedCategory.getId());
		template.setName("Updated");
		template.setContentHtml("<div>Updated content</div>");
		template.setUpdatedBy(userId);
		template.setUpdatedAt(LocalDateTime.now());
		templateMapper.update(template);

		Template found = templateMapper.findById(template.getId());
		assertThat(found.getCategoryId()).isEqualTo(movedCategory.getId());
		assertThat(found.getName()).isEqualTo("Updated");
		assertThat(found.getContentHtml()).isEqualTo("<div>Updated content</div>");
		assertThat(found.getUpdatedBy()).isEqualTo(userId);
	}

	@Test
	void deleteByIdRemovesRow() {
		Template template = newTemplate("Template to delete", "<div>x</div>");
		templateMapper.insert(template);

		templateMapper.deleteById(template.getId());

		assertThat(templateMapper.findById(template.getId())).isNull();
	}

	private Template newTemplate(String name, String contentHtml) {
		Template template = new Template();
		template.setCategoryId(categoryId);
		template.setName(name);
		template.setContentHtml(contentHtml);
		template.setCreatedBy(userId);
		template.setCreatedAt(LocalDateTime.now());
		return template;
	}
}
