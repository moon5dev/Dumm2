package dev.moon5.web.service;

import dev.moon5.web.dto.PrintQueueRequestDto;
import dev.moon5.web.dto.PrintQueueResponseDto;

import java.util.List;

public interface PrintQueueService {

    PrintQueueResponseDto request(PrintQueueRequestDto dto);

    List<PrintQueueResponseDto> getPending();

    List<PrintQueueResponseDto> getHistoryByTemplateId(Long templateId);

    PrintQueueResponseDto complete(Long id);

    PrintQueueResponseDto error(Long id);

}
