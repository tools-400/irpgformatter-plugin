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
import de.tools400.lpex.irpgformatter.parser.DirectiveType;
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
     * <p>
     * Lines following an /EOF directive outside of a conditional group are
     * ignored by the compiler. They are collected as single-line statements
     * and marked with {@link CollectedStatement#isAfterEof()}.
     * </p>
     *
     * @param sourceLines the source lines to process
     * @param startLineNumber the line number of the first source line within
     *        the overall source member; the formatter counts from 1
     * @return list of complete statements (may span multiple original lines)
     */
    public static CollectedStatement[] collectStatements(String[] sourceLines, int startLineNumber) throws RpgleFormatterException {

        Stack<CollectedStatement> parents = new Stack<>();
        parents.push(null);

        List<CollectedStatement> statements = new LinkedList<>();
        CollectedStatement currentStatement = new CollectedStatement();
        currentStatement.setStartLineNumber(startLineNumber);

        boolean haveOnlySingleLineStatements = false;
        boolean isAfterEof = false;
        int conditionalGroupDepth = 0;
        for (int i = 0; i < sourceLines.length; i++) {
            String line = sourceLines[i];

            currentStatement.add(line);

            if (haveOnlySingleLineStatements) {
                currentStatement.enforceComplete();
            }

            // Add complete statements
            if (currentStatement.isComplete()) {

                statements.add(currentStatement);
                if (isAfterEof) {
                    currentStatement.setAfterEof();
                } else {
                    nestStatement(parents, currentStatement);
                }

                // all statements following the first compile time array are
                // single line statements by definition
                if (currentStatement.getType() == StatementType.COMPILE_TIME_ARRAY) {
                    haveOnlySingleLineStatements = true;
                }

                // the compiler ignores all lines following an /EOF directive
                // outside of a conditional group
                if (currentStatement.getType() == StatementType.COMPILER_DIRECTIVE) {
                    switch (DirectiveType.of(currentStatement.getStatement())) {
                    case IF:
                        conditionalGroupDepth++;
                        break;
                    case ENDIF:
                        conditionalGroupDepth = Math.max(0, conditionalGroupDepth - 1);
                        break;
                    case EOF:
                        if (conditionalGroupDepth == 0) {
                            isAfterEof = true;
                            haveOnlySingleLineStatements = true;
                        }
                        break;
                    default:
                        break;
                    }
                }

                // start a new statement
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
            throw new RpgleFormatterException("Parents stack is not empty.");
        }

        setTestMode(false);

        return statements.toArray(new CollectedStatement[statements.size()]);
    }

    /**
     * Places a complete statement into the block structure and sets its indent
     * level: a statement that starts a block is pushed onto the stack of
     * parents, the statement that ends the block pops it. Sub-fields are added
     * to their parent.
     *
     * @param parents the blocks that enclose the statement, <code>null</code>
     *        at the bottom
     * @param currentStatement the complete statement
     */
    private static void nestStatement(Stack<CollectedStatement> parents, CollectedStatement currentStatement) throws RpgleFormatterException {

        StatementType currentType = currentStatement.getType();
        if (currentType.isStartOfBLock()) {

            // Experimental:
            // Pop parent if statement type matches previous type.
            // Actually we needed an Abstract Syntax Tree (AST) for
            // correct source formatting in order to have cleanly
            // identified statements, if the developer uses includes and
            // conditional compiling. This fix at least makes the
            // following statement work:
            // @formatter:off
            //   /If DEFINED (DEFINE_PROCPTR)
            //   Dcl-Pr SrvPgmFilter_delete Ind
            //     ExtProc(g_pFilter_delete);
            //   /Else
            //     Dcl-Pr SrvPgmFilter_delete Ind
            //     ExtProc('SrvPgmFilter_delete');
            //   /Endif
            //     io_pHandle Pointer;
            //   End-Pr;
            // @formatter:on

            if (parents.peek() != null && parents.peek().getType() == currentType) {
                parents.pop();
            }

            int indent = parents.size() - 1;
            currentStatement.setIndentLevel(indent);
            parents.push(currentStatement);
            // single line DCL-DS or DCL-PI/DCL-PR statement
            if (StatementIdentifier.isSingleLineStatement(currentStatement.getStatement())) {
                parents.pop();
            }
        } else {
            CollectedStatement parent = parents.peek();
            if (parent != null) {
                StatementType parentEndOfBlockType = parent.getType().getEndOfBlockType();
                if (currentType == parentEndOfBlockType) {
                    parents.pop();
                } else {
                    if (currentType == StatementType.DCL_SUBF) {
                        parent.addChild(currentStatement);
                    }
                }
            }
            int indent = parents.size() - 1;
            currentStatement.setIndentLevel(indent);
        }
    }
}
