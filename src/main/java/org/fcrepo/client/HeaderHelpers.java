/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree.
 */
package org.fcrepo.client;

import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;
import static java.util.Optional.ofNullable;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Helpers for constructing headers.
 *
 * @author bbpennel
 */
public class HeaderHelpers {

    // Formatter for converting instants to RFC1123 timestamps in UTC
    public static DateTimeFormatter UTC_RFC_1123_FORMATTER = RFC_1123_DATE_TIME.withZone(ZoneId.of("UTC"));

    /**
     * Format a map of values to q values into a quality value formatted header, as per:
     *  https://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html
     *
     * For example, "md5;q=1.0, sha256,sha512;q=0.3"
     *
     * @param qualityMap mapping of values to their quality values.
     * @return Formatted quality value header representation of the provided map.
     */
    public static String formatQualityValues(final Map<String, ? extends Object> qualityMap) {
        // Group header values by common q values
        final Map<Optional<Object>, List<Entry<String, ? extends Object>>> qualityToVal =
                qualityMap.entrySet().stream()
                .collect(Collectors.groupingBy(e -> ofNullable(e.getValue())));

        // Join together all the groupings of q values to header values to produce final header
        return qualityToVal.entrySet().stream()
                .map(e -> {
                    // Join together all the header values with the same q value
                    final String joinedValues = e.getValue().stream()
                            .map(Entry::getKey)
                            .collect(Collectors.joining(","));
                    // Add the q value if one was specified
                    if (e.getKey().isPresent()) {
                        return joinedValues + ";q=" + e.getKey().get();
                    } else {
                        return joinedValues;
                    }
                }) // join together the groupings
                .collect(Collectors.joining(", "));
    }

    private HeaderHelpers() {
    }
}
