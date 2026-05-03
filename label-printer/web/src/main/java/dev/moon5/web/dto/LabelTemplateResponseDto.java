package dev.moon5.web.dto;

import dev.moon5.web.domain.LabelTemplate;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class LabelTemplateResponseDto {

    private Long id;
    private String templateName;
    private String description;
    private String templateJson;
    private Boolean isActive;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
    private String createdBy;
    private String modifiedBy;

    public static LabelTemplateResponseDto from(LabelTemplate labelTemplate) {
        LabelTemplateResponseDto response = new LabelTemplateResponseDto();
        response.id = labelTemplate.getId();
        response.templateName = labelTemplate.getTemplateName();
        response.description = labelTemplate.getDescription();
        response.templateJson = labelTemplate.getTemplateJson();
        response.isActive = labelTemplate.getIsActive();
        response.createdDate = labelTemplate.getCreatedDate();
        response.modifiedDate = labelTemplate.getModifiedDate();
        response.createdBy = labelTemplate.getCreatedBy();
        response.modifiedBy = labelTemplate.getModifiedBy();

        return response;
    }

}
