package com.kulturman.irembotest.domain.application.entities;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kulturman.irembotest.domain.application.Variable;
import com.kulturman.irembotest.domain.entities.CertificateStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Certificate {
    private UUID id;
    private Template template;
    private String variables;
    private CertificateStatus status;
    private UUID tenantId;


    public List<Variable> getVariablesAsList() throws JsonProcessingException {
        var objectMapper = new ObjectMapper();
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(getVariables(), new TypeReference<List<Variable>>() {});
    }
}
