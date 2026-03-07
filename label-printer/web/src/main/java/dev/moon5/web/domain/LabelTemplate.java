package dev.moon5.web.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "label_template")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class LabelTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String templateName;

    @Column(length = 500)
    private String description;

    @Column(nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String templateJson;

    @Column(nullable = false)
    private Boolean isActive = true;

    @CreatedDate
    private LocalDateTime createdDate;

    @CreatedBy
    private String createdBy;

    @LastModifiedDate
    private LocalDateTime modifiedDate;

    @LastModifiedBy
    private String modifiedBy;

    public static LabelTemplate of(String templateName, String description, String templateJson, Boolean isActive) {
        LabelTemplate labelTemplate = new LabelTemplate();
        labelTemplate.templateName = templateName;
        labelTemplate.description = description;
        labelTemplate.templateJson = templateJson;
        labelTemplate.isActive = isActive;

        return labelTemplate;
    }

    public void update(String templateName, String description, String templateJson, Boolean isActive) {
        this.templateName = templateName;
        this.description = description;
        this.templateJson = templateJson;
        this.isActive = isActive;
    }

    public void activate() {
        this.isActive = true;
    }

    public void deactivate() {
        this.isActive = false;
    }

}
