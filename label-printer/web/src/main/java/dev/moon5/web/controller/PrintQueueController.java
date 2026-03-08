package dev.moon5.web.controller;

import dev.moon5.web.dto.PrintQueueRequestDto;
import dev.moon5.web.dto.PrintQueueResponseDto;
import dev.moon5.web.service.PrintQueueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/print-queue")
@RequiredArgsConstructor
public class PrintQueueController {

    private final PrintQueueService printQueueService;

    @PostMapping
    public ResponseEntity<PrintQueueResponseDto> request(@RequestBody @Valid PrintQueueRequestDto dto) {
        PrintQueueResponseDto result = printQueueService.request(dto);
        // TODO: Call C# Client
        return ResponseEntity.ok(result);
    }

    @GetMapping("/history/{templateId}")
    public ResponseEntity<Map<String, Object>> getHistory(@PathVariable Long templateId) {
        List<PrintQueueResponseDto> data = printQueueService.getHistoryByTemplateId(templateId);
        return ResponseEntity.ok(Map.of(
                "data", data,
                "total", data.size()
        ));
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<PrintQueueResponseDto> complete(@PathVariable Long id) {
        return ResponseEntity.ok(printQueueService.complete(id));
    }

    @PutMapping("/{id}/error")
    public ResponseEntity<PrintQueueResponseDto> error(@PathVariable Long id) {
        return ResponseEntity.ok(printQueueService.error(id));
    }

}
