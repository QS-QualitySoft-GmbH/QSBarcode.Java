package de.qualitysoft.barcode;

import java.util.Arrays;
import java.util.Optional;

/** One decoded barcode result. */
public final class BarcodeResult {
    private final BarcodeImageFormat imageFormat;
    private final int symbologyMask;
    private final String text;
    private final byte[] rawBytes;
    private final int width;
    private final int height;
    private final long imageIndex;
    private final int pageIndex;
    private final BarcodeBounds bounds;

    public BarcodeResult(
            BarcodeImageFormat imageFormat,
            int symbologyMask,
            String text,
            byte[] rawBytes,
            int width,
            int height,
            long imageIndex,
            int pageIndex,
            BarcodeBounds bounds) {
        this.imageFormat = imageFormat;
        this.symbologyMask = symbologyMask;
        this.text = text;
        this.rawBytes = rawBytes.clone();
        this.width = width;
        this.height = height;
        this.imageIndex = imageIndex;
        this.pageIndex = pageIndex;
        this.bounds = bounds;
    }

    public BarcodeImageFormat getImageFormat() {
        return imageFormat;
    }

    public int getSymbologyMask() {
        return symbologyMask;
    }

    public String getText() {
        return text;
    }

    public byte[] getRawBytes() {
        return rawBytes.clone();
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public long getImageIndex() {
        return imageIndex;
    }

    public int getPageIndex() {
        return pageIndex;
    }

    public Optional<BarcodeBounds> getBounds() {
        return Optional.ofNullable(bounds);
    }

    @Override
    public String toString() {
        return "BarcodeResult{"
                + "imageFormat=" + imageFormat
                + ", symbologyMask=" + symbologyMask
                + ", text='" + text + '\''
                + ", rawBytes=" + Arrays.toString(rawBytes)
                + ", width=" + width
                + ", height=" + height
                + ", imageIndex=" + imageIndex
                + ", pageIndex=" + pageIndex
                + '}';
    }
}
