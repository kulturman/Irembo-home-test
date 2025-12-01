package com.kulturman.irembotest.infrastructure.storage;

import com.kulturman.irembotest.infrastructure.pdf.OpenHtmlToPdfGenerator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FileSystemStorageTest {
    private FileSystemStorage storage;
    private OpenHtmlToPdfGenerator pdfGenerator;
    private String testBaseDirectory;
    private List<String> createdFilePaths;

    @BeforeEach
    void setUp() {
        testBaseDirectory = "certificates/test-storage-" + System.currentTimeMillis();
        storage = new FileSystemStorage(testBaseDirectory);
        pdfGenerator = new OpenHtmlToPdfGenerator();
        createdFilePaths = new ArrayList<>();
    }

    @AfterEach
    void tearDown() throws IOException {
        for (String filePath : createdFilePaths) {
            Files.deleteIfExists(Paths.get(filePath));
        }
        deleteDirectoryRecursively(Paths.get(testBaseDirectory));
    }

    private void deleteDirectoryRecursively(Path directory) throws IOException {
        if (Files.exists(directory)) {
            Files.walk(directory)
                .sorted((a, b) -> b.compareTo(a))
                .forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
        }
    }

    @Test
    void shouldStoreAndVerifyRealPdfFile() throws IOException {
        String html = "<h1>Test Certificate</h1><p>Integration test for file storage</p>";
        byte[] pdfBytes = pdfGenerator.generatePdf(html);

        String storedPath = storage.store("test-certificate.pdf", pdfBytes);
        createdFilePaths.add(storedPath);

        Path path = Paths.get(storedPath);
        assertTrue(Files.exists(path));

        byte[] readBytes = Files.readAllBytes(path);
        assertArrayEquals(pdfBytes, readBytes);

        assertArrayEquals(
            new byte[]{0x25, 0x50, 0x44, 0x46},
            new byte[]{readBytes[0], readBytes[1], readBytes[2], readBytes[3]}
        );
    }

    @Test
    void shouldCreateParentDirectoriesIfNeeded() throws IOException {
        String html = "<h1>Nested Test</h1>";
        byte[] pdfBytes = pdfGenerator.generatePdf(html);

        String storedPath = storage.store("2024/12/certificate.pdf", pdfBytes);
        createdFilePaths.add(storedPath);

        assertTrue(Files.exists(Paths.get(storedPath)));

        Path parentDir = Paths.get(storedPath).getParent();
        assertTrue(Files.isDirectory(parentDir));
    }

    @Test
    void shouldOverwriteExistingFile() throws IOException {
        String html1 = "<h1>First Version</h1>";
        String html2 = "<h1>Second Version</h1>";

        byte[] pdf1 = pdfGenerator.generatePdf(html1);
        byte[] pdf2 = pdfGenerator.generatePdf(html2);

        String path1 = storage.store("overwrite-test.pdf", pdf1);
        createdFilePaths.add(path1);

        String path2 = storage.store("overwrite-test.pdf", pdf2);

        assertEquals(path1, path2);

        byte[] readBytes = Files.readAllBytes(Paths.get(path2));
        assertArrayEquals(pdf2, readBytes);
    }
}
