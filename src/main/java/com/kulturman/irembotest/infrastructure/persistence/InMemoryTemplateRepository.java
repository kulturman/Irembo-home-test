package com.kulturman.irembotest.infrastructure.persistence;

import com.kulturman.irembotest.domain.application.entities.Template;
import com.kulturman.irembotest.domain.ports.TemplateRepository;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Getter
public class InMemoryTemplateRepository implements TemplateRepository {
    public final List<Template> savedTemplates = new ArrayList<>();

    @Override
    public void save(Template template) {
        savedTemplates.add(template);
    }
}
