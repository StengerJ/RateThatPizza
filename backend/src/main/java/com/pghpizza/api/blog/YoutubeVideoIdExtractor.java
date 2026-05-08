package com.pghpizza.api.blog;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Pattern;

final class YoutubeVideoIdExtractor {
    private static final Pattern YOUTUBE_ID_PATTERN = Pattern.compile("^[A-Za-z0-9_-]{11}$");
    private static final Pattern YOUTUBE_PATH_ID_PATTERN =
            Pattern.compile("^/(?:embed|shorts|live|v)/([A-Za-z0-9_-]{11})(?:/|$)");

    private YoutubeVideoIdExtractor() {
    }

    static String extract(String value) {
        String candidate = value == null ? "" : value.trim();

        if (candidate.isBlank()) {
            return null;
        }

        if (YOUTUBE_ID_PATTERN.matcher(candidate).matches()) {
            return candidate;
        }

        String queryOnlyId = extractFromQueryOnly(candidate);
        if (queryOnlyId != null) {
            return queryOnlyId;
        }

        try {
            URI uri = URI.create(candidate);
            String host = normalizedHost(uri);

            if ("youtu.be".equals(host)) {
                return firstPathSegment(uri.getPath())
                        .filter(YoutubeVideoIdExtractor::isValidId)
                        .orElse(null);
            }

            if (isYoutubeHost(host)) {
                String watchId = queryValue(uri.getRawQuery(), "v");
                if (isValidId(watchId)) {
                    return watchId;
                }

                var pathMatch = YOUTUBE_PATH_ID_PATTERN.matcher(Optional.ofNullable(uri.getPath()).orElse(""));
                if (pathMatch.find()) {
                    return pathMatch.group(1);
                }

                String nestedUrl = Optional.ofNullable(queryValue(uri.getRawQuery(), "u"))
                        .orElseGet(() -> queryValue(uri.getRawQuery(), "url"));
                if (nestedUrl == null) {
                    return null;
                }

                String nestedCandidate = nestedUrl.startsWith("/")
                        ? URI.create("https://www.youtube.com").resolve(nestedUrl).toString()
                        : nestedUrl;
                return extract(nestedCandidate);
            }
        } catch (IllegalArgumentException exception) {
            return null;
        }

        return null;
    }

    private static String extractFromQueryOnly(String candidate) {
        String query = "";

        if (candidate.startsWith("?")) {
            query = candidate.substring(1);
        } else if (candidate.startsWith("watch?")) {
            query = candidate.substring("watch?".length());
        } else if (candidate.startsWith("/watch?")) {
            query = candidate.substring("/watch?".length());
        } else if (candidate.startsWith("v=")) {
            query = candidate;
        }

        String id = queryValue(query, "v");
        return isValidId(id) ? id : null;
    }

    private static String normalizedHost(URI uri) {
        String host = uri.getHost();
        if (host == null) {
            return "";
        }

        return host.replaceFirst("^www\\.", "").toLowerCase();
    }

    private static Optional<String> firstPathSegment(String path) {
        return Arrays.stream(Optional.ofNullable(path).orElse("").split("/"))
                .filter(segment -> !segment.isBlank())
                .findFirst();
    }

    private static boolean isYoutubeHost(String host) {
        return "youtube.com".equals(host)
                || "m.youtube.com".equals(host)
                || "music.youtube.com".equals(host)
                || "youtube-nocookie.com".equals(host);
    }

    private static String queryValue(String rawQuery, String key) {
        if (rawQuery == null || rawQuery.isBlank()) {
            return null;
        }

        return Arrays.stream(rawQuery.split("&"))
                .map(parameter -> parameter.split("=", 2))
                .filter(parts -> parts.length == 2)
                .filter(parts -> key.equals(urlDecode(parts[0])))
                .map(parts -> urlDecode(parts[1]))
                .findFirst()
                .orElse(null);
    }

    private static String urlDecode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    private static boolean isValidId(String id) {
        return id != null && YOUTUBE_ID_PATTERN.matcher(id).matches();
    }
}
