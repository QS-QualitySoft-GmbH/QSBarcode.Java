package de.qualitysoft.barcode;

import com.sun.jna.Memory;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import de.qualitysoft.barcode.internal.NativeApi;
import de.qualitysoft.barcode.internal.NativeBarcodeResult;
import de.qualitysoft.barcode.internal.NativeImageBuffer;
import de.qualitysoft.barcode.internal.NativeRuntime;
import de.qualitysoft.barcode.internal.NativeScanOptions;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/** Default Java wrapper for the native QS Barcode SDK. */
public final class BarcodeReader implements Closeable {
    private static final int STREAM_COPY_BUFFER_SIZE = 81920;
    private static final long NATIVE_SCAN_THREAD_STACK_SIZE = 16L * 1024L * 1024L;
    private static final NativeApi.ResultCallback COLLECT_CALLBACK = BarcodeReader::collectResult;
    private static final ThreadLocal<Boolean> IN_NATIVE_SCAN = ThreadLocal.withInitial(() -> false);

    private final BarcodeReaderOptions defaultOptions;
    private final ExecutorService nativeScans;
    private volatile boolean closed;

    public BarcodeReader() {
        this(new BarcodeReaderSettings());
    }

    public BarcodeReader(BarcodeReaderOptions defaultOptions) {
        BarcodeReaderSettings settings = new BarcodeReaderSettings();
        settings.setDefaultOptions(defaultOptions);
        settings.validate();
        BarcodeReaderSettings snapshot = settings.clone();
        this.defaultOptions = snapshot.getDefaultOptions().clone();
        this.nativeScans = createNativeScanExecutor();
    }

    public BarcodeReader(BarcodeReaderSettings settings) {
        Objects.requireNonNull(settings, "settings");
        settings.validate();
        BarcodeReaderSettings snapshot = settings.clone();
        this.defaultOptions = snapshot.getDefaultOptions().clone();
        this.nativeScans = createNativeScanExecutor();
    }

    public List<BarcodeResult> read(String path) {
        return read(path, null);
    }

    public List<BarcodeResult> read(String path, BarcodeReaderOptions options) {
        Objects.requireNonNull(path, "path");
        return runNative(() -> readFileCore(path, optionsSnapshot(options)));
    }

    public List<BarcodeResult> read(Path path) {
        return read(path, null);
    }

    public List<BarcodeResult> read(Path path, BarcodeReaderOptions options) {
        Objects.requireNonNull(path, "path");
        return read(path.toString(), options);
    }

    public List<BarcodeResult> read(File file) {
        return read(file, null);
    }

    public List<BarcodeResult> read(File file, BarcodeReaderOptions options) {
        Objects.requireNonNull(file, "file");
        return read(file.toPath(), options);
    }

    public List<BarcodeResult> read(byte[] bytes) {
        return read(bytes, null);
    }

    public List<BarcodeResult> read(byte[] bytes, BarcodeReaderOptions options) {
        validateBytes(bytes, "bytes");
        byte[] snapshot = bytes;
        return runNative(() -> readBytesCore(snapshot, optionsSnapshot(options)));
    }

    public List<BarcodeResult> read(ByteBuffer bytes) {
        return read(bytes, null);
    }

    public List<BarcodeResult> read(ByteBuffer bytes, BarcodeReaderOptions options) {
        Objects.requireNonNull(bytes, "bytes");
        if (!bytes.hasRemaining()) {
            throw new IllegalArgumentException("bytes must not be empty.");
        }
        byte[] copy = copyRemaining(bytes);
        return read(copy, options);
    }

    public List<BarcodeResult> read(InputStream stream) throws IOException {
        return read(stream, null);
    }

    public List<BarcodeResult> read(InputStream stream, BarcodeReaderOptions options) throws IOException {
        Objects.requireNonNull(stream, "stream");
        return read(readAllBytes(stream), options);
    }

    public CompletableFuture<List<BarcodeResult>> readAsync(String path, BarcodeReaderOptions options) {
        Objects.requireNonNull(path, "path");
        BarcodeReaderOptions snapshot = optionsSnapshot(options);
        return CompletableFuture.supplyAsync(() -> {
            ensureOpen();
            return readFileCore(path, snapshot);
        }, nativeScans);
    }

    public CompletableFuture<List<BarcodeResult>> readAsync(byte[] bytes, BarcodeReaderOptions options) {
        validateBytes(bytes, "bytes");
        BarcodeReaderOptions optionSnapshot = optionsSnapshot(options);
        return CompletableFuture.supplyAsync(() -> {
            ensureOpen();
            return readBytesCore(bytes, optionSnapshot);
        }, nativeScans);
    }

