package com.kulturman.irembotest.infrastructure.storage;

import com.kulturman.irembotest.domain.exceptions.FileStorageException;
import com.kulturman.irembotest.domain.ports.FileStorage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

@Component
public class FileSystemStorage implements FileStorage {
    private final String baseDirectory;

    public FileSystemStorage(@Value("${file.storage.base-directory:./storage}") String baseDirectory) {
        this.baseDirectory = baseDirectory;
        createBaseDirectoryIfNotExists();
    }

    @Override
    public String store(String fileName, byte[] content) throws IOException {
        Path filePath = Paths.get(baseDirectory, fileName);

        Files.createDirectories(filePath.getParent());
        Files.write(filePath, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        return filePath.toAbsolutePath().toString();
    }

    @Override
    public byte[] retrieve(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        return Files.readAllBytes(path);
    }

    private void createBaseDirectoryIfNotExists() {
        try {
            Files.createDirectories(Paths.get(baseDirectory));
        } catch (IOException e) {
            throw new FileStorageException("Failed to create base directory: " + baseDirectory, e);
        }
    }
}
