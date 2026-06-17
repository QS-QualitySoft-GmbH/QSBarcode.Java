package de.qualitysoft.barcode.internal;

import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

public class NativeImageBuffer extends Structure {
    public Pointer data;
    public NativeLong len;
    public int width;
    public int height;
    public int format;
    public int page_index;
    public int reserved0;
    public int reserved1;

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList(
                "data",
                "len",
                "width",
                "height",
                "format",
                "page_index",
                "reserved0",
                "reserved1");
    }
}
