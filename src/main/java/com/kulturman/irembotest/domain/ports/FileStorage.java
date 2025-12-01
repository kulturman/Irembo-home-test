package com.kulturman.irembotest.domain.ports;

import java.io.IOException;

public interface FileStorage {
    String store(String fileName, byte[] content) throws IOException;
}
