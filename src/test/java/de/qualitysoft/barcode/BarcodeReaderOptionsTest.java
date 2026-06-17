package de.qualitysoft.barcode;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

class BarcodeReaderOptionsTest {
    @Test
    void defaultOptionsMatchNativeDefaults() {
        BarcodeReaderOptions options = new BarcodeReaderOptions();

        assertEquals(BarcodeSymbology.NATIVE_DEFAULT, options.getSymbologies());
        assertEquals(1, options.getMinLength());
        assertEquals(-1, options.getPageStart());
        assertEquals(0, options.getPageCount());
        assertEquals(0, options.getScanTimeoutMs());
    }

    @Test
    void validatesPageRangeAndTimeout() {
        assertThrows(IllegalArgumentException.class, () -> new BarcodeReaderOptions().setPageStart(-2).validate());
        assertThrows(IllegalArgumentException.class, () -> new BarcodeReaderOptions().setPageCount(-1).validate());
        assertThrows(IllegalArgumentException.class, () -> new BarcodeReaderOptions().setScanTimeoutMs(-1).validate());
    }

    @Test
    void readerSettingsCloneDetachesDefaultOptions() {
        BarcodeReaderSettings settings = new BarcodeReaderSettings()
                .setDefaultOptions(new BarcodeReaderOptions().setDpi(300));

        BarcodeReaderSettings clone = settings.clone();
        settings.getDefaultOptions().setDpi(200);

        assertEquals(300, clone.getDefaultOptions().getDpi());
        assertNotSame(settings.getDefaultOptions(), clone.getDefaultOptions());
    }

    @Test
    void validatesReaderSettings() {
        assertThrows(NullPointerException.class, () -> new BarcodeReaderSettings().setDefaultOptions(null));
    }
}
