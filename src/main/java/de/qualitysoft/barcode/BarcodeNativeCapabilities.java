package de.qualitysoft.barcode;

/** Native loader capability bit masks. */
public final class BarcodeNativeCapabilities {
    public static final int MEMORY_SCAN = 1 << 0;
    public static final int FILE_SCAN = 1 << 1;
    public static final int CALLBACK_SCAN = 1 << 2;
    public static final int MULTIPAGE_SCAN = 1 << 3;
    public static final int FORMAT_GIF = 1 << 8;
    public static final int FORMAT_PNG = 1 << 9;
    public static final int FORMAT_BMP = 1 << 10;
    public static final int FORMAT_JPEG = 1 << 11;
    public static final int FORMAT_PDF = 1 << 12;
    public static final int FORMAT_TIFF = 1 << 13;

    private BarcodeNativeCapabilities() {
    }
}
