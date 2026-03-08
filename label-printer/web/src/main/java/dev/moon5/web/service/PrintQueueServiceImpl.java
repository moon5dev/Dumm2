package dev.moon5.web.service;

import dev.moon5.web.domain.LabelTemplate;
import dev.moon5.web.domain.PrintQueue;
import dev.moon5.web.dto.PrintQueueRequestDto;
import dev.moon5.web.dto.PrintQueueResponseDto;
import dev.moon5.web.repository.LabelTemplateRepository;
import dev.moon5.web.repository.PrintQueueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PrintQueueServiceImpl implements PrintQueueService {

    private final PrintQueueRepository printQueueRepository;
    private final LabelTemplateRepository labelTemplateRepository;

    @Override
    @Transactional
    public PrintQueueResponseDto request(PrintQueueRequestDto dto) {
        LabelTemplate labelTemplate = labelTemplateRepository.findById(dto.getLabelTemplateId())
                .orElseThrow(() -> new RuntimeException("Label template not found. id: " + dto.getLabelTemplateId()));

        PrintQueue printQueue = PrintQueue.of(
                labelTemplate,
                dto.getPrintData(),
                dto.getPrinterName()
        );

        return PrintQueueResponseDto.from(printQueueRepository.save(printQueue));
    }

    @Override
    public List<PrintQueueResponseDto> getPending() {
        return printQueueRepository.findByStatusOrderByRequestedAtAsc("N")
                .stream()
                .map(PrintQueueResponseDto::from)
                .toList();
    }

    @Override
    public List<PrintQueueResponseDto> getHistoryByTemplateId(Long templateId) {
        return printQueueRepository.findByLabelTemplateIdOrderByRequestedAtDesc(templateId)
                .stream()
                .map(PrintQueueResponseDto::from)
                .toList();
    }

    @Override
    @Transactional
    public PrintQueueResponseDto complete(Long id) {
        PrintQueue printQueue = printQueueRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Print queue not found. id: " + id));
        printQueue.complete();
        return PrintQueueResponseDto.from(printQueue);
    }

    @Override
    @Transactional
    public PrintQueueResponseDto error(Long id) {
        PrintQueue printQueue = printQueueRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Print queue not found. id: " + id));
        printQueue.error();
        return PrintQueueResponseDto.from(printQueue);
    }

}
