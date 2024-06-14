package enums;

import com.google.common.collect.Maps;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

public enum MimeType {
    txt("text/plain", ".txt"),
    html("text/html", ".html"),
    js("text/javascript", ".js"),
    css("text/css", ".css"),
    jpg("image/jpeg", ".jpeg"),
    jpeg("image/jpeg", ".jpeg"),
    png("image/png", ".png"),
    mp4("video/mp4", ".mp4"),
    gif("image/gif", ".gif"),
    json("application/json", ".json"),
    zip("application/zip", ".zip"),
    xlsx("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", ".xlsx"),
    pdf("application/pdf", ".pdf"),
    svg("image/svg+xml", ".svg"),
    csv("application/CSV", ".csv");

    static final Map<String, MimeType> map = Maps.newHashMap();

    static {
        Arrays.stream(MimeType.values()).forEach(MimeType::accept);
        Collections.unmodifiableMap(map);
    }

    final String contentType;
    final String extension;

    MimeType(final String contentType, final String extension) {
        this.contentType = contentType;
        this.extension = extension;
    }

    private static void accept(final MimeType mimetype) {
        map.put(mimetype.contentType, mimetype);
    }

    public String contentType() {
        return this.contentType;
    }

    public String extension() {
        return extension;
    }

    public static MimeType getMimeType(final String contentType) {
        return map.get(contentType);
    }
}
