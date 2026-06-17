package de.qualitysoft.barcode;

/** Legacy orientation mask values. */
public final class BarcodeOrientation {
    public static final int NATIVE_DEFAULT = 0;
    public static final int DEGREE_0 = 1;
    public static final int DEGREE_90 = 2;
    public static final int DEGREE_180 = 4;
    public static final int DEGREE_270 = 8;
    public static final int ALL = DEGREE_0 | DEGREE_90 | DEGREE_180 | DEGREE_270;

    private BarcodeOrientation() {
    }
}
