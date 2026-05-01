package com.pedrozc90.http.objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonIgnore
    private final File file;

    /**
     * Original filename as reported by the {@code Content-Disposition} header,
     * or {@code null} when the header is absent.
     */
    @JsonProperty(value = "filename")
    private final String filename;

    /**
     * MIME type from the response {@code Content-Type} header,
     * or {@code null} when the header is absent.
     */
    @JsonProperty(value = "content_type")
    private final String contentType;

    /**
     * Size of the payload in bytes.
     */
    @JsonProperty(value = "size")
    private final long size;

    public HttpFile(
        final File file,
        final String filename,
        final String contentType,
        final long size
    ) {
        this.file = file;
        this.filename = filename;
        this.contentType = contentType;
        this.size = size;
    }

    @JsonCreator
    public HttpFile(
        @JsonProperty(value = "filename") String filename,
        @JsonProperty(value = "content_type") String contentType,
        @JsonProperty(value = "size") long size
    ) {
        this(null, filename, contentType, size);
    }
}
