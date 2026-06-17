package de.qualitysoft.barcode;

/** Optional engine flags passed to the native scanner. */
public final class BarcodeScanFlags {
    public static final int NONE = 0;
    public static final int DM_REPORT_SYMBOL_IDENTIFIER = 1 << 0;
    public static final int DM_SUPPRESS_ECI = 1 << 1;
    public static final int DM_INTENSIVE_SEARCH = 1 << 2;
    public static final int DM_SEARCH_ON_DOUBLED_REGION = 1 << 3;
    public static final int DM_ZEBRA_DOUBLING = 1 << 4;
    public static final int DM_TRY_ERODED_IMAGE = 1 << 5;
    public static final int QR_ECI = 1 << 6;
    public static final int QR_DOUBLE_IMAGE = 1 << 7;

    private BarcodeScanFlags() {
    }
}
