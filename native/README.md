# Native Runtime Assets

The public source repository keeps this directory as a placeholder only.
Runtime binaries are not committed to Git.

Official Maven packages include the matching QS Barcode native runtime assets
for the supported desktop/server platforms. Local source builds that need native
smoke tests can place runtime files under `native/<rid>/` or use the
`qualitysoft.barcode.native.path` JVM property.

Expected local layout:

```text
native/
  win-x86/qs_barcode_loader_sdk.dll
  win-x86/pdfium.dll
  win-x64/qs_barcode_loader_sdk.dll
  win-x64/pdfium.dll
  win-arm64/qs_barcode_loader_sdk.dll
  win-arm64/pdfium.dll
  linux-x64/libqs_barcode_loader_sdk.so
  linux-x64/libpdfium.so
  linux-arm64/libqs_barcode_loader_sdk.so
  linux-arm64/libpdfium.so
  osx-x64/libqs_barcode_loader_sdk.dylib
  osx-x64/libpdfium.dylib
  osx-arm64/libqs_barcode_loader_sdk.dylib
  osx-arm64/libpdfium.dylib
```
