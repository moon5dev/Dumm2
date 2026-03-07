package dev.moon5.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class LabelTemplateUpdateDto {

    @NotBlank(message = "Template name is required.")
    private String templateName;

    private String description;

    @NotBlank(message = "Template JSON is required.")
    private String templateJson;

    private Boolean isActive;

}
