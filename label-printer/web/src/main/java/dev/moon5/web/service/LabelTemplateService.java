package dev.moon5.web.service;

import dev.moon5.web.dto.LabelTemplateCreateDto;
import dev.moon5.web.dto.LabelTemplateResponseDto;
import dev.moon5.web.dto.LabelTemplateUpdateDto;

import java.util.List;

public interface LabelTemplateService {

    List<LabelTemplateResponseDto> getAll();

    LabelTemplateResponseDto getById(Long id);

    LabelTemplateResponseDto create(LabelTemplateCreateDto dto);

    LabelTemplateResponseDto update(Long id, LabelTemplateUpdateDto dto);

    void delete(Long id);

}
