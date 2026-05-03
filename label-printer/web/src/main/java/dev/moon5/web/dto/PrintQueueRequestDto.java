package dev.moon5.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class PrintQueueRequestDto {

    @NotNull(message = "Label template ID is required.")
    private Long labelTemplateId;

    @NotBlank(message = "Print data is required.")
    private String printData;  // JSON string {"PLANT": "서울공장", ...}

    private String printerName;

}