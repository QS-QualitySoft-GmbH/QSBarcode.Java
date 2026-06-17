package de.qualitysoft.barcode;

import de.qualitysoft.barcode.internal.NativeRuntime;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NativeRuntimeTest {
    @Test
    void runtimeIdentifierUsesSupportedRidShape() {
        String rid = NativeRuntime.runtimeIdentifier();

        assertTrue(rid.startsWith("win-") || rid.startsWith("linux-") || rid.startsWith("osx-"));
        assertFalse(rid.endsWith("-amd64"));
        assertFalse(rid.endsWith("-x86_64"));
    }

    @Test
    void diagnosticsAreAvailableBeforeNativeLoad() {
        assertTrue(BarcodeNativeLibrary.getDiagnostics().contains("Runtime identifier:"));
    }
}
