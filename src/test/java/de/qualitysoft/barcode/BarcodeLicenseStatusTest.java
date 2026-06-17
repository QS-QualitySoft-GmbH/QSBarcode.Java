package de.qualitysoft.barcode;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BarcodeLicenseStatusTest {
    @Test
    void reportsAllowedAndMissingFeatures() {
        BarcodeLicenseStatus status = new BarcodeLicenseStatus(
                BarcodeLicenseFeatures.DEMO
                        | BarcodeLicenseFeatures.LINEAR
                        | BarcodeLicenseFeatures.DATAMATRIX);

        int requested = BarcodeSymbology.CODE128 | BarcodeSymbology.DATAMATRIX | BarcodeSymbology.QR;
        int missing = status.missingFeaturesFor(requested);

        assertTrue(status.isDemo());
        assertTrue(status.allowsLinear());
        assertTrue(status.allowsDataMatrix());
        assertFalse(status.allowsQr());
        assertFalse(status.canScan(requested));
        assertEquals(BarcodeLicenseFeatures.QR, missing);
        assertEquals(1, status.missingFeatureListFor(requested).size());
        assertEquals(BarcodeLicenseFeatures.QR, status.missingFeatureListFor(requested).get(0));
    }

    @Test
    void exceptionCarriesLicenseDiagnostics() {
        BarcodeLicenseStatus status = new BarcodeLicenseStatus(BarcodeLicenseFeatures.LINEAR);
        BarcodeScanException exception = new BarcodeScanException(
                de.qualitysoft.barcode.internal.NativeApi.QSBC_ERROR_LICENSE_REQUIRED,
                "license_required",
                BarcodeSymbology.QR,
                status,
                status.missingFeaturesFor(BarcodeSymbology.QR));

        assertEquals(de.qualitysoft.barcode.internal.NativeApi.QSBC_ERROR_LICENSE_REQUIRED, exception.getStatusCode());
        assertEquals("license_required", exception.getStatusName());
        assertEquals(BarcodeSymbology.QR, exception.getRequestedSymbologies());
        assertEquals(status, exception.getLicenseStatus());
        assertEquals(BarcodeLicenseFeatures.QR, exception.getMissingLicenseFeatures());
        assertTrue(exception.getMessage().contains("requires additional license features"));
    }
}
