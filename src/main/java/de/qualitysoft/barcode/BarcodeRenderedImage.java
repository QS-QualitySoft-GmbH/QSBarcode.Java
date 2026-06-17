package de.qualitysoft.barcode;

/** Rendered native page image. */
public final class BarcodeRenderedImage {
    private final byte[] bytes;
    private final int width;
    private final int height;
    private final BarcodeImageFormat inputFormat;
    private final int pageIndex;
    private final BarcodeRenderedPixelFormat pixelFormat;
    private final int stride;

    public BarcodeRenderedImage(
            byte[] bytes,
            int width,
            int height,
            BarcodeImageFormat inputFormat,
            int pageIndex,
            BarcodeRenderedPixelFormat pixelFormat,
            int stride) {
        this.bytes = bytes.clone();
        this.width = width;
        this.height = height;
        this.inputFormat = inputFormat;
        this.pageIndex = pageIndex;
        this.pixelFormat = pixelFormat;
        this.stride = stride;
    }

    public byte[] getBytes() {
        return bytes.clone();
    }

    public byte[] getBmpBytes() {
        if (pixelFormat != BarcodeRenderedPixelFormat.BMP24) {
            throw new IllegalStateException("Rendered image is not BMP24.");
        }
        return getBytes();
    }

    public byte[] getPixels() {
        return getBytes();
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public BarcodeImageFormat getInputFormat() {
        return inputFormat;
    }

    public int getPageIndex() {
        return pageIndex;
    }

    public BarcodeRenderedPixelFormat getPixelFormat() {
        return pixelFormat;
    }

    public int getStride() {
        return stride;
    }
}
