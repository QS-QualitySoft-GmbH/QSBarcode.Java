package de.qualitysoft.barcode;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NativeSmokeTest {
    @Test
    void nativeVersionCanBeReadWhenRuntimeAssetsAreAvailable() {
        Optional<String> version = BarcodeNativeLibrary.tryGetVersion();
        Assumptions.assumeTrue(version.isPresent(), BarcodeNativeLibrary.getDiagnostics());

        assertNotNull(version.get());
        assertTrue(BarcodeNativeLibrary.getVersionMajor() >= 1);
    }

    @Test
    void detectFormatFromFileBytesAndDirectBuffer() throws Exception {
        Path fixture = findFixture();
        Assumptions.assumeTrue(fixture != null, "No Java SDK fixture found.");
        Assumptions.assumeTrue(BarcodeNativeLibrary.tryGetVersion().isPresent(), BarcodeNativeLibrary.getDiagnostics());

        byte[] bytes = Files.readAllBytes(fixture);
        try (BarcodeReader reader = new BarcodeReader(new BarcodeReaderSettings())) {
            BarcodeImageFormat fileFormat = reader.detectFormat(fixture);
            BarcodeImageFormat memoryFormat = reader.detectFormat(bytes);
            ByteBuffer direct = ByteBuffer.allocateDirect(bytes.length);
            direct.put(bytes).flip();
            List<BarcodeResult> ignored = reader.read(direct, new BarcodeReaderOptions()
                    .setSymbologies(BarcodeSymbology.QR)
                    .setScanTimeoutMs(5_000));

            assertTrue(fileFormat != BarcodeImageFormat.UNKNOWN);
            assertTrue(memoryFormat != BarcodeImageFormat.UNKNOWN);
            assertNotNull(ignored);
        }
    }

    @Test
    void asyncScansReturnStableResults() throws Exception {
        Path fixture = findFixture();
        Assumptions.assumeTrue(fixture != null, "No Java SDK fixture found.");
        Assumptions.assumeTrue(BarcodeNativeLibrary.tryGetVersion().isPresent(), BarcodeNativeLibrary.getDiagnostics());

        byte[] bytes = Files.readAllBytes(fixture);
        try (BarcodeReader reader = new BarcodeReader()) {
            BarcodeReaderOptions options = new BarcodeReaderOptions()
                    .setSymbologies(BarcodeSymbology.QR)
                    .setScanTimeoutMs(5_000);
            CompletableFuture<List<BarcodeResult>> first = reader.readAsync(bytes, options);
            CompletableFuture<List<BarcodeResult>> second = reader.readAsync(bytes, options);

            assertNotNull(first.get(30, TimeUnit.SECONDS));
            assertNotNull(second.get(30, TimeUnit.SECONDS));
        }
    }

    private static Path findFixture() {
        Path[] candidates = new Path[] {
                Paths.get("src/test/resources/fixtures/barcode.png"),
                Paths.get("../../examples/QR_Codes.jpg"),
                Paths.get("../../examples/BarTest.tif"),
                Paths.get("../../examples/AdobeTest.pdf")
        };
        for (Path candidate : candidates) {
            if (Files.isRegularFile(candidate)) {
                return candidate;
            }
        }
        return null;
    }
}
