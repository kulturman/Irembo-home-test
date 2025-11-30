package com.kulturman.irembotest.domain.application;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateTemplateRequest {
    public String name;
    public String content;
}
