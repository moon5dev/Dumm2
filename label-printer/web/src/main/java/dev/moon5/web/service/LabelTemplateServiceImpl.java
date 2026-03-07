package dev.moon5.web.service;

import dev.moon5.web.domain.LabelTemplate;
import dev.moon5.web.dto.LabelTemplateCreateDto;
import dev.moon5.web.dto.LabelTemplateResponseDto;
import dev.moon5.web.dto.LabelTemplateUpdateDto;
import dev.moon5.web.repository.LabelTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LabelTemplateServiceImpl implements LabelTemplateService {

    private final LabelTemplateRepository labelTemplateRepository;

    @Override
    public List<LabelTemplateResponseDto> getAll() {
        return labelTemplateRepository.findAll()
                .stream()
                .map(LabelTemplateResponseDto::from)
                .toList();
    }

    @Override
    public LabelTemplateResponseDto getById(Long id) {
        LabelTemplate labelTemplate = labelTemplateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Label template not found. id: " + id));
        return LabelTemplateResponseDto.from(labelTemplate);
    }

    @Override
    @Transactional
    public LabelTemplateResponseDto create(LabelTemplateCreateDto dto) {
        LabelTemplate labelTemplate = LabelTemplate.of(
                dto.getTemplateName(),
                dto.getDescription(),
                dto.getTemplateJson(),
                dto.getIsActive()
        );
        return LabelTemplateResponseDto.from(labelTemplateRepository.save(labelTemplate));
    }

    @Override
    @Transactional
    public LabelTemplateResponseDto update(Long id, LabelTemplateUpdateDto dto) {
        LabelTemplate labelTemplate = labelTemplateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Label template not found. id: " + id));
        labelTemplate.update(
                dto.getTemplateName(),
                dto.getDescription(),
                dto.getTemplateJson(),
                dto.getIsActive()
        );
        return LabelTemplateResponseDto.from(labelTemplate);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        LabelTemplate labelTemplate = labelTemplateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Label template not found. id: " + id));
        labelTemplateRepository.delete(labelTemplate);
    }

}
