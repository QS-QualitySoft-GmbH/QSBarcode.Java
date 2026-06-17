# QualitySoft Barcode Java SDK

Java 11 JNA wrapper for the native QS Barcode 6.0 SDK.

Public repository: `https://github.com/QS-QualitySoft-GmbH/QSBarcode.Java`

Maven coordinates:

```text
de.qualitysoft.barcode:qualitysoft-barcode:6.0.0
```

## Installation

Maven:

```xml
<dependency>
  <groupId>de.qualitysoft.barcode</groupId>
  <artifactId>qualitysoft-barcode</artifactId>
  <version>6.0.0</version>
</dependency>
```

Gradle:

```kotlin
implementation("de.qualitysoft.barcode:qualitysoft-barcode:6.0.0")
```

The official JAR is all-in-one. It contains the native QS Barcode loader and
PDFium assets for every supported desktop/server RID. At runtime the SDK
extracts only the matching RID into a versioned cache directory and loads the
native runtime from there.

PDFium runs in-process inside the native loader. The loader owns one dedicated
render thread and serializes PDF document open, page render and close work
through a bounded queue. The Java wrapper does not package or configure a
separate PDF render executable, and there is no PDF renderer warmup API in
6.0.0.

## Quick Start

```java
import de.qualitysoft.barcode.BarcodeReader;
import de.qualitysoft.barcode.BarcodeReaderOptions;
import de.qualitysoft.barcode.BarcodeSymbology;

try (BarcodeReader reader = new BarcodeReader()) {
    BarcodeReaderOptions options = new BarcodeReaderOptions()
            .setSymbologies(BarcodeSymbology.DATAMATRIX | BarcodeSymbology.QR)
            .setDpi(300)
            .setScanTimeoutMs(5_000);

    reader.read("invoice.pdf", options).forEach(result ->
            System.out.println(result.getText()));
}
```

The reader supports scans from `Path`, `File`, `byte[]`, heap/direct
`ByteBuffer`, `InputStream`, raw Gray8 and raw RGB/BGR/RGBA/BGRA pixel buffers.
It also exposes page count, format detection, capabilities, license status,
status/format names and PDF/page rendering helpers.

Async scans run on an internal native scan executor. `readAsync(byte[], ...)`
does not make an extra defensive input copy before queuing work; keep the byte
array valid and unchanged until the returned `CompletableFuture` completes. Use
`InputStream` when the wrapper should own a memory copy before scanning.

For performance-sensitive scans, set an explicit symbology mask instead of
relying on the native default. For example, use only
`BarcodeSymbology.DATAMATRIX | BarcodeSymbology.QR` when those are the only
expected formats. Wrapper/native comparisons should use the same bytes or raw
pixels and the same options.

## License File

The native runtime searches for `qsbc.lic` by default. For services,
containers and explicit deployments, set `QSBC_LICENSE_FILE` before the first
scan:

```bash
export QSBC_LICENSE_FILE=/etc/qualitysoft/qsbc.lic
```

`*.lic` is ignored by this repository and must not be committed.

Without a valid commercial license the runtime runs in demo/evaluation mode.
Results may still be returned, but decoded 1D and 2D values are deliberately
modified. Production code should check license status and reject demo mode.

```java
import de.qualitysoft.barcode.BarcodeNativeLibrary;

var status = BarcodeNativeLibrary.getLicenseStatus();
if (status.isDemo()) {
    throw new IllegalStateException("QS Barcode is running in demo mode.");
}
```

## Native Runtime Override

For custom deployments, set `qualitysoft.barcode.native.path` to either a
directory containing the runtime files or the full loader path:

```bash
java -Dqualitysoft.barcode.native.path=/opt/qsbarcode/native ...
```

When a directory is used, the loader and PDFium must be siblings.

The native ABI for this release is 2.0.0. Old external PDF renderer path and
warmup configuration is not supported; remove it from applications when
upgrading.

## Supported Platforms

- `win-x86`
- `win-x64`
- `win-arm64`
- `linux-x64`
- `linux-arm64`
- `osx-x64`
- `osx-arm64`

Windows and macOS native assets are platform-signed before release packaging.
Linux assets are covered by SHA256 manifests and the GPG-signed Maven
artifacts.

## Build From Source

Wrapper build:

```powershell
mvn -f sdk/java/pom.xml test
```

Local install:

```powershell
mvn -f sdk/java/pom.xml -DskipTests install
```

In the standalone public repository, run the same commands from the repository
root:

```powershell
mvn test
mvn -DskipTests install
```

The public repository does not store native binaries in git. Release builds
inject signed runtime assets under `native/<rid>/` before packaging.

## Release Packaging

The release build runs from the private monorepo:

```powershell
powershell -ExecutionPolicy Bypass -File sdk/build-java-artifacts.ps1 `
  -Version 6.0.0 `
  -SignNative `
  -RequireNativeSigning `
  -SignMavenArtifacts `
  -RequireMavenSigning
```

Publishing uses Maven Central Portal credentials configured under server id
`central`:

```powershell
powershell -ExecutionPolicy Bypass -File sdk/publish-maven.ps1 `
  -Version 6.0.0 `
  -SignNative `
  -RequireNativeSigning
```

Maven Central requires PGP signatures for the POM, main JAR, sources JAR and
Javadoc JAR.

## Repository Scope

This public repository contains the Java wrapper, tests, package metadata,
documentation and native asset placeholders. It does not contain proprietary
native engine source, signing material, customer license files or internal demo
applications.
