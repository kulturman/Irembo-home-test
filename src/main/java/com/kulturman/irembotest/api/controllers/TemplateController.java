package com.kulturman.irembotest.api.controllers;

import com.kulturman.irembotest.api.dto.ResourceId;
import com.kulturman.irembotest.api.dto.TemplateResponse;
import com.kulturman.irembotest.domain.application.CreateTemplateRequest;
import com.kulturman.irembotest.domain.application.TemplateService;
import com.kulturman.irembotest.domain.application.UpdateTemplateRequest;
import com.kulturman.irembotest.domain.entities.Template;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/templates")
@AllArgsConstructor
public class TemplateController {
    private final TemplateService templateService;

    @GetMapping
    public ResponseEntity<Page<TemplateResponse>> getTemplates(Pageable pageable) {
        return ResponseEntity.ok(templateService.getTemplates(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TemplateResponse> getTemplate(@PathVariable UUID id) {
        return ResponseEntity.ok(templateService.getTemplate(id));
    }

    @PostMapping
    public ResponseEntity<ResourceId> createTemplate(@Valid @RequestBody CreateTemplateRequest request) {
        Template template = templateService.createTemplate(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ResourceId(template.getId().toString()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateTemplate(@PathVariable UUID id, @Valid @RequestBody UpdateTemplateRequest request) {
        templateService.updateTemplate(id, request);
        return ResponseEntity.noContent().build();
    }
}
