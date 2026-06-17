package de.qualitysoft.barcode;

/** Thrown when the Java wrapper cannot load or call the native QS Barcode runtime. */
public class BarcodeNativeLibraryException extends RuntimeException {
    public BarcodeNativeLibraryException(String message) {
        super(message);
    }

    public BarcodeNativeLibraryException(String message, Throwable cause) {
        super(message, cause);
    }
}
