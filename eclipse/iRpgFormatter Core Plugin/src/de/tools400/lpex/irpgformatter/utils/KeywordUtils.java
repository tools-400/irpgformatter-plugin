/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import de.tools400.lpex.irpgformatter.preferencepages.keywordeditor.KeywordEntry;

public final class KeywordUtils {

    // Classpath resource paths (when resources folder is on classpath)
    private static final String DATA_TYPES_RESOURCE = "/data_types.properties";
    private static final String DECLARATION_TYPES_RESOURCE = "/declaration_types.properties";
    private static final String KEYWORDS_RESOURCE = "/keywords.properties";
    private static final String SPECIAL_WORDS_RESOURCE = "/special_words.properties";
    // Bundle resource paths (when running as plugin)
    private static final String DATA_TYPES_BUNDLE_RESOURCE = "/resources" + DATA_TYPES_RESOURCE;
    private static final String DECLARATION_TYPES_BUNDLE_RESOURCE = "/resources" + DECLARATION_TYPES_RESOURCE;
    private static final String KEYWORDS_BUNDLE_RESOURCE = "/resources" + KEYWORDS_RESOURCE;
    private static final String SPECIAL_WORDS_BUNDLE_RESOURCE = "/resources" + SPECIAL_WORDS_RESOURCE;

    private KeywordUtils() {
        // Utility class
    }

    /**
     * Gets the default data-types from the resource file.
     *
     * @return map of default data-types
     */
    public static Map<String, String> getDefaultDataTypes() {
        return ResourceUtils.loadFromPropertiesResource(DATA_TYPES_RESOURCE);
    }

    /**
     * Gets the default declaration-types from the resource file.
     *
     * @return map of default declaration-types
     */
    public static Map<String, String> getDefaultDeclarationTypes() {
        return ResourceUtils.loadFromPropertiesResource(DECLARATION_TYPES_RESOURCE);
    }

    /**
     * Gets the default keywords from the resource file.
     *
     * @return map of default keywords
     */
    public static Map<String, String> getDefaultKeywords() {
        return ResourceUtils.loadFromPropertiesResource(KEYWORDS_RESOURCE);
    }

    /**
     * Gets the default keyword parameters from the resource file.
     *
     * @return map of default keyword parameters
     */
    public static Map<String, String> getDefaultSpecialWords() {
        return ResourceUtils.loadFromPropertiesResource(SPECIAL_WORDS_RESOURCE);
    }

    /**
     * Converts a data-types map to a display string (KEY=value format, one per
     * line).
     *
     * @param dataTypes the data-types map
     * @return formatted string
     */
    public static String dataTypesToString(Map<String, String> dataTypes) {
        return formatKeywordProperties(dataTypes);
    }

    /**
     * Converts a declaration-types map to a display string (KEY=value format,
     * one per line).
     *
     * @param declarationTypes the declaration-types map
     * @return formatted string
     */
    public static String declarationTypesToString(Map<String, String> declarationTypes) {
        return formatKeywordProperties(declarationTypes);
    }

    /**
     * Converts a keyword map to a display string (KEY=value format, one per
     * line).
     *
     * @param keywords the keywords map
     * @return formatted string
     */
    public static String keywordsToString(Map<String, String> keywords) {
        return formatKeywordProperties(keywords);
    }

    /**
     * Parses a display string to a keyword map.
     *
     * @param text the text (KEY=value format, one per line)
     * @return map of keywords
     */
    public static Map<String, String> stringToKeywords(String text) {
        return parseKeywordProperties(text);
    }

    /**
     * Converts a Map to a List of KeywordEntry objects.
     */
    public static List<KeywordEntry> mapToEntries(Map<String, String> map) {
        List<KeywordEntry> entries = new ArrayList<>();
        for (Map.Entry<String, String> e : map.entrySet()) {
            entries.add(new KeywordEntry(e.getKey(), e.getValue()));
        }
        // Sort by key
        entries.sort((a, b) -> a.getKey().compareToIgnoreCase(b.getKey()));
        return entries;
    }

    /**
     * Converts a List of KeywordEntry objects to a Map.
     */
    public static Map<String, String> entriesToMap(List<KeywordEntry> entries) {
        Map<String, String> map = new LinkedHashMap<>();
        for (KeywordEntry entry : entries) {
            map.put(entry.getKey(), entry.getValue());
        }
        return map;
    }

    /**
     * Parses a keyword properties string (KEY=value format, one per line).
     */
    private static Map<String, String> parseKeywordProperties(String text) {
        Map<String, String> result = new LinkedHashMap<>();
        try (BufferedReader reader = new BufferedReader(new StringReader(text))) {
            Properties props = new Properties();
            props.load(reader);
            for (String key : props.stringPropertyNames()) {
                String value = props.getProperty(key);
                // Store with uppercase key for consistent matching
                result.put(key.toUpperCase(), value);
            }
        } catch (IOException e) {
            // Should not happen with StringReader
        }
        return Collections.unmodifiableMap(result);
    }

    /**
     * Formats a keyword map as a properties string (KEY=value format, sorted by
     * key).
     */
    private static String formatKeywordProperties(Map<String, String> keywords) {
        // Sort by key (case-insensitive)
        Map<String, String> sorted = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        sorted.putAll(keywords);

        StringWriter sw = new StringWriter();
        for (Map.Entry<String, String> entry : sorted.entrySet()) {
            sw.write(entry.getKey());
            sw.write("=");
            sw.write(entry.getValue());
            sw.write(System.lineSeparator());
        }
        return sw.toString();
    }
}
