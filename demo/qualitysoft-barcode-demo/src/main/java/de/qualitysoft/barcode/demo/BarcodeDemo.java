package de.qualitysoft.barcode.demo;

import de.qualitysoft.barcode.BarcodeNativeLibrary;
import de.qualitysoft.barcode.BarcodeReader;
import de.qualitysoft.barcode.BarcodeReaderOptions;
import de.qualitysoft.barcode.BarcodeResult;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.WString;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class BarcodeDemo {
    private BarcodeDemo() {
    }

    public static void main(String[] args) throws IOException {
        DemoArguments arguments = DemoArguments.parse(args);
        if (arguments.showHelp) {
            DemoArguments.printUsage();
            System.exit(2);
        }

        if (arguments.licenseFile != null) {
            setLicenseEnvironment(arguments.licenseFile);
        }

        System.out.println("QS Barcode ABI: " + BarcodeNativeLibrary.getVersion());
        System.out.println("QS Barcode engine: " + BarcodeNativeLibrary.getEngineVersion());
        if (arguments.licenseFile == null) {
            System.out.println("License status: " + BarcodeNativeLibrary.getLicenseStatus());
            System.out.println("No qsbc.lic passed; native default search is used and demo mode may degrade decoded values.");
        } else {
            System.out.println("License file: " + arguments.licenseFile);
            System.out.println("License status for file: " + BarcodeNativeLibrary.getLicenseStatus(arguments.licenseFile.toString()));
        }

        BarcodeReaderOptions options = new BarcodeReaderOptions()
                .setDpi(arguments.dpi)
                .setMinLength(arguments.minLength)
                .setScanTimeoutMs(arguments.timeoutMs);

        try (BarcodeReader reader = new BarcodeReader()) {
            for (Path file : inputFiles(arguments.inputPath)) {
                System.out.println(file);
                List<BarcodeResult> results = reader.read(file, options);
                if (results.isEmpty()) {
                    System.out.println("  no barcode found");
                    continue;
                }
                for (BarcodeResult result : results) {
                    System.out.println("  format=" + result.getImageFormat()
                            + " page=" + result.getPageIndex()
                            + " text=" + result.getText());
                }
            }
        }
    }

    private static List<Path> inputFiles(Path input) throws IOException {
        if (Files.isRegularFile(input)) {
            return List.of(input.toAbsolutePath());
        }
        if (!Files.isDirectory(input)) {
            throw new IllegalArgumentException("Input file or directory was not found: " + input);
        }
        List<Path> files = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(input)) {
            for (Path file : stream) {
                if (Files.isRegularFile(file)) {
                    files.add(file.toAbsolutePath());
                }
            }
        }
        files.sort(Comparator.comparing(Path::toString));
        return files;
    }

    private static void setLicenseEnvironment(Path licenseFile) {
        String value = licenseFile.toAbsolutePath().normalize().toString();
        if (System.getProperty("os.name", "").toLowerCase().contains("win")) {
            if (!Kernel32.INSTANCE.SetEnvironmentVariableW(new WString("QSBC_LICENSE_FILE"), new WString(value))) {
                throw new IllegalStateException("SetEnvironmentVariableW failed.");
            }
            putEnv("ucrtbase", "QSBC_LICENSE_FILE", value);
            putEnv("msvcrt", "QSBC_LICENSE_FILE", value);
        } else {
            int status = Libc.INSTANCE.setenv("QSBC_LICENSE_FILE", value, 1);
            if (status != 0) {
                throw new IllegalStateException("setenv failed with status " + status);
            }
        }
    }

    private static void putEnv(String library, String name, String value) {
        try {
            int status = Native.load(library, CEnvironment.class)._putenv_s(name, value);
            if (status != 0) {
                throw new IllegalStateException(library + " _putenv_s failed with status " + status);
            }
        } catch (UnsatisfiedLinkError ignored) {
        }
    }

    private interface CEnvironment extends Library {
        int _putenv_s(String name, String value);
    }

    private interface Kernel32 extends Library {
        Kernel32 INSTANCE = Native.load("kernel32", Kernel32.class);
        boolean SetEnvironmentVariableW(WString name, WString value);
    }

    private interface Libc extends Library {
        Libc INSTANCE = Native.load("c", Libc.class);
        int setenv(String name, String value, int overwrite);
    }

    private static final class DemoArguments {
        private final Path inputPath;
        private final Path licenseFile;
        private final int dpi;
        private final int minLength;
        private final int timeoutMs;
        private final boolean showHelp;

        private DemoArguments(Path inputPath, Path licenseFile, int dpi, int minLength, int timeoutMs, boolean showHelp) {
            this.inputPath = inputPath;
            this.licenseFile = licenseFile;
            this.dpi = dpi;
            this.minLength = minLength;
            this.timeoutMs = timeoutMs;
            this.showHelp = showHelp;
        }

        private static DemoArguments parse(String[] args) {
            if (args.length == 0) {
                return new DemoArguments(null, null, 300, 1, 0, true);
            }
            Path input = null;
            Path license = null;
            int dpi = 300;
            int minLength = 1;
            int timeout = 0;
            for (int index = 0; index < args.length; index++) {
                switch (args[index]) {
                    case "--help":
                    case "-h":
                        return new DemoArguments(null, null, dpi, minLength, timeout, true);
                    case "--license":
                        license = Path.of(requireValue(args, ++index, "--license")).toAbsolutePath();
                        break;
                    case "--dpi":
                        dpi = Integer.parseInt(requireValue(args, ++index, "--dpi"));
                        break;
                    case "--min-length":
                        minLength = Integer.parseInt(requireValue(args, ++index, "--min-length"));
                        break;
                    case "--timeout-ms":
                        timeout = Integer.parseInt(requireValue(args, ++index, "--timeout-ms"));
                        break;
                    default:
                        if (input == null) {
                            input = Path.of(args[index]).toAbsolutePath();
                        }
                        break;
                }
            }
            return new DemoArguments(input, license, dpi, minLength, timeout, input == null);
        }

        private static void printUsage() {
            System.out.println("Usage: mvn -f <demo-pom.xml> compile exec:java -Dexec.args=\"<file-or-directory> [--license qsbc.lic]\"");
        }

        private static String requireValue(String[] args, int index, String option) {
            if (index >= args.length) {
                throw new IllegalArgumentException(option + " requires a value.");
            }
            return args[index];
        }
    }
}