    public List<BarcodeResult> readRawGray8(byte[] pixels, int width, int height, int stride, BarcodeReaderOptions options) {
        validateRawBuffer(pixels.length, width, height, BarcodeRawPixelFormat.GRAY8, stride);
        return runNative(() -> {
            Memory memory = new Memory(pixels.length);
            memory.write(0, pixels, 0, pixels.length);
            return readRawGray8Core(memory, width, height, stride, optionsSnapshot(options));
        });
    }

    public List<BarcodeResult> readRawPixels(byte[] pixels, int width, int height, BarcodeRawPixelFormat pixelFormat, int stride, BarcodeReaderOptions options) {
        Objects.requireNonNull(pixelFormat, "pixelFormat");
        int requiredLength = validateRawBuffer(pixels.length, width, height, pixelFormat, stride);
        if (pixelFormat == BarcodeRawPixelFormat.GRAY8) {
            return readRawGray8(pixels, width, height, stride, options);
        }
        byte[] gray = convertRawPixelsToGray8(pixels, requiredLength, width, height, pixelFormat, stride);
        return readRawGray8(gray, width, height, width, options);
    }

    public BarcodeImageFormat detectFormat(String path) {
        Objects.requireNonNull(path, "path");
        int status = api().qsbc_loader_detect_file_format(NativeRuntime.toNullTerminatedUtf8(path));
        return BarcodeImageFormat.fromNative(status);
    }

    public BarcodeImageFormat detectFormat(Path path) {
        Objects.requireNonNull(path, "path");
        return detectFormat(path.toString());
    }

    public BarcodeImageFormat detectFormat(byte[] bytes) {
        validateBytes(bytes, "bytes");
        Memory memory = new Memory(bytes.length);
        memory.write(0, bytes, 0, bytes.length);
        return BarcodeImageFormat.fromNative(api().qsbc_loader_detect_image_format(memory, new NativeLong(bytes.length)));
    }

    public int getPageCount(String path) {
        Objects.requireNonNull(path, "path");
        return requireNonError(api().qsbc_loader_page_count_file(NativeRuntime.toNullTerminatedUtf8(path)));
    }

    public int getPageCount(Path path) {
        Objects.requireNonNull(path, "path");
        return getPageCount(path.toString());
    }

    public int getPageCount(byte[] bytes) {
        validateBytes(bytes, "bytes");
        Memory memory = new Memory(bytes.length);
        memory.write(0, bytes, 0, bytes.length);
        return requireNonError(api().qsbc_loader_page_count_image_memory(memory, new NativeLong(bytes.length)));
    }

    public BarcodeRenderedImage renderPage(String path, BarcodeReaderOptions options) {
        Objects.requireNonNull(path, "path");
        return runNative(() -> renderFile(path, optionsSnapshot(options), false));
    }

    public BarcodeRenderedImage renderPage(Path path, BarcodeReaderOptions options) {
        Objects.requireNonNull(path, "path");
        return renderPage(path.toString(), options);
    }

    public BarcodeRenderedImage renderPage(byte[] bytes, BarcodeReaderOptions options) {
        validateBytes(bytes, "bytes");
        byte[] snapshot = bytes;
        return runNative(() -> renderBytes(snapshot, optionsSnapshot(options), false));
    }

    public BarcodeRenderedImage renderPageGray8(String path, BarcodeReaderOptions options) {
        Objects.requireNonNull(path, "path");
        return runNative(() -> renderFile(path, optionsSnapshot(options), true));
    }

    public BarcodeRenderedImage renderPageGray8(byte[] bytes, BarcodeReaderOptions options) {
        validateBytes(bytes, "bytes");
        byte[] snapshot = bytes;
        return runNative(() -> renderBytes(snapshot, optionsSnapshot(options), true));
    }

    public List<BarcodeRenderedImage> renderPages(String path, BarcodeReaderOptions options) {
        return renderPagesCore(path, options, false);
    }

    public List<BarcodeRenderedImage> renderPagesGray8(String path, BarcodeReaderOptions options) {
        return renderPagesCore(path, options, true);
    }

    public CompletableFuture<BarcodeRenderedImage> renderPageAsync(String path, BarcodeReaderOptions options) {
        Objects.requireNonNull(path, "path");
        BarcodeReaderOptions snapshot = optionsSnapshot(options);
        return CompletableFuture.supplyAsync(() -> {
            ensureOpen();
            return renderFile(path, snapshot, false);
        }, nativeScans);
    }

    @Override
    public void close() {
        closed = true;
        nativeScans.shutdownNow();
    }

