package de.qualitysoft.barcode;

/** Native scan or render failure. */
public class BarcodeScanException extends RuntimeException {
    private final int statusCode;
    private final String statusName;
    private final Integer requestedSymbologies;
    private final BarcodeLicenseStatus licenseStatus;
    private final int missingLicenseFeatures;

    public BarcodeScanException(int statusCode, String statusName) {
        this(statusCode, statusName, null, null, 0);
    }

    public BarcodeScanException(
            int statusCode,
            String statusName,
            Integer requestedSymbologies,
            BarcodeLicenseStatus licenseStatus,
            int missingLicenseFeatures) {
        super(createMessage(statusCode, statusName, requestedSymbologies, licenseStatus, missingLicenseFeatures));
        this.statusCode = statusCode;
        this.statusName = statusName;
        this.requestedSymbologies = requestedSymbologies;
        this.licenseStatus = licenseStatus;
        this.missingLicenseFeatures = missingLicenseFeatures;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getStatusName() {
        return statusName;
    }

    public Integer getRequestedSymbologies() {
        return requestedSymbologies;
    }

    public BarcodeLicenseStatus getLicenseStatus() {
        return licenseStatus;
    }

    public int getMissingLicenseFeatures() {
        return missingLicenseFeatures;
    }

    private static String createMessage(
            int statusCode,
            String statusName,
            Integer requestedSymbologies,
            BarcodeLicenseStatus licenseStatus,
            int missingLicenseFeatures) {
        if (statusCode == de.qualitysoft.barcode.internal.NativeApi.QSBC_ERROR_LICENSE_REQUIRED) {
            String requested = requestedSymbologies == null ? "unknown" : "0x" + Integer.toHexString(requestedSymbologies);
            String features = licenseStatus == null ? "unknown" : licenseStatus.toString();
            String missing = missingLicenseFeatures == 0 ? "unknown" : "0x" + Integer.toHexString(missingLicenseFeatures);
            return "Native barcode scan requires additional license features. Requested symbologies: "
                    + requested + ". Current license features: " + features + ". Missing features: " + missing + ".";
        }

        return "Native barcode operation failed with status " + statusCode + " (" + statusName + ").";
    }
}
