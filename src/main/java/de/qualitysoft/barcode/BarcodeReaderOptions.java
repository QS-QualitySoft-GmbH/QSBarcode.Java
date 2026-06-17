package de.qualitysoft.barcode;

import java.nio.charset.Charset;

/** Options for native barcode scanning and rendering. Zero-valued numeric fields let the native SDK use defaults. */
public final class BarcodeReaderOptions implements Cloneable {
    private int symbologies = BarcodeSymbology.NATIVE_DEFAULT;
    private int minLength = 1;
    private int flags;
    private int pageStart = -1;
    private int pageCount;
    private int dpi;
    private int dataMatrixFinderAngleTolerance;
    private int dataMatrixOverlapPercent;
    private int dataMatrixMaxLineCandidates;
    private int threshold;
    private int orientation = BarcodeOrientation.NATIVE_DEFAULT;
    private int maxSkewDegrees;
    private int lightMargin;
    private int scanDistanceBarcode;
    private int tolerance;
    private int minHeight;
    private int percent;
    private int scanDistance;
    private int maxGap;
    private int maxHeight;
    private int checksumFlags;
    private int scanTimeoutMs;
    private Charset textEncoding;

    public int getSymbologies() {
        return symbologies;
    }

    public BarcodeReaderOptions setSymbologies(int symbologies) {
        this.symbologies = symbologies;
        return this;
    }

    public int getMinLength() {
        return minLength;
    }

    public BarcodeReaderOptions setMinLength(int minLength) {
        this.minLength = minLength;
        return this;
    }

    public int getFlags() {
        return flags;
    }

    public BarcodeReaderOptions setFlags(int flags) {
        this.flags = flags;
        return this;
    }

    public int getPageStart() {
        return pageStart;
    }

    public BarcodeReaderOptions setPageStart(int pageStart) {
        this.pageStart = pageStart;
        return this;
    }

    public int getPageCount() {
        return pageCount;
    }

    public BarcodeReaderOptions setPageCount(int pageCount) {
        this.pageCount = pageCount;
        return this;
    }

    public int getDpi() {
        return dpi;
    }

    public BarcodeReaderOptions setDpi(int dpi) {
        this.dpi = dpi;
        return this;
    }

    public int getDataMatrixFinderAngleTolerance() {
        return dataMatrixFinderAngleTolerance;
    }

    public BarcodeReaderOptions setDataMatrixFinderAngleTolerance(int value) {
        this.dataMatrixFinderAngleTolerance = value;
        return this;
    }

    public int getDataMatrixOverlapPercent() {
        return dataMatrixOverlapPercent;
    }

    public BarcodeReaderOptions setDataMatrixOverlapPercent(int value) {
        this.dataMatrixOverlapPercent = value;
        return this;
    }

    public int getDataMatrixMaxLineCandidates() {
        return dataMatrixMaxLineCandidates;
    }

    public BarcodeReaderOptions setDataMatrixMaxLineCandidates(int value) {
        this.dataMatrixMaxLineCandidates = value;
        return this;
    }

    public int getThreshold() {
        return threshold;
    }

    public BarcodeReaderOptions setThreshold(int threshold) {
        this.threshold = threshold;
        return this;
    }

    public int getOrientation() {
        return orientation;
    }

    public BarcodeReaderOptions setOrientation(int orientation) {
        this.orientation = orientation;
        return this;
    }

    public int getMaxSkewDegrees() {
        return maxSkewDegrees;
    }

    public BarcodeReaderOptions setMaxSkewDegrees(int value) {
        this.maxSkewDegrees = value;
        return this;
    }

    public int getLightMargin() {
        return lightMargin;
    }

    public BarcodeReaderOptions setLightMargin(int value) {
        this.lightMargin = value;
        return this;
    }

    public int getScanDistanceBarcode() {
        return scanDistanceBarcode;
    }

    public BarcodeReaderOptions setScanDistanceBarcode(int value) {
        this.scanDistanceBarcode = value;
        return this;
    }

    public int getTolerance() {
        return tolerance;
    }

    public BarcodeReaderOptions setTolerance(int tolerance) {
        this.tolerance = tolerance;
        return this;
    }

    public int getMinHeight() {
        return minHeight;
    }

    public BarcodeReaderOptions setMinHeight(int minHeight) {
        this.minHeight = minHeight;
        return this;
    }

    public int getPercent() {
        return percent;
    }

    public BarcodeReaderOptions setPercent(int percent) {
        this.percent = percent;
        return this;
    }

    public int getScanDistance() {
        return scanDistance;
    }

    public BarcodeReaderOptions setScanDistance(int scanDistance) {
        this.scanDistance = scanDistance;
        return this;
    }

    public int getMaxGap() {
        return maxGap;
    }

    public BarcodeReaderOptions setMaxGap(int maxGap) {
        this.maxGap = maxGap;
        return this;
    }

    public int getMaxHeight() {
        return maxHeight;
    }

    public BarcodeReaderOptions setMaxHeight(int maxHeight) {
        this.maxHeight = maxHeight;
        return this;
    }

    public int getChecksumFlags() {
        return checksumFlags;
    }

    public BarcodeReaderOptions setChecksumFlags(int checksumFlags) {
        this.checksumFlags = checksumFlags;
        return this;
    }

    public int getScanTimeoutMs() {
        return scanTimeoutMs;
    }

    public BarcodeReaderOptions setScanTimeoutMs(int scanTimeoutMs) {
        this.scanTimeoutMs = scanTimeoutMs;
        return this;
    }

    public Charset getTextEncoding() {
        return textEncoding;
    }

    public BarcodeReaderOptions setTextEncoding(Charset textEncoding) {
        this.textEncoding = textEncoding;
        return this;
    }

    public void validate() {
        if (pageStart < -1) {
            throw new IllegalArgumentException("pageStart must be -1 or a zero-based page index.");
        }
        if (pageCount < 0) {
            throw new IllegalArgumentException("pageCount must be zero for all pages or a positive count.");
        }
        if (scanTimeoutMs < 0) {
            throw new IllegalArgumentException("scanTimeoutMs must be zero or positive.");
        }
    }

    @Override
    public BarcodeReaderOptions clone() {
        try {
            return (BarcodeReaderOptions) super.clone();
        } catch (CloneNotSupportedException ex) {
            throw new AssertionError(ex);
        }
    }
}
