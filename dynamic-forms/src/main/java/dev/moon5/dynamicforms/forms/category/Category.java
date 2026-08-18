package dev.moon5.dynamicforms.forms.category;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Category {

	private Long id;
	private Long parentId;
	private String name;
	private Integer depth;
	private Integer sortOrder;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}
