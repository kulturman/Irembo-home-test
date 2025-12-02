package com.kulturman.irembotest.api.controllers;

import com.kulturman.irembotest.api.dto.TemplateResponse;
import com.kulturman.irembotest.domain.application.TemplateService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/templates")
@AllArgsConstructor
public class TemplateController {
    private final TemplateService templateService;

    @GetMapping
    public ResponseEntity<Page<TemplateResponse>> getTemplates(Pageable pageable) {
        return ResponseEntity.ok(templateService.getTemplates(pageable));
    }
}
