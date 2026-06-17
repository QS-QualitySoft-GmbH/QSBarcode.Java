package de.qualitysoft.barcode;

/** Native barcode symbology bit masks. */
public final class BarcodeSymbology {
    public static final int NATIVE_DEFAULT = 0;
    public static final int CODE128 = 0x00000001;
    public static final int EAN128 = 0x00000002;
    public static final int CODE39 = 0x00000004;
    public static final int CODE39_EXT = 0x00000008;
    public static final int CODE32 = 0x00000010;
    public static final int CODE11 = 0x00000020;
    public static final int I25 = 0x00000040;
    public static final int INDUSTRIAL25 = 0x00000080;
    public static final int IATA25 = 0x00000100;
    public static final int INVERTED25 = 0x00000200;
    public static final int MATRIX25 = 0x00000400;
    public static final int DATALOGIC25 = 0x00000800;
    public static final int BCD_MATRIX25 = 0x00001000;
    public static final int CODABAR = 0x00002000;
    public static final int CODE93 = 0x00004000;
    public static final int CODE93_EXT = 0x00008000;
    public static final int EAN8 = 0x00010000;
    public static final int EAN13 = 0x00020000;
    public static final int UPCA = 0x00040000;
    public static final int UPCE = 0x00080000;
    public static final int CODABLOCK = 0x00100000;
    public static final int DATABAR = 0x00200000;
    public static final int PHARMA = 0x00400000;
    public static final int PATCH = 0x00800000;
    public static final int DATABAR_OMNI = 0x01000000;
    public static final int DATABAR_EXPANDED = 0x02000000;
    public static final int DATABAR_LIMITED = 0x04000000;
    public static final int DATAMATRIX = 0x08000000;
    public static final int QR = 0x10000000;
    public static final int AZTEC = 0x20000000;
    public static final int PDF417 = 0x40000000;
    public static final int POSTAL = 0x80000000;
    public static final int TWO_D = DATAMATRIX | QR | AZTEC | PDF417;
    public static final int ALL = -1;

    private BarcodeSymbology() {
    }
}
