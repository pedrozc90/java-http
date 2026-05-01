package com.pedrozc90.http.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class FileUtils {

    private FileUtils() {
    }

    private static final String _dir = System.getProperty("java.io.tmpdir");
    private static final Map<String, String> _map;

    static {
        final Map<String, String> tmp = new HashMap<>();

        // spreadsheets
        tmp.put("xls", "application/vnd.ms-excel");
        tmp.put("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        tmp.put("csv", "text/csv");

        // text / data
        tmp.put("txt", "text/plain");
        tmp.put("json", "application/json");
        tmp.put("xml", "application/xml");
        tmp.put("pdf", "application/pdf");

        // images
        tmp.put("jpg", "image/jpeg");
        tmp.put("jpeg", "image/jpeg");
        tmp.put("png", "image/png");
        tmp.put("gif", "image/gif");
        tmp.put("bmp", "image/bmp");
        tmp.put("svg", "image/svg+xml");

        // video
        tmp.put("mp4", "video/mp4");
        tmp.put("mkv", "video/x-matroska");

        // extras
        tmp.put("html", "text/html");
        tmp.put("css", "text/css");
        tmp.put("js", "application/javascript");
        tmp.put("yml", "application/x-yaml");
        tmp.put("yaml", "application/x-yaml");
        tmp.put("md", "text/markdown");
        tmp.put("zip", "application/zip");
        tmp.put("gzip", "application/gzip");
        tmp.put("7z", "application/x-7z-compressed");
        tmp.put("rar", "application/vnd.rar");
        tmp.put("tar", "application/x-tar");
        tmp.put("jar", "application/java-archive");
        tmp.put("jnlp", "application/x-java-jnlp-file");

        // tags
        tmp.put("zpl", "text/zpl");
        tmp.put("epl", "text/epl");
        tmp.put("sbpl", "text/sbpl");
        tmp.put("prn", "text/prn");
        tmp.put("lbl", "text/label");

        _map = Collections.unmodifiableMap(tmp);
    }

    public static File createTempFile(final String filename) throws IOException {
        final String ext = getExtension(filename);
        final String basename = filename.replace("." + ext, "");
        final File file = File.createTempFile(basename, ext);
        file.deleteOnExit();
        return file;
    }

    /**
     * Returns the extension without dot, or null.
     */
    public static String getExtension(final String filename) {
        if (filename == null) {
            return null;
        }
        int lastDot = filename.lastIndexOf('.');
        if (lastDot < 0 || lastDot == filename.length() - 1) {
            return null;
        }
        return filename.substring(lastDot + 1);
    }

    /**
     * Guess content type by filename extension.
     * Falls back to Files.probeContentType if available.
     */
    public static String guessContentType(final String filename) {
        if (StringUtils.isNotBlank(filename)) {
            String ext = getExtension(filename);
            if (ext != null) {
                String byExt = _map.get(ext.toLowerCase(Locale.ROOT));
                if (byExt != null) {
                    return byExt;
                }
            }

            // fallback to OS/JDK detection
            try {
                Path path = Paths.get(filename);
                String detected = Files.probeContentType(path);
                if (detected != null) {
                    return detected;
                }
            } catch (Exception ignored) {
                // ignore
            }
        }

        return "application/octet-stream";
    }

    /**
     * Normalize filename:
     * - remove accents
     * - replace spaces with underscore
     * - remove special characters
     */
    public static String normalize(final String filename) {
        if (filename == null) return null;

        final String normalized = Normalizer.normalize(filename, Normalizer.Form.NFD)
            .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");

        return normalized
            .replaceAll("[^a-zA-Z0-9._-]", "_")
            .replaceAll("_+", "_")
            .replaceAll("^_|_$", "");
    }

}
