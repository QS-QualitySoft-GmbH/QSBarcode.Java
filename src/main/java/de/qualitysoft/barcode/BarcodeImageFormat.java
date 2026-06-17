package de.qualitysoft.barcode;

/** Input document or image format detected by the native loader. */
public enum BarcodeImageFormat {
    UNKNOWN(0),
    GIF(1),
    PNG(2),
    BMP(3),
    JPEG(4),
    PDF(5),
    TIFF(6);

    private final int nativeValue;

    BarcodeImageFormat(int nativeValue) {
        this.nativeValue = nativeValue;
    }

    public int nativeValue() {
        return nativeValue;
    }

    public static BarcodeImageFormat fromNative(int value) {
        for (BarcodeImageFormat format : values()) {
            if (format.nativeValue == value) {
                return format;
            }
        }
        return UNKNOWN;
    }
}
