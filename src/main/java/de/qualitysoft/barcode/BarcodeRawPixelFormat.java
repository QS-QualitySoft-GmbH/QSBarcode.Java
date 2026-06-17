package de.qualitysoft.barcode;

/** Raw pixel formats accepted by the Java wrapper. Non-Gray8 inputs are converted before native scanning. */
public enum BarcodeRawPixelFormat {
    GRAY8(1),
    RGB24(3),
    BGR24(3),
    RGBA32(4),
    BGRA32(4);

    private final int bytesPerPixel;

    BarcodeRawPixelFormat(int bytesPerPixel) {
        this.bytesPerPixel = bytesPerPixel;
    }

    public int bytesPerPixel() {
        return bytesPerPixel;
    }
}
