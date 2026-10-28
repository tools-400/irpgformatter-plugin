/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.preferencepages;

/**
 * Comment lines of the preview source, to which the preview scrolls, when the
 * user changes a setting. Each comment must exist in
 * <code>resources/default_preview_source.rpgle</code>; the unit test
 * <code>PreviewCommentsTest</code> checks that.
 */
public final class PreviewComments {

    public static final String USE_CONST_KEYWORD = "// Use const() in dcl-c statements.";
    public static final String PUT_DELIMITER_BEFORE_PARAMETER = "// Put delimiter before parameter.";
    public static final String ALIGN_SUB_FIELDS = "// Align sub-fields/parameters.";
    public static final String BREAK_NAME_ON_CASE_CHANGE = "// Break name on case change.";
    public static final String BREAK_BEFORE_KEYWORD = "// Break before keyword.";
    public static final String SORT_CONST_VALUE_TO_END = "// Sort const/value to end.";
    public static final String REPLACE_PI_NAME = "// Replace dcl-pi name with *N.";
    public static final String REMOVE_END_PROC_NAME = "// Remove end-proc name.";
    public static final String UNINDENT_COMPILER_DIRECTIVES = "// Unindent compiler directives.";
    public static final String FORMAT_CALCULATIONS = "// Format calculations.";
    public static final String CONDITION_PARENTHESES = "// Outer parentheses of conditions.";
    public static final String FORMAT_OPERATION_CODES_AND_BUILT_IN_FUNCTIONS = "// Casing of operation codes and built-in functions.";
    public static final String REMOVE_EMPTY_COMMENT_LINES = "// Remove empty comment lines.";
    public static final String REMOVE_EMPTY_LINES_BEFORE_DCL_PI = "// Remove empty comment lines before dcl-pi.";

    private PreviewComments() {
        // Utility class
    }

    /**
     * Returns the index of the first line, that is the given comment. The
     * number of blanks does not matter, so that a comment of a comment block
     * like <code>//  Remove empty comment lines.</code> is found, too.
     *
     * @param lines the lines of the preview source
     * @param comment the comment, e.g. {@link #BREAK_BEFORE_KEYWORD}
     * @return index of the line or -1, if no line is the comment
     */
    public static int findLine(String[] lines, String comment) {

        String normalizedComment = normalizeSpaces(comment);
        for (int i = 0; i < lines.length; i++) {
            if (normalizeSpaces(lines[i]).equals(normalizedComment)) {
                return i;
            }
        }

        return -1;
    }

    private static String normalizeSpaces(String line) {
        return line.trim().replaceAll("\\s+", " ");
    }
}
