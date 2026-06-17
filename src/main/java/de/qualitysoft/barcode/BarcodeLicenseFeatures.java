package de.qualitysoft.barcode;

/** Native license feature bit masks. */
public final class BarcodeLicenseFeatures {
    public static final int DEMO = 0x00000001;
    public static final int LINEAR = 0x00000002;
    public static final int PDF417 = 0x00000004;
    public static final int DATAMATRIX = 0x00000008;
    public static final int QR = 0x00000010;
    public static final int AZTEC = 0x00000020;

    private BarcodeLicenseFeatures() {
    }
}