    private List<BarcodeRenderedImage> renderPagesCore(String path, BarcodeReaderOptions options, boolean gray8) {
        Objects.requireNonNull(path, "path");
        BarcodeReaderOptions base = optionsSnapshot(options);
        int pageCount = getPageCount(path);
        int start = base.getPageStart() < 0 ? 0 : base.getPageStart();
        int end = base.getPageCount() <= 0 ? pageCount : Math.min(pageCount, start + base.getPageCount());
        List<BarcodeRenderedImage> images = new ArrayList<>();
        for (int page = start; page < end; page++) {
            BarcodeReaderOptions pageOptions = base.clone().setPageStart(page).setPageCount(1);
            images.add(runNative(() -> renderFile(path, pageOptions, gray8)));
        }
        return Collections.unmodifiableList(images);
    }

    private List<BarcodeResult> readFileCore(String path, BarcodeReaderOptions options) {
        NativeScanOptions nativeOptions = toNativeOptions(options);
        List<BarcodeResult> results = new ArrayList<>();
        int status = api().qsbc_loader_scan_file_cb_with_options(
                NativeRuntime.toNullTerminatedUtf8(path),
                nativeOptions,
                (result, userData) -> collectResult(result, userData, results, options),
                null);
        return finishScan(status, results, options.getSymbologies());
    }

    private List<BarcodeResult> readBytesCore(byte[] bytes, BarcodeReaderOptions options) {
        Memory memory = new Memory(bytes.length);
        memory.write(0, bytes, 0, bytes.length);
        return readMemoryCore(memory, bytes.length, options);
    }

    private List<BarcodeResult> readMemoryCore(Pointer pointer, int byteLength, BarcodeReaderOptions options) {
        NativeScanOptions nativeOptions = toNativeOptions(options);
        List<BarcodeResult> results = new ArrayList<>();
        int status = api().qsbc_loader_scan_image_memory_cb_with_options(
                pointer,
                new NativeLong(byteLength),
                nativeOptions,
                (result, userData) -> collectResult(result, userData, results, options),
                null);
        return finishScan(status, results, options.getSymbologies());
    }

    private List<BarcodeResult> readRawGray8Core(Pointer pixels, int width, int height, int stride, BarcodeReaderOptions options) {
        NativeScanOptions nativeOptions = toNativeOptions(options);
        List<BarcodeResult> results = new ArrayList<>();
        int effectiveStride = stride == 0 ? width : stride;
        int status = api().qsbc_loader_scan_gray8_cb_with_options(
                pixels,
                width,
                height,
                effectiveStride,
                nativeOptions,
                (result, userData) -> collectResult(result, userData, results, options),
                null);
        return finishScan(status, results, options.getSymbologies());
    }

    private BarcodeRenderedImage renderFile(String path, BarcodeReaderOptions options, boolean gray8) {
        NativeImageBuffer output = new NativeImageBuffer();
        NativeScanOptions nativeOptions = toNativeOptions(options);
        int status = gray8
                ? api().qsbc_loader_render_file_page_gray8_with_options(NativeRuntime.toNullTerminatedUtf8(path), nativeOptions, output)
                : api().qsbc_loader_render_file_page_bmp_with_options(NativeRuntime.toNullTerminatedUtf8(path), nativeOptions, output);
        return finishRender(status, output, gray8);
    }

    private BarcodeRenderedImage renderBytes(byte[] bytes, BarcodeReaderOptions options, boolean gray8) {
        Memory memory = new Memory(bytes.length);
        memory.write(0, bytes, 0, bytes.length);
        NativeImageBuffer output = new NativeImageBuffer();
        NativeScanOptions nativeOptions = toNativeOptions(options);
        int status = gray8
                ? api().qsbc_loader_render_image_memory_page_gray8_with_options(memory, new NativeLong(bytes.length), nativeOptions, output)
                : api().qsbc_loader_render_image_memory_page_bmp_with_options(memory, new NativeLong(bytes.length), nativeOptions, output);
        return finishRender(status, output, gray8);
    }

    private BarcodeRenderedImage finishRender(int status, NativeImageBuffer output, boolean gray8) {
        if (status < 0) {
            throw new BarcodeScanException(status, BarcodeNativeLibrary.getStatusName(status));
        }
        try {
            long length = output.len == null ? 0 : output.len.longValue();
            if (output.data == null || length <= 0 || length > Integer.MAX_VALUE) {
                throw new BarcodeScanException(-2, "invalid buffer");
            }
            byte[] bytes = output.data.getByteArray(0, (int) length);
            int stride = gray8 ? output.width : 0;
            return new BarcodeRenderedImage(
                    bytes,
                    output.width,
                    output.height,
                    BarcodeImageFormat.fromNative(output.format),
                    output.page_index,
                    gray8 ? BarcodeRenderedPixelFormat.GRAY8 : BarcodeRenderedPixelFormat.BMP24,
                    stride);
        } finally {
            api().qsbc_loader_free_image_buffer(output);
        }
    }

