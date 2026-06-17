# QualitySoft Barcode Java SDK

`qualitysoft-barcode` is the official Java wrapper for the QS Barcode native
engine. It gives Java applications a small API for barcode recognition while
the performance-critical scanning, image handling and license enforcement remain
in the native QS Barcode runtime.

The SDK is distributed as a Maven package:

```text
de.qualitysoft.barcode:qualitysoft-barcode:6.0.0
```

Version `6.0.0` is published on Maven Central:
`https://repo1.maven.org/maven2/de/qualitysoft/barcode/qualitysoft-barcode/6.0.0/`

## What It Does

The QS Barcode engine is built for document and image based barcode recognition
in business applications. It can scan barcodes from image files, image streams,
byte arrays, `ByteBuffer` input, raw pixel buffers and PDF documents.

Supported feature groups include:

- 1D / linear barcodes such as Code 128, Code 39, EAN/UPC, GS1-128 and related
  industrial formats
- PDF417
- Data Matrix
- QR Code
- Aztec
- multi-page PDF input
- cross-platform native execution on Windows, Linux and macOS
- license-aware feature checks for demo, evaluation and commercial deployments

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

The official JAR contains the Java wrapper and the native runtime assets for
the supported desktop/server platforms. At runtime the SDK loads the matching
platform runtime automatically.

No custom Maven repository is required for the published release; Maven Central
is used by default by Maven and Gradle builds.

Product page, pricing and documentation:
`https://qualitysoft.de/products/qs-barcode-sdk/`

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

## Public API Overview

The stable entry point is `BarcodeReader`.

| Area | API | Use |
| --- | --- | --- |
| Encoded file input | `read(String)`, `read(Path)`, `read(File)` | Scan supported image files and PDFs from disk. |
| Encoded memory input | `read(byte[])`, `read(ByteBuffer)` | Scan encoded image/PDF bytes already held by the application. |
| Stream input | `read(InputStream)` | Scan stream content after copying it into memory. |
| Raw pixel input | `readRawGray8(...)`, `readRawPixels(...)` | Scan pre-rendered Gray8/RGB/BGR/RGBA/BGRA images. |
| Async input | `readAsync(...)` | Run scan work asynchronously with `CompletableFuture`. Caller-owned byte arrays must remain unchanged until completion. |
| Format and pages | `detectFormat(...)`, `getPageCount(...)` | Route work and inspect PDF or multi-page image inputs. |
| Rendering | `renderPage(...)`, `renderPageGray8(...)`, `renderPages(...)`, `renderPageAsync(...)` | Render pages or frames through the same native loader used for scanning. |
| Scan options | `BarcodeReaderOptions` | Configure symbology mask, page range, DPI, orientation, timeout and tuning. |
| Reader settings | `BarcodeReaderSettings` | Configure managed default scan options. |
| License checks | `BarcodeNativeLibrary.getLicenseStatus()`, `BarcodeLicenseStatus`, `BarcodeScanException` | Detect demo mode, commercial features and missing license capabilities. |
| Diagnostics | `BarcodeNativeLibrary` | Read native/runtime version, capabilities, format support and probing diagnostics. |

For throughput-sensitive code, set an explicit symbology mask instead of relying
on the native default. For example, use only
`BarcodeSymbology.DATAMATRIX | BarcodeSymbology.QR` when those are the only
expected formats.

## License File

The native runtime searches for `qsbc.lic` by default. For services, containers
and explicit deployments, set `QSBC_LICENSE_FILE` before the first scan:

```bash
export QSBC_LICENSE_FILE=/etc/qualitysoft/qsbc.lic
```

`*.lic` is ignored by this repository and must not be committed.

Without a valid commercial license the runtime runs in demo/evaluation mode.
Scans can still return results, but decoded 1D and 2D payloads are deliberately
modified. Production code should check license status and reject demo mode.

```java
import de.qualitysoft.barcode.BarcodeNativeLibrary;

var status = BarcodeNativeLibrary.getLicenseStatus();
if (status.isDemo()) {
    throw new IllegalStateException("QS Barcode is running in demo mode.");
}
```

If a scan requires license features that are not available, `BarcodeScanException`
contains the requested symbology mask, current license status and missing feature
mask.

## Pricing And Commercial Use

`qualitysoft-barcode` is proprietary commercial software owned by QS QualitySoft
GmbH. Evaluation and production use require a valid license agreement unless
your agreement explicitly grants demo usage.

Pricing, licensing options and product documentation are maintained on the
official product page:

`https://qualitysoft.de/products/qs-barcode-sdk/`

## Supported Platforms

The Maven package contains native runtime assets for:

| RID | Platform |
| --- | --- |
| `win-x86` | Windows 32-bit |
| `win-x64` | Windows x64 |
| `win-arm64` | Windows ARM64 |
| `linux-x64` | Linux x64, glibc |
| `linux-arm64` | Linux ARM64 / AWS Graviton, glibc |
| `osx-x64` | macOS Intel |
| `osx-arm64` | macOS Apple Silicon |

Android, iOS, WebAssembly and Alpine/musl are not part of this package.

PDF input is supported on all listed desktop/server platforms through the
bundled PDFium runtime assets. Applications normally do not need to copy native
libraries manually when they use the official package.

## Native Runtime Override

For custom deployments, set `qualitysoft.barcode.native.path` to either a
directory containing the runtime files or the full loader path:

```bash
java -Dqualitysoft.barcode.native.path=/opt/qsbarcode/native ...
```

When a directory is used, the QS Barcode loader and PDFium must be siblings.

## Build From Source

Build and test the wrapper source:

```powershell
mvn test
```

Install into the local Maven cache:

```powershell
mvn -DskipTests install
```

The public repository does not store runtime binaries in Git. Native smoke tests
need a local runtime under `native/<rid>/` or a runtime override through
`qualitysoft.barcode.native.path`.

## Repository Scope

This public repository contains the Java wrapper, tests, package metadata,
documentation and native asset placeholders. It does not contain proprietary
native engine source, customer license files or release signing material.

## Links

- Product page, pricing and documentation: `https://qualitysoft.de/products/qs-barcode-sdk/`
- Public Java SDK repository: `https://github.com/QS-QualitySoft-GmbH/QSBarcode.Java`
