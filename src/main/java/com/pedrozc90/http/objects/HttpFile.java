package com.pedrozc90.http.objects;

import lombok.Data;

import java.io.File;

/**
 * Represents a file downloaded via an HTTP response.
 *
 * <p>Holds the actual temp {@link File} on disk together with the original metadata
 * extracted from the response headers (filename, content-type, size), so callers
 * don't need to re-parse the response after calling {@link Response#asFile()}.
 */
@Data
public class HttpFile {

    /**
     * The file written to {@code /tmp/<filename>} (or a generated temp path when no
     * filename was present in the {@code Content-Disposition} header).
     */
    private final File file;

    /**
     * Original filename as reported by the {@code Content-Disposition} header,
     * or {@code null} when the header is absent.
     */
    private final String filename;

    /**
     * MIME type from the response {@code Content-Type} header,
     * or {@code null} when the header is absent.
     */
    private final String contentType;

    /**
     * Size of the payload in bytes.
     */
    private final long size;

}
