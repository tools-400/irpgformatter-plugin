/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.rules.statements;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.tools400.lpex.irpgformatter.formatter.RpgleFormatterException;
import de.tools400.lpex.irpgformatter.parser.StatementType;
import de.tools400.lpex.irpgformatter.rules.IStatementListRule;
import de.tools400.lpex.irpgformatter.statement.CollectedStatement;

/**
 * Removes empty comment lines ({@code //}) that serve no structural purpose.
 * <p>
 * A three-pass algorithm is applied:
 * <ol>
 * <li>Mark all bare {@code //} lines as suppressed (except inside ILEDoc blocks
 *     and {@code @formatter:off} regions).</li>
 * <li>When a structural marker (separator line or ILEDoc delimiter) is found,
 *     scan backward and restore suppressed lines that form the comment block
 *     immediately preceding the marker. The backward scan stops at the first
 *     non-comment line or at a {@code @formatter:off} directive.</li>
 * <li>From the boundary found in step 2, scan forward and re-suppress any
 *     leading empty {@code //} lines until the first non-empty comment.</li>
 * </ol>
 */
public class RemoveEmptyCommentLinesRule implements IStatementListRule {

    private static final String EMPTY_COMMENT = "//";

    private static final Pattern ILEDOC_DELIMITER =
        Pattern.compile("^///\\s*$");

    private static final Pattern SEPARATOR_PATTERN =
        Pattern.compile("^//\\s*([-=*+~])\\s*(\\1\\s*){2,}$");

    private static final Pattern FORMATTER_DIRECTIVE_PATTERN =
        Pattern.compile("^//\\s*@formatter:(on|off)\\s*$", Pattern.CASE_INSENSITIVE);

    private static final Pattern FORMATTER_OFF_PATTERN =
        Pattern.compile("^//\\s*@formatter:off\\s*$", Pattern.CASE_INSENSITIVE);

    @Override
    public boolean[] apply(CollectedStatement[] statements) throws RpgleFormatterException  {

        boolean[] suppress = new boolean[statements.length];
        boolean inIleDoc = false;
        boolean formatterDisabled = false;

        
        for (int i = 0; i < statements.length; i++) {
            StatementType type = statements[i].getType();
            if(type==StatementType.COMPILE_TIME_ARRAY) {
                break;
            }
            
            String text = statements[i].getStatement().trim();

            // Track @formatter:on/off regions; do not touch statements inside them
            if (type == StatementType.COMMENT) {
                Matcher m = FORMATTER_DIRECTIVE_PATTERN.matcher(text);
                if (m.matches()) {
                    formatterDisabled = "off".equalsIgnoreCase(m.group(1));
                    continue;
                }
            }

            if (formatterDisabled) {
                continue;
            }

            // ILEDoc delimiter (exactly "///") toggles ILEDoc mode and acts as
            // structural marker
            if (type == StatementType.COMMENT && ILEDOC_DELIMITER.matcher(text).matches()) {
                inIleDoc = !inIleDoc;
                processStructuralMarker(statements, suppress, i);
                continue;
            }

            if (inIleDoc) {
                continue;
            }

            if (type == StatementType.COMMENT) {
                if (text.equals(EMPTY_COMMENT)) {
                    suppress[i] = true;
                } else if (SEPARATOR_PATTERN.matcher(text).matches()) {
                    processStructuralMarker(statements, suppress, i);
                }
            }
        }

        return suppress;
    }

    private void processStructuralMarker(CollectedStatement[] statements, boolean[] suppress, int markerIndex) throws RpgleFormatterException {

        // Step 2: scan backward, restoring suppressed comments until a
        // non-comment
        // line or a @formatter:off boundary is reached
        int codeLineIndex = -1;
        boolean stoppedAtFormatterOff = false;
        for (int j = markerIndex - 1; j >= 0; j--) {
            if (statements[j].getType() != StatementType.COMMENT) {
                codeLineIndex = j;
                break;
            }
            String text = statements[j].getStatement().trim();
            if (FORMATTER_OFF_PATTERN.matcher(text).matches()) {
                codeLineIndex = j;
                stoppedAtFormatterOff = true;
                break;
            }
            suppress[j] = false;
        }

        // Step 3: scan forward from the boundary, re-suppressing leading empty comments
        // until the first non-empty comment is found.
        // Skipped entirely when the backward scan stopped at a @formatter:off boundary,
        // because the region between the boundary and the marker may contain disabled lines.
        if (!stoppedAtFormatterOff) {
            for (int j = codeLineIndex + 1; j < markerIndex; j++) {
                if (statements[j].getType() != StatementType.COMMENT) {
                    break;
                }
                if (!statements[j].getStatement().trim().equals(EMPTY_COMMENT)) {
                    break;
                }
                suppress[j] = true;
            }
        }
    }
}
