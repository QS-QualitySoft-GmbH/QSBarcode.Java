package de.qualitysoft.barcode.internal;

import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

public class NativeScanOptions extends Structure {
    public int struct_size;
    public int mask;
    public int min_length;
    public int reserved_result_limit;
    public int flags;
    public int page_start;
    public int page_count;
    public int dpi;
    public int reserved0;
    public int reserved1;
    public int reserved2;
    public int reserved3;
    public int threshold;
    public int orientation;
    public int max_skew_degrees;
    public int light_margin;
    public int scan_distance_barcode;
    public int tolerance;
    public int min_height;
    public int percent;
    public int scan_distance;
    public int max_gap;
    public int max_height;
    public int checksum_flags;
    public int scan_timeout_ms;

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList(
                "struct_size",
                "mask",
                "min_length",
                "reserved_result_limit",
                "flags",
                "page_start",
                "page_count",
                "dpi",
                "reserved0",
                "reserved1",
                "reserved2",
                "reserved3",
                "threshold",
                "orientation",
                "max_skew_degrees",
                "light_margin",
                "scan_distance_barcode",
                "tolerance",
                "min_height",
                "percent",
                "scan_distance",
                "max_gap",
                "max_height",
                "checksum_flags",
                "scan_timeout_ms");
    }
}
