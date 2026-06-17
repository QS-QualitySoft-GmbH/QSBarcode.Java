package de.qualitysoft.barcode.internal;

import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;

public interface NativeApi extends Library {
    int QSBC_STATUS_MISS = 0;
    int QSBC_STATUS_HIT = 1;
    int QSBC_ERROR_LICENSE_REQUIRED = -6;

    int qsbc_loader_license_status();

    int qsbc_loader_license_status_file(byte[] licenseFile);

    int qsbc_loader_abi_version_major();

    int qsbc_loader_abi_version_minor();

    int qsbc_loader_abi_version_patch();

    Pointer qsbc_loader_abi_version_string();

    Pointer qsbc_loader_engine_version_string();

    int qsbc_loader_capabilities();

    int qsbc_loader_is_format_supported(int format);

    int qsbc_loader_status_is_error(int status);

    Pointer qsbc_loader_format_name(int format);

    Pointer qsbc_loader_status_name(int status);

    int qsbc_loader_scan_options_init(NativeScanOptions options);

    int qsbc_loader_detect_file_format(byte[] path);

    int qsbc_loader_detect_image_format(Pointer bytes, NativeLong byteLen);

    int qsbc_loader_page_count_file(byte[] path);

    int qsbc_loader_page_count_image_memory(Pointer bytes, NativeLong byteLen);

    int qsbc_loader_scan_file_cb_with_options(byte[] path, NativeScanOptions options, ResultCallback callback, Pointer userData);

    int qsbc_loader_scan_image_memory_cb_with_options(Pointer bytes, NativeLong byteLen, NativeScanOptions options, ResultCallback callback, Pointer userData);

    int qsbc_loader_scan_gray8_cb_with_options(Pointer pixels, int width, int height, int stride, NativeScanOptions options, ResultCallback callback, Pointer userData);

    int qsbc_loader_render_file_page_bmp_with_options(byte[] path, NativeScanOptions options, NativeImageBuffer output);

    int qsbc_loader_render_image_memory_page_bmp_with_options(Pointer bytes, NativeLong byteLen, NativeScanOptions options, NativeImageBuffer output);

    int qsbc_loader_render_file_page_gray8_with_options(byte[] path, NativeScanOptions options, NativeImageBuffer output);

    int qsbc_loader_render_image_memory_page_gray8_with_options(Pointer bytes, NativeLong byteLen, NativeScanOptions options, NativeImageBuffer output);

    void qsbc_loader_free_image_buffer(NativeImageBuffer buffer);

    interface ResultCallback extends Callback {
        int invoke(Pointer result, Pointer userData);
    }
}
