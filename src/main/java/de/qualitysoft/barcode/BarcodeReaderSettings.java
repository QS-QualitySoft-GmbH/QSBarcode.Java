package de.qualitysoft.barcode;

/** Reader-level settings for managed defaults. */
public final class BarcodeReaderSettings implements Cloneable {
    private BarcodeReaderOptions defaultOptions = new BarcodeReaderOptions();

    public BarcodeReaderOptions getDefaultOptions() {
        return defaultOptions;
    }

    public BarcodeReaderSettings setDefaultOptions(BarcodeReaderOptions defaultOptions) {
        if (defaultOptions == null) {
            throw new NullPointerException("defaultOptions");
        }
        this.defaultOptions = defaultOptions;
        return this;
    }

    public void validate() {
        defaultOptions.validate();
    }

    @Override
    public BarcodeReaderSettings clone() {
        try {
            BarcodeReaderSettings clone = (BarcodeReaderSettings) super.clone();
            clone.defaultOptions = defaultOptions.clone();
            return clone;
        } catch (CloneNotSupportedException ex) {
            throw new AssertionError(ex);
        }
    }
}
