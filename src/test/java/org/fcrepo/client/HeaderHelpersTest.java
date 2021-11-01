/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree.
 */
package org.fcrepo.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

/**
 * @author bbpennel
 */
public class HeaderHelpersTest {

    @Test
    public void testFormatQualityValues() {
        final Map<String, Double> qualityMap = new HashMap<>();
        qualityMap.put("md5", 1.0);

        assertEquals("md5;q=1.0", HeaderHelpers.formatQualityValues(qualityMap));
    }

    @Test
    public void testFormatQualityValuesNoQValue() {
        final Map<String, Double> qualityMap = new HashMap<>();
        qualityMap.put("md5", null);

        assertEquals("md5", HeaderHelpers.formatQualityValues(qualityMap));
    }

    @Test
    public void testFormatQualityValuesMultipleWithDifferentQ() {
        final Map<String, String> qualityMap = new HashMap<>();
        qualityMap.put("md5", "1.0");
        qualityMap.put("sha-512", "0.4");

        final String result = HeaderHelpers.formatQualityValues(qualityMap);
        assertTrue(result.contains("md5;q=1.0"));
        assertTrue(result.contains("sha-512;q=0.4"));
    }

    @Test
    public void testFormatQualityValuesMultipleSameQ() {
        final Map<String, Double> qualityMap = new HashMap<>();
        qualityMap.put("md5", 1.0);
        qualityMap.put("sha", 1.0);
        qualityMap.put("sha-512", 0.4);

        final String result = HeaderHelpers.formatQualityValues(qualityMap);
        // can't guarantee order
        assertTrue(result.contains("md5,sha;q=1.0") || result.contains("sha,md5;q=1.0"));
        assertTrue(result.contains("sha-512;q=0.4"));
    }

    @Test
    public void testFormatQualityValuesMultipleQ() {
        final Map<String, Double> qualityMap = new HashMap<>();
        qualityMap.put("md5", null);
        qualityMap.put("sha", null);

        final String result = HeaderHelpers.formatQualityValues(qualityMap);
        // can't guarantee order
        assertTrue(result.contains("md5,sha") || result.contains("sha,md5"));
    }
}
