package de.qualitysoft.barcode;

import java.util.Objects;

/** Barcode bounding box in source image coordinates. */
public final class BarcodeBounds {
    private final int x;
    private final int y;
    private final int width;
    private final int height;

    public BarcodeBounds(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof BarcodeBounds)) {
            return false;
        }
        BarcodeBounds that = (BarcodeBounds) other;
        return x == that.x && y == that.y && width == that.width && height == that.height;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, width, height);
    }
}
