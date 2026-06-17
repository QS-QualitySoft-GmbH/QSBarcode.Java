package de.qualitysoft.barcode;

import de.qualitysoft.barcode.internal.NativeRuntime;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class PackagingLayoutTest {
    @Test
    void publicConstantsMatchNativeMasks() {
        assertEquals(0x08000000, BarcodeSymbology.DATAMATRIX);
        assertEquals(0x10000000, BarcodeSymbology.QR);
        assertEquals(0x20000000, BarcodeSymbology.AZTEC);
        assertEquals(0x40000000, BarcodeSymbology.PDF417);
        assertEquals(BarcodeSymbology.DATAMATRIX | BarcodeSymbology.QR | BarcodeSymbology.AZTEC | BarcodeSymbology.PDF417, BarcodeSymbology.TWO_D);
    }

    @Test
    void runtimeResourcesUseCurrentInProcessPdfiumLayout() {
        String rid = NativeRuntime.runtimeIdentifier();
        String loader = rid.startsWith("win-")
                ? "qs_barcode_loader_sdk.dll"
                : rid.startsWith("osx-") ? "libqs_barcode_loader_sdk.dylib" : "libqs_barcode_loader_sdk.so";
        String base = "de/qualitysoft/barcode/native/" + rid + "/";
        ClassLoader classLoader = PackagingLayoutTest.class.getClassLoader();

        assertNotNull(classLoader.getResource(base + loader));
        assertNull(classLoader.getResource(base + "qs_barcode_pdf_render_worker.exe"));
        assertNull(classLoader.getResource(base + "qs_barcode_pdf_render_worker"));
        assertNull(classLoader.getResource(base + "pdf_render_worker"));
    }
}
