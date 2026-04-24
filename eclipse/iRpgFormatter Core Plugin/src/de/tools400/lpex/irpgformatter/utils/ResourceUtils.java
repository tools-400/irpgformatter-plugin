/*******************************************************************************
 * Copyright (c) 2012-2026 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.utils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

public class ResourceUtils {

    private ResourceUtils() {
        // Utility class
    }

    /**
     * Loads keywords from a properties resource file.
     *
     * @param classpathResource path for classpath loading (e.g.,
     *        /keywords.properties)
     * @param bundleResource path for plugin bundle loading (e.g.,
     *        /resources/keywords.properties)
     */
    public static Map<String, String> loadFromPropertiesResource(String classpathResource) {
        Map<String, String> result = new LinkedHashMap<>();
        try {
            URL url = KeywordUtils.class.getResource(classpathResource);
            if (url != null) {
                try (InputStream is = url.openStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                    Properties props = new Properties();
                    props.load(reader);
                    for (String key : props.stringPropertyNames()) {
                        String value = props.getProperty(key);
                        // Store with uppercase key for consistent matching
                        result.put(key.toUpperCase(), value);
                    }
                }
            } else {
                throw new FileNotFoundException("Resource '" + classpathResource + "' not found.");
            }
        } catch (IOException e) {
            // Log error but continue with empty map
            e.printStackTrace();
            System.err.println("Error loading keywords from " + classpathResource + ": " + e.getMessage());
        }
        return Collections.unmodifiableMap(result);
    }

    /**
     * Loads keywords from a properties resource file.
     *
     * @param classpathResource path for classpath loading (e.g.,
     *        /keywords.properties)
     * @param bundleResource path for plugin bundle loading (e.g.,
     *        /resources/keywords.properties)
     */
    public static String loadFromStringResource(String classpathResource) {
        StringBuilder lines = new StringBuilder();
        try {
            URL url = KeywordUtils.class.getResource(classpathResource);
            if (url != null) {
                try (InputStream is = url.openStream()) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(is));
                    String line;
                    while ((line = br.readLine()) != null) {
                        lines.append(line).append("\n");
                    }
                }
            } else {
                throw new FileNotFoundException("Resource '" + classpathResource + "' not found.");
            }
        } catch (IOException e) {
            // Log error but continue with empty map
            e.printStackTrace();
            System.err.println("Error loading string from " + classpathResource + ": " + e.getMessage());
        }
        return lines.toString();
    }
}
