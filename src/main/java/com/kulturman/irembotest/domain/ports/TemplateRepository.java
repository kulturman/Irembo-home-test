package com.kulturman.irembotest.domain.ports;

import com.kulturman.irembotest.domain.application.entities.Template;

public interface TemplateRepository {
    void save(Template template);
}
