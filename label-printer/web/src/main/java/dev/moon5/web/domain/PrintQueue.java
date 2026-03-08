package dev.moon5.web.domain;

import jakarta.persistence.Entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "print_queue")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class PrintQueue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "label_template_id", nullable = false)
    private LabelTemplate labelTemplate;

    @Column(nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String printData;

    @Column(nullable = false, length = 1)
    private String status = "N";

    @Column(length = 100)
    private String printerName;

    private LocalDateTime printedAt;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime requestedAt;

    @CreatedBy
    @Column(updatable = false, length = 50)
    private String createdBy;

    public static PrintQueue of(LabelTemplate labelTemplate, String printData, String printerName) {
        PrintQueue queue = new PrintQueue();
        queue.labelTemplate = labelTemplate;
        queue.printData = printData;
        queue.printerName = printerName;
        queue.status = "N";
        return queue;
    }

    public void complete() {
        this.status = "Y";
        this.printedAt = LocalDateTime.now();
    }

    public void error() {
        this.status = "E";
        this.printedAt = LocalDateTime.now();
    }

}
