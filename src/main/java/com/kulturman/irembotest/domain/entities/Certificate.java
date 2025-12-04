package com.kulturman.irembotest.domain.entities;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kulturman.irembotest.domain.application.Variable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "certificates")
@EntityListeners(AuditingEntityListener.class)
public class Certificate {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private Template template;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(columnDefinition = "TEXT")
    private String variables;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private CertificateStatus status;

    @Column(name = "file_path", length = 500)
    private String filePath;

    @Column(name = "download_token", unique = true, length = 64)
    private String downloadToken;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public List<Variable> getVariablesAsList() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(getVariables(), new TypeReference<List<Variable>>() {});
    }

    public String replaceVariables() {
        var templateContent = template.getContent();
        try {
            var variables = getVariablesAsList();

            for (Variable variable : variables) {
                templateContent = templateContent.replace("{" + variable.getKey() + "}", variable.getValue());
            }

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return templateContent;
    }
}
