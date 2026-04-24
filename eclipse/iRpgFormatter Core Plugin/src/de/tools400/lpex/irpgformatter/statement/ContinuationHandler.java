/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.statement;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import de.tools400.lpex.irpgformatter.formatter.RpgleFormatterException;
import de.tools400.lpex.irpgformatter.parser.StatementIdentifier;
import de.tools400.lpex.irpgformatter.parser.StatementType;

/**
 * Handles RPGLE line continuation rules.
 * <p>
 * Supports:
 * <ul>
 * <li>Name/keyword continuation with ellipsis (...)</li>
 * <li>Literal continuation with plus (+) - skips leading whitespace</li>
 * <li>Literal continuation with hyphen (-) - preserves leading whitespace</li>
 * </ul>
 * </p>
 */
public final class ContinuationHandler {

    private static boolean isTestMode = false;

    private ContinuationHandler() {
        // Utility class
    }

    public static void setTestMode(boolean enabled) {
        isTestMode = enabled;
    }

    /**
     * Collects lines from source into complete statements. A statement is
     * complete when it ends with a semicolon or is a single-line statement
     * (like **FREE, comments, or compiler directives).
     *
     * @param sourceLines the source lines to process
     * @return list of complete statements (may span multiple original lines)
     */
    public static CollectedStatement[] collectStatements(String[] sourceLines) throws RpgleFormatterException {
        return collectStatements(sourceLines, 0);
    }

    /**
     * Collects lines from source into complete statements. A statement is
     * complete when it ends with a semicolon or is a single-line statement
     * (like **FREE, comments, or compiler directives).
     *
     * @param sourceLines the source lines to process
     * @param startLineNumber the 0-based line number of the first source line
     *            within the overall source member
     * @return list of complete statements (may span multiple original lines)
     */
    public static CollectedStatement[] collectStatements(String[] sourceLines, int startLineNumber) throws RpgleFormatterException {

        Stack<CollectedStatement> parents = new Stack<>();
        parents.push(null);

        List<CollectedStatement> statements = new LinkedList<>();
        CollectedStatement currentStatement = new CollectedStatement();
        currentStatement.setStartLineNumber(startLineNumber);

        for (int i = 0; i < sourceLines.length; i++) {
            String line = sourceLines[i];

            currentStatement.add(line);

            // Add complete statements
            if (currentStatement.isComplete()) {

                StatementType currentType = currentStatement.getType();

                statements.add(currentStatement);

                if (currentStatement.getType().isStartOfBLock()) {
                    int indent = parents.size() - 1;
                    currentStatement.setIndentLevel(indent);
                    parents.push(currentStatement);
                    // implicitly close DCL-DS statement
                    if (StatementIdentifier.isImplicitlyClosedDclBlock(currentStatement.getStatement())) {
                        parents.pop();
                    }
                } else {
                    CollectedStatement parent = parents.peek();
                    if (parent != null) {
                        StatementType parentEndOfBlockType = parent.getType().getEndOfBlockType();
                        if (currentType == parentEndOfBlockType) {
                            parents.pop();
                        } else {
                            if (currentStatement.getType() == StatementType.DCL_SUBF) {
                                parent.addChild(currentStatement);
                            }
                        }
                    }
                    int indent = parents.size() - 1;
                    currentStatement.setIndentLevel(indent);
                }

                CollectedStatement parent = parents.peek();
                currentStatement = new CollectedStatement(parent);
                currentStatement.setStartLineNumber(startLineNumber + i + 1);
            }
        }

        // Handle incomplete statement at end of file
        if (!currentStatement.isEmpty()) {
            statements.add(currentStatement);
        }

        if (!isTestMode && parents.peek() != null) {
            throw new RpgleFormatterException("Parents stack contains unexpected parent: " + parents.peek().getStatement());
        }

        parents.pop();

        if (!isTestMode && !parents.isEmpty()) {
            throw new RpgleFormatterException("Parents atack is not empty.");
        }

        setTestMode(false);

        return statements.toArray(new CollectedStatement[statements.size()]);
    }
}
