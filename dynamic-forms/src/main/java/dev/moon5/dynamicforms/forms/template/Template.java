package dev.moon5.dynamicforms.forms.template;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Template {

	private Long id;
	private Long categoryId;
	private String name;
	private String contentHtml;
	private Long createdBy;
	private LocalDateTime createdAt;
	private Long updatedBy;
	private LocalDateTime updatedAt;
}
