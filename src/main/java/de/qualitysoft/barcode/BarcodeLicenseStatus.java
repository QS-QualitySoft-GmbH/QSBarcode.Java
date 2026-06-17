package de.qualitysoft.barcode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Interpreted native license status. */
public final class BarcodeLicenseStatus {
    private final int nativeValue;

    public BarcodeLicenseStatus(int nativeValue) {
        this.nativeValue = nativeValue;
    }

    public int nativeValue() {
        return nativeValue;
    }

    public boolean hasFeature(int featureMask) {
        return (nativeValue & featureMask) != 0;
    }

    public boolean isDemo() {
        return hasFeature(BarcodeLicenseFeatures.DEMO);
    }

    public boolean allowsLinear() {
        return hasFeature(BarcodeLicenseFeatures.LINEAR);
    }

    public boolean allowsPdf417() {
        return hasFeature(BarcodeLicenseFeatures.PDF417);
    }

    public boolean allowsDataMatrix() {
        return hasFeature(BarcodeLicenseFeatures.DATAMATRIX);
    }

    public boolean allowsQr() {
        return hasFeature(BarcodeLicenseFeatures.QR);
    }

    public boolean allowsAztec() {
        return hasFeature(BarcodeLicenseFeatures.AZTEC);
    }

    public boolean canScan(int symbologies) {
        return missingFeaturesFor(symbologies) == 0;
    }

    public int missingFeaturesFor(int symbologies) {
        int requested = symbologies == BarcodeSymbology.NATIVE_DEFAULT ? BarcodeSymbology.ALL : symbologies;
        int missing = 0;

        if ((requested & ~(BarcodeSymbology.TWO_D)) != 0 && !allowsLinear()) {
            missing |= BarcodeLicenseFeatures.LINEAR;
        }
        if ((requested & BarcodeSymbology.PDF417) != 0 && !allowsPdf417()) {
            missing |= BarcodeLicenseFeatures.PDF417;
        }
        if ((requested & BarcodeSymbology.DATAMATRIX) != 0 && !allowsDataMatrix()) {
            missing |= BarcodeLicenseFeatures.DATAMATRIX;
        }
        if ((requested & BarcodeSymbology.QR) != 0 && !allowsQr()) {
            missing |= BarcodeLicenseFeatures.QR;
        }
        if ((requested & BarcodeSymbology.AZTEC) != 0 && !allowsAztec()) {
            missing |= BarcodeLicenseFeatures.AZTEC;
        }

        return missing;
    }

    public List<Integer> missingFeatureListFor(int symbologies) {
        int missing = missingFeaturesFor(symbologies);
        if (missing == 0) {
            return Collections.emptyList();
        }

        List<Integer> result = new ArrayList<>(5);
        addIfMissing(result, missing, BarcodeLicenseFeatures.LINEAR);
        addIfMissing(result, missing, BarcodeLicenseFeatures.PDF417);
        addIfMissing(result, missing, BarcodeLicenseFeatures.DATAMATRIX);
        addIfMissing(result, missing, BarcodeLicenseFeatures.QR);
        addIfMissing(result, missing, BarcodeLicenseFeatures.AZTEC);
        return Collections.unmodifiableList(result);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("0x").append(Integer.toHexString(nativeValue)).append(" [");
        int appended = 0;
        appended += appendFeature(builder, appended, BarcodeLicenseFeatures.DEMO, "Demo");
        appended += appendFeature(builder, appended, BarcodeLicenseFeatures.LINEAR, "Linear");
        appended += appendFeature(builder, appended, BarcodeLicenseFeatures.PDF417, "PDF417");
        appended += appendFeature(builder, appended, BarcodeLicenseFeatures.DATAMATRIX, "DataMatrix");
        appended += appendFeature(builder, appended, BarcodeLicenseFeatures.QR, "QR");
        appended += appendFeature(builder, appended, BarcodeLicenseFeatures.AZTEC, "Aztec");
        if (appended == 0) {
            builder.append("None");
        }
        return builder.append(']').toString();
    }

    private int appendFeature(StringBuilder builder, int appended, int featureMask, String name) {
        if (!hasFeature(featureMask)) {
            return 0;
        }
        if (appended > 0) {
            builder.append(", ");
        }
        builder.append(name);
        return 1;
    }

    private static void addIfMissing(List<Integer> result, int missing, int featureMask) {
        if ((missing & featureMask) == featureMask) {
            result.add(featureMask);
        }
    }
}
