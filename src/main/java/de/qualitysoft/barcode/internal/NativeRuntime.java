package de.qualitysoft.barcode.internal;

import com.sun.jna.Native;
import de.qualitysoft.barcode.BarcodeNativeLibraryException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;

public final class NativeRuntime {
    public static final String VERSION = "6.0.0";
    public static final String NATIVE_PATH_PROPERTY = "qualitysoft.barcode.native.path";

    private static final Object LOCK = new Object();
    private static volatile NativeApi api;
    private static volatile RuntimeLayout layout;

    private NativeRuntime() {
    }

    public static NativeApi api() {
        NativeApi current = api;
        if (current != null) {
            return current;
        }
        synchronized (LOCK) {
            if (api == null) {
                layout = resolveLayout();
                preload(layout);
                api = Native.load(layout.loader.toAbsolutePath().toString(), NativeApi.class);
            }
            return api;
        }
    }

    public static String runtimeIdentifier() {
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        String arch = System.getProperty("os.arch", "").toLowerCase(Locale.ROOT);
        String osPart;
        if (os.contains("win")) {
            osPart = "win";
        } else if (os.contains("mac") || os.contains("darwin")) {
            osPart = "osx";
        } else if (os.contains("linux")) {
            osPart = "linux";
        } else {
            throw new BarcodeNativeLibraryException("Unsupported QS Barcode platform: " + System.getProperty("os.name"));
        }

        String archPart;
        if (arch.equals("x86") || arch.equals("i386") || arch.equals("i686")) {
            archPart = "x86";
        } else if (arch.equals("aarch64") || arch.equals("arm64")) {
            archPart = "arm64";
        } else if (arch.equals("amd64") || arch.equals("x86_64")) {
            archPart = "x64";
        } else {
            throw new BarcodeNativeLibraryException("Unsupported QS Barcode CPU architecture: " + System.getProperty("os.arch"));
        }

        return osPart + "-" + archPart;
    }

    public static String diagnostics() {
        RuntimeLayout current = layout;
        String configured = System.getProperty(NATIVE_PATH_PROPERTY);
        return "Runtime identifier: " + runtimeIdentifier()
                + ". Native path property: " + (configured == null ? "<unset>" : configured)
                + ". Resolved layout: " + (current == null ? "<not loaded>" : current);
    }

    private static RuntimeLayout resolveLayout() {
        String configured = System.getProperty(NATIVE_PATH_PROPERTY);
        if (configured != null && !configured.trim().isEmpty()) {
            return layoutFromConfiguredPath(Paths.get(configured.trim()));
        }

        String rid = runtimeIdentifier();
        RuntimeLayout extracted = extractResourceLayout(rid);
        if (extracted != null) {
            return extracted;
        }

        RuntimeLayout dev = findDevelopmentLayout(rid);
        if (dev != null) {
            return dev;
        }

        throw new BarcodeNativeLibraryException("Unable to find QS Barcode native runtime for " + rid + ". " + diagnostics());
    }

    private static RuntimeLayout layoutFromConfiguredPath(Path path) {
        Path absolute = path.toAbsolutePath().normalize();
        if (Files.isDirectory(absolute)) {
            return layoutFromDirectory(absolute);
        }
        return new RuntimeLayout(absolute, null);
    }

    private static RuntimeLayout findDevelopmentLayout(String rid) {
        Path current = Paths.get("").toAbsolutePath();
        for (Path path = current; path != null; path = path.getParent()) {
            Path candidate = path.resolve("sdk").resolve("dotnet").resolve("native").resolve(rid);
            RuntimeLayout layout = layoutFromDirectoryIfPresent(candidate);
            if (layout != null) {
                return layout;
            }
        }
        return null;
    }

    private static RuntimeLayout extractResourceLayout(String rid) {
        String loader = loaderFileName(rid);
        String base = "de/qualitysoft/barcode/native/" + rid + "/";
        if (NativeRuntime.class.getClassLoader().getResource(base + loader) == null) {
            return null;
        }

        Path directory = Paths.get(System.getProperty("java.io.tmpdir"), "qualitysoft-barcode", VERSION, rid);
        try {
            Files.createDirectories(directory);
            Path pdfium = copyResourceIfPresent(base + pdfiumFileName(rid), directory.resolve(pdfiumFileName(rid)));
            Path loaderPath = copyResource(base + loader, directory.resolve(loader));
            makeExecutable(loaderPath);
            return new RuntimeLayout(loaderPath, pdfium);
        } catch (IOException ex) {
            throw new BarcodeNativeLibraryException("Unable to extract QS Barcode native runtime for " + rid + ".", ex);
        }
    }