    private NativeScanOptions toNativeOptions(BarcodeReaderOptions options) {
        options.validate();
        NativeScanOptions nativeOptions = new NativeScanOptions();
        int init = api().qsbc_loader_scan_options_init(nativeOptions);
        if (init < 0) {
            throw new BarcodeScanException(init, BarcodeNativeLibrary.getStatusName(init));
        }
        nativeOptions.struct_size = nativeOptions.size();
        nativeOptions.mask = options.getSymbologies();
        nativeOptions.min_length = options.getMinLength() <= 0 ? 1 : options.getMinLength();
        nativeOptions.flags = options.getFlags();
        nativeOptions.page_start = options.getPageStart();
        nativeOptions.page_count = options.getPageCount();
        nativeOptions.dpi = options.getDpi();
        nativeOptions.reserved0 = options.getDataMatrixFinderAngleTolerance();
        nativeOptions.reserved1 = options.getDataMatrixOverlapPercent();
        nativeOptions.reserved2 = options.getDataMatrixMaxLineCandidates();
        nativeOptions.threshold = options.getThreshold();
        nativeOptions.orientation = options.getOrientation();
        nativeOptions.max_skew_degrees = options.getMaxSkewDegrees();
        nativeOptions.light_margin = options.getLightMargin();
        nativeOptions.scan_distance_barcode = options.getScanDistanceBarcode();
        nativeOptions.tolerance = options.getTolerance();
        nativeOptions.min_height = options.getMinHeight();
        nativeOptions.percent = options.getPercent();
        nativeOptions.scan_distance = options.getScanDistance();
        nativeOptions.max_gap = options.getMaxGap();
        nativeOptions.max_height = options.getMaxHeight();
        nativeOptions.checksum_flags = options.getChecksumFlags();
        nativeOptions.scan_timeout_ms = options.getScanTimeoutMs();
        nativeOptions.write();
        return nativeOptions;
    }

    private List<BarcodeResult> finishScan(int status, List<BarcodeResult> results, int requestedSymbologies) {
        if (status < 0) {
            String statusName = BarcodeNativeLibrary.getStatusName(status);
            if (status == NativeApi.QSBC_ERROR_LICENSE_REQUIRED) {
                BarcodeLicenseStatus licenseStatus = BarcodeNativeLibrary.getLicenseStatus();
                throw new BarcodeScanException(
                        status,
                        statusName,
                        requestedSymbologies,
                        licenseStatus,
                        licenseStatus.missingFeaturesFor(requestedSymbologies));
            }
            throw new BarcodeScanException(status, statusName);
        }
        return Collections.unmodifiableList(new ArrayList<>(results));
    }

    private static int collectResult(Pointer result, Pointer userData) {
        return 0;
    }

    private static int collectResult(Pointer result, Pointer userData, List<BarcodeResult> results, BarcodeReaderOptions options) {
        if (result == null) {
            return NativeApi.QSBC_STATUS_MISS;
        }
        NativeBarcodeResult nativeResult = new NativeBarcodeResult(result);
        byte[] raw = nativeResult.text == null || nativeResult.text_len <= 0
                ? new byte[0]
                : nativeResult.text.getByteArray(0, nativeResult.text_len);
        BarcodeBounds bounds = nativeResult.has_bounds == 0
                ? null
                : new BarcodeBounds(nativeResult.barcode_x, nativeResult.barcode_y, nativeResult.barcode_width, nativeResult.barcode_height);
        results.add(new BarcodeResult(
                BarcodeImageFormat.fromNative(nativeResult.format),
                nativeResult.symbology_mask,
                decodeText(raw, options.getTextEncoding()),
                raw,
                nativeResult.width,
                nativeResult.height,
                Integer.toUnsignedLong(nativeResult.image_index),
                nativeResult.page_index,
                bounds));
        return 0;
    }

