package dev.moon5.web.controller;

import dev.moon5.web.dto.LabelTemplateCreateDto;
import dev.moon5.web.dto.LabelTemplateResponseDto;
import dev.moon5.web.dto.LabelTemplateUpdateDto;
import dev.moon5.web.service.LabelTemplateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/labels")
@RequiredArgsConstructor
public class LabelTemplateController {

    private final LabelTemplateService labelTemplateService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAll() {
        List<LabelTemplateResponseDto> data = labelTemplateService.getAll();
        return ResponseEntity.ok(Map.of(
                "data", data,
                "total", data.size()
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LabelTemplateResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(labelTemplateService.getById(id));
    }

    @PostMapping
    public ResponseEntity<LabelTemplateResponseDto> create(@RequestBody @Valid LabelTemplateCreateDto dto) {
        return ResponseEntity.ok(labelTemplateService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LabelTemplateResponseDto> update(@PathVariable Long id,
            @RequestBody @Valid LabelTemplateUpdateDto dto) {
        return ResponseEntity.ok(labelTemplateService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        labelTemplateService.delete(id);
        return ResponseEntity.noContent().build();
    }

}