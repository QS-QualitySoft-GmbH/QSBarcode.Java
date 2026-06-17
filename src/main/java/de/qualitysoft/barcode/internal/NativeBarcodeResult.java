package de.qualitysoft.barcode.internal;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

public class NativeBarcodeResult extends Structure {
    public int format;
    public int symbology_mask;
    public Pointer text;
    public int text_len;
    public int width;
    public int height;
    public int image_index;
    public int page_index;
    public int barcode_x;
    public int barcode_y;
    public int barcode_width;
    public int barcode_height;
    public int has_bounds;
    public int reserved0;
    public int reserved1;
    public int reserved2;

    public NativeBarcodeResult() {
    }

    public NativeBarcodeResult(Pointer pointer) {
        super(pointer);
        read();
    }

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList(
                "format",
                "symbology_mask",
                "text",
                "text_len",
                "width",
                "height",
                "image_index",
                "page_index",
                "barcode_x",
                "barcode_y",
                "barcode_width",
                "barcode_height",
                "has_bounds",
                "reserved0",
                "reserved1",
                "reserved2");
    }
}
