# Native Runtime Assets

Release builds place the signed native QS Barcode runtime assets here before
packaging the all-in-one Maven artifact.

Expected layout:

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

The public repository intentionally does not store runtime binaries. The release
build injects signed assets before packaging.
