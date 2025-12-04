package com.kulturman.irembotest.domain.application;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class CertificateDownload {
    private final byte[] pdfContent;
    private final UUID certificateId;
}