    private static RuntimeLayout layoutFromDirectoryIfPresent(Path directory) {
        if (!Files.isDirectory(directory)) {
            return null;
        }
        RuntimeLayout layout = layoutFromDirectory(directory);
        return Files.isRegularFile(layout.loader) ? layout : null;
    }

    private static RuntimeLayout layoutFromDirectory(Path directory) {
        String rid = runtimeIdentifier();
        return new RuntimeLayout(
                directory.resolve(loaderFileName(rid)),
                directory.resolve(pdfiumFileName(rid)));
    }

    private static Path copyResourceIfPresent(String resource, Path target) throws IOException {
        if (NativeRuntime.class.getClassLoader().getResource(resource) == null) {
            return null;
        }
        return copyResource(resource, target);
    }

    private static Path copyResource(String resource, Path target) throws IOException {
        try (InputStream stream = NativeRuntime.class.getClassLoader().getResourceAsStream(resource)) {
            if (stream == null) {
                throw new IOException("Missing resource " + resource);
            }
            byte[] bytes = stream.readAllBytes();
            String hash = sha256(bytes);
            Path hashFile = target.resolveSibling(target.getFileName().toString() + ".sha256");
            if (Files.isRegularFile(target)
                    && Files.isRegularFile(hashFile)
                    && hash.equals(new String(Files.readAllBytes(hashFile), java.nio.charset.StandardCharsets.US_ASCII).trim())) {
                return target;
            }
            Files.write(target, bytes);
            Files.write(hashFile, Collections.singleton(hash), java.nio.charset.StandardCharsets.US_ASCII);
            return target;
        }
    }

    private static void preload(RuntimeLayout layout) {
        try {
            if (layout.pdfium != null && Files.isRegularFile(layout.pdfium)) {
                System.load(layout.pdfium.toAbsolutePath().toString());
            }
        } catch (UnsatisfiedLinkError ex) {
            throw new BarcodeNativeLibraryException("Unable to preload native dependency: " + layout.pdfium, ex);
        }
    }

    private static void makeExecutable(Path path) {
        if (path == null || isWindows()) {
            return;
        }
        try {
            Set<PosixFilePermission> permissions = Files.getPosixFilePermissions(path);
            permissions = EnumSet.copyOf(permissions);
            permissions.add(PosixFilePermission.OWNER_EXECUTE);
            permissions.add(PosixFilePermission.GROUP_EXECUTE);
            permissions.add(PosixFilePermission.OTHERS_EXECUTE);
            Files.setPosixFilePermissions(path, permissions);
        } catch (UnsupportedOperationException | IOException ignored) {
            path.toFile().setExecutable(true, false);
        }
    }

    private static String loaderFileName(String rid) {
        if (rid.startsWith("win-")) {
            return "qs_barcode_loader_sdk.dll";
        }
        if (rid.startsWith("osx-")) {
            return "libqs_barcode_loader_sdk.dylib";
        }
        return "libqs_barcode_loader_sdk.so";
    }

    private static String pdfiumFileName(String rid) {
        if (rid.startsWith("win-")) {
            return "pdfium.dll";
        }
        if (rid.startsWith("osx-")) {
            return "libpdfium.dylib";
        }
        return "libpdfium.so";
    }

    public static byte[] toNullTerminatedUtf8(String value) {
        byte[] bytes = value.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        byte[] result = new byte[bytes.length + 1];
        System.arraycopy(bytes, 0, result, 0, bytes.length);
        return result;
    }

    public static String pointerString(com.sun.jna.Pointer pointer) {
        return pointer == null ? null : pointer.getString(0, "UTF-8");
    }

    public static String sha256(byte[] bytes) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(bytes);
            StringBuilder builder = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                builder.append(String.format("%02x", b & 0xff));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win");
    }

    private static final class RuntimeLayout {
        private final Path loader;
        private final Path pdfium;

        private RuntimeLayout(Path loader, Path pdfium) {
            this.loader = loader;
            this.pdfium = pdfium;
        }

        @Override
        public String toString() {
            return "RuntimeLayout{"
                    + "loader=" + loader
                    + ", pdfium=" + pdfium
                    + '}';
        }
    }
}