    private static String decodeText(byte[] raw, Charset configuredEncoding) {
        if (configuredEncoding != null) {
            return new String(raw, configuredEncoding);
        }
        try {
            return StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(raw))
                    .toString();
        } catch (CharacterCodingException ex) {
            return new String(raw, Charset.forName("windows-1252"));
        }
    }

    private BarcodeReaderOptions optionsSnapshot(BarcodeReaderOptions options) {
        return (options == null ? defaultOptions : options).clone();
    }

    private <T> T runNative(NativeCallable<T> callable) {
        if (Boolean.TRUE.equals(IN_NATIVE_SCAN.get())) {
            return callNative(callable);
        }

        Future<T> future = nativeScans.submit(() -> callNative(callable));
        try {
            return future.get();
        }
        catch (RuntimeException ex) {
            throw ex;
        }
        catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for native scan.", ex);
        }
        catch (ExecutionException ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            if (cause instanceof Error) {
                throw (Error) cause;
            }
            throw new IllegalStateException(cause);
        }
    }

    private <T> T callNative(NativeCallable<T> callable) {
        try {
            ensureOpen();
            IN_NATIVE_SCAN.set(true);
            return callable.call();
        }
        catch (RuntimeException ex) {
            throw ex;
        }
        catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
        finally {
            IN_NATIVE_SCAN.remove();
        }
    }

    private static NativeApi api() {
        return NativeRuntime.api();
    }

    private static ExecutorService createNativeScanExecutor() {
        int count = Math.max(1, Runtime.getRuntime().availableProcessors());
        AtomicInteger next = new AtomicInteger(1);
        ThreadFactory threadFactory = runnable -> {
            Thread thread = new Thread(
                    null,
                    runnable,
                    "QS Barcode native scan " + next.getAndIncrement(),
                    NATIVE_SCAN_THREAD_STACK_SIZE);
            thread.setDaemon(true);
            return thread;
        };
        return Executors.newFixedThreadPool(count, threadFactory);
    }

    private static int requireNonError(int status) {
        if (status < 0) {
            throw new BarcodeScanException(status, BarcodeNativeLibrary.getStatusName(status));
        }
        return status;
    }

    private void ensureOpen() {
        if (closed) {
            throw new IllegalStateException("BarcodeReader is closed.");
        }
    }

    private static void validateBytes(byte[] bytes, String name) {
        Objects.requireNonNull(bytes, name);
        if (bytes.length == 0) {
            throw new IllegalArgumentException(name + " must not be empty.");
        }
    }

    private static byte[] copyRemaining(ByteBuffer buffer) {
        ByteBuffer duplicate = buffer.slice();
        byte[] copy = new byte[duplicate.remaining()];
        duplicate.get(copy);
        return copy;
    }

    private static byte[] readAllBytes(InputStream stream) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[STREAM_COPY_BUFFER_SIZE];
        int read;
        while ((read = stream.read(buffer)) >= 0) {
            output.write(buffer, 0, read);
        }
        return output.toByteArray();
    }

    private static int validateRawBuffer(int length, int width, int height, BarcodeRawPixelFormat pixelFormat, int stride) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("width and height must be positive.");
        }
        int effectiveStride = stride == 0 ? width * pixelFormat.bytesPerPixel() : stride;
        int minimumStride = width * pixelFormat.bytesPerPixel();
        if (effectiveStride < minimumStride) {
            throw new IllegalArgumentException("stride is too small for width and pixel format.");
        }
        long required = (long) effectiveStride * height;
        if (required > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("raw pixel buffer is too large.");
        }
        if (length < required) {
            throw new IllegalArgumentException("raw pixel buffer is shorter than width, height and stride require.");
        }
        return (int) required;
    }

    private static byte[] convertRawPixelsToGray8(byte[] source, int requiredLength, int width, int height, BarcodeRawPixelFormat pixelFormat, int stride) {
        int effectiveStride = stride == 0 ? width * pixelFormat.bytesPerPixel() : stride;
        byte[] gray = new byte[width * height];
        for (int y = 0; y < height; y++) {
            int row = y * effectiveStride;
            for (int x = 0; x < width; x++) {
                int offset = row + x * pixelFormat.bytesPerPixel();
                int r;
                int g;
                int b;
                switch (pixelFormat) {
                    case RGB24:
                    case RGBA32:
                        r = source[offset] & 0xff;
                        g = source[offset + 1] & 0xff;
                        b = source[offset + 2] & 0xff;
                        break;
                    case BGR24:
                    case BGRA32:
                        b = source[offset] & 0xff;
                        g = source[offset + 1] & 0xff;
                        r = source[offset + 2] & 0xff;
                        break;
                    default:
                        throw new IllegalArgumentException("Unsupported conversion pixel format " + pixelFormat);
                }
                gray[y * width + x] = (byte) ((r * 299 + g * 587 + b * 114 + 500) / 1000);
            }
        }
        if (requiredLength < 0) {
            throw new IllegalArgumentException("requiredLength");
        }
        return gray;
    }

    private interface NativeCallable<T> {
        T call();
    }
}
