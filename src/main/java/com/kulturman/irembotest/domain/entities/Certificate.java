package com.kulturman.irembotest.domain.entities;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Map;
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

    public Map<String, String> getVariablesAsMap() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(getVariables(), new TypeReference<Map<String, String>>() {});
    }

    public String replaceVariables() {
        var templateContent = template.getContent();
        try {
            var variablesMap = getVariablesAsMap();

            for (Map.Entry<String, String> entry : variablesMap.entrySet()) {
                templateContent = templateContent.replace("{" + entry.getKey() + "}", entry.getValue());
            }

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return templateContent;
    }
}
