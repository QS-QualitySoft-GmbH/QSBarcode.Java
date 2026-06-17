package de.qualitysoft.barcode;

import de.qualitysoft.barcode.internal.NativeApi;
import de.qualitysoft.barcode.internal.NativeRuntime;

import java.util.Optional;

/** Native runtime diagnostics for deployments and health checks. */
public final class BarcodeNativeLibrary {
    private BarcodeNativeLibrary() {
    }

    public static String getVersion() {
        return NativeRuntime.pointerString(api().qsbc_loader_abi_version_string());
    }

    public static int getVersionMajor() {
        return api().qsbc_loader_abi_version_major();
    }

    public static int getVersionMinor() {
        return api().qsbc_loader_abi_version_minor();
    }

    public static int getVersionPatch() {
        return api().qsbc_loader_abi_version_patch();
    }

    public static String getEngineVersion() {
        return NativeRuntime.pointerString(api().qsbc_loader_engine_version_string());
    }

    public static int getCapabilities() {
        return api().qsbc_loader_capabilities();
    }

    public static boolean isFormatSupported(BarcodeImageFormat format) {
        return api().qsbc_loader_is_format_supported(format.nativeValue()) != 0;
    }

    public static String getFormatName(BarcodeImageFormat format) {
        return NativeRuntime.pointerString(api().qsbc_loader_format_name(format.nativeValue()));
    }

    public static String getStatusName(int status) {
        return NativeRuntime.pointerString(api().qsbc_loader_status_name(status));
    }

    public static boolean isErrorStatus(int status) {
        return api().qsbc_loader_status_is_error(status) != 0;
    }

    public static BarcodeLicenseStatus getLicenseStatus() {
        return new BarcodeLicenseStatus(api().qsbc_loader_license_status());
    }

    public static BarcodeLicenseStatus getLicenseStatus(String licenseFile) {
        return new BarcodeLicenseStatus(api().qsbc_loader_license_status_file(NativeRuntime.toNullTerminatedUtf8(licenseFile)));
    }

    public static Optional<String> tryGetVersion() {
        try {
            return Optional.ofNullable(getVersion());
        } catch (RuntimeException ex) {
            return Optional.empty();
        }
    }

    public static Optional<String> tryGetEngineVersion() {
        try {
            return Optional.ofNullable(getEngineVersion());
        } catch (RuntimeException ex) {
            return Optional.empty();
        }
    }

    public static String getDiagnostics() {
        return NativeRuntime.diagnostics();
    }

    private static NativeApi api() {
        return NativeRuntime.api();
    }
}
