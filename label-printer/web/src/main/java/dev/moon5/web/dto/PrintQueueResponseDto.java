package dev.moon5.web.dto;

import dev.moon5.web.domain.PrintQueue;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PrintQueueResponseDto {

    private Long id;
    private Long labelTemplateId;
    private String templateName;
    private String templateJson;
    private String printData;
    private String status;
    private String printerName;
    private LocalDateTime requestedAt;
    private LocalDateTime printedAt;
    private String createdBy;

    public static PrintQueueResponseDto from(PrintQueue printQueue) {
        PrintQueueResponseDto dto = new PrintQueueResponseDto();
        dto.id              = printQueue.getId();
        dto.labelTemplateId = printQueue.getLabelTemplate().getId();
        dto.templateName    = printQueue.getLabelTemplate().getTemplateName();
        dto.templateJson    = printQueue.getLabelTemplate().getTemplateJson();  // ⭐ 추가
        dto.printData       = printQueue.getPrintData();
        dto.status          = printQueue.getStatus();
        dto.printerName     = printQueue.getPrinterName();
        dto.requestedAt     = printQueue.getRequestedAt();
        dto.printedAt       = printQueue.getPrintedAt();
        dto.createdBy       = printQueue.getCreatedBy();
        return dto;
    }

}
