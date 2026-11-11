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

        Stack<ConditionalGroup> conditionalGroups = new Stack<>();

        boolean haveOnlySingleLineStatements = false;
        boolean isAfterEof = false;
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

                    if (currentStatement.getType() == StatementType.COMPILER_DIRECTIVE) {
                        DirectiveType directiveType = DirectiveType.of(currentStatement.getStatement());
                        nestConditionalGroup(conditionalGroups, parents, currentStatement, directiveType);

                        // the compiler ignores all lines following an /EOF
                        // directive outside of a conditional group
                        if (directiveType == DirectiveType.EOF && conditionalGroups.isEmpty()) {
                            isAfterEof = true;
                            haveOnlySingleLineStatements = true;
                        }
                    }
                }

                // all statements following the first compile time array are
                // single line statements by definition
                if (currentStatement.getType() == StatementType.COMPILE_TIME_ARRAY) {
                    haveOnlySingleLineStatements = true;
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

        // Calculation blocks that are still open, e.g. because the selected
        // lines end in front of the ENDIF, only affect the indentation.
        while (parents.peek() != null && parents.peek().getType().isCalculation()) {
            parents.pop();
        }

        if (!isTestMode && parents.peek() != null) {
            throw new RpgleFormatterException("Parents stack contains unexpected parent: " + parents.peek().getStatement());
        }

        parents.pop();

        if (!isTestMode && !parents.isEmpty()) {
            throw new RpgleFormatterException("Parents stack is not empty.");
        }

        setTestMode(false);

        CollectedStatement[] collectedStatements = statements.toArray(new CollectedStatement[statements.size()]);
        indentComments(collectedStatements);

        return collectedStatements;
    }

    /**
     * Gives each comment line the indent level of the next statement, like the
     * IBM formatter does. Blank lines and compiler directives in between are
     * skipped. A comment without a following statement keeps the indent level
     * of the blocks that enclose it.
     *
     * @param statements the collected statements
     */
    private static void indentComments(CollectedStatement[] statements) throws RpgleFormatterException {

        CollectedStatement nextStatement = null;
        for (int i = statements.length - 1; i >= 0; i--) {

            CollectedStatement statement = statements[i];
            if (!statement.isComplete() || statement.isAfterEof()) {
                nextStatement = null;
                continue;
            }

            switch (statement.getType()) {
            case COMMENT:
                statement.setFollowingStatement(nextStatement);
                if (nextStatement != null) {
                    statement.setIndentLevel(nextStatement.getIndentLevel());
                    statement.setSelectDepth(nextStatement.getSelectDepth());
                }
                break;
            case BLANK:
            case COMPILER_DIRECTIVE:
            case FREE_DIRECTIVE:
                break;
            case COMPILE_TIME_ARRAY:
                // the lines that follow are data
                nextStatement = null;
                break;
            default:
                nextStatement = statement;
                break;
            }
        }
    }

    /**
     * Places a complete statement into the block structure and sets its indent
     * level: a statement that starts a block is pushed onto the stack of
     * parents, the statement that ends the block pops it, together with the
     * calculation blocks that are still open within it. Sub-fields are added
     * to their parent.
     *
     * @param parents the blocks that enclose the statement, <code>null</code>
     *        at the bottom
     * @param currentStatement the complete statement
     */
    private static void nestStatement(Stack<CollectedStatement> parents, CollectedStatement currentStatement) throws RpgleFormatterException {

        StatementType currentType = currentStatement.getType();
        if (currentType.isStartOfBLock()) {

            setIndent(currentStatement, parents, 0);
            parents.push(currentStatement);
            // single line DCL-DS or DCL-PI/DCL-PR statement
            if (StatementIdentifier.isSingleLineStatement(currentStatement.getStatement())) {
                parents.pop();
            }
        } else if (currentType.isBranchOfBlock()) {
            // ELSE, WHEN, ON-ERROR etc. are placed on the level of the
            // statement that starts the block
            setIndent(currentStatement, parents, -1);
        } else {
            closeOpenCalculationBlocks(parents, currentType);
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
            setIndent(currentStatement, parents, 0);
        }
    }

    /**
     * Pops the calculation blocks, that are still open, when the statement ends
     * the block that encloses them, such as an <code>IF</code> without
     * <code>ENDIF</code> at <code>END-PROC</code>. Otherwise the enclosing block
     * could not be closed. Like at the end of the source, the open calculation
     * blocks only affect the indentation.
     *
     * @param parents the blocks that enclose the statement, <code>null</code>
     *        at the bottom
     * @param currentType the type of the statement
     */
    private static void closeOpenCalculationBlocks(Stack<CollectedStatement> parents, StatementType currentType)
        throws RpgleFormatterException {

        int index = parents.size() - 1;
        while (parents.get(index) != null && parents.get(index).getType().isCalculation()) {
            index--;
        }

        CollectedStatement block = parents.get(index);
        if (block != null && currentType == block.getType().getEndOfBlockType()) {
            parents.setSize(index + 1);
        }
    }

    /**
     * Sets the indent level of a statement from the blocks that enclose it and
     * counts the select groups among them.
     *
     * @param statement the statement
     * @param parents the blocks that enclose the statement
     * @param levelOffset -1 for a branch of a block, such as <code>ELSE</code>,
     *        otherwise 0
     */
    private static void setIndent(CollectedStatement statement, Stack<CollectedStatement> parents, int levelOffset)
        throws RpgleFormatterException {

        statement.setIndentLevel(Math.max(0, parents.size() - 1 + levelOffset));

        int selectDepth = 0;
        for (CollectedStatement parent : parents) {
            if (parent != null && parent.getType() == StatementType.SELECT) {
                selectDepth++;
            }
        }
        statement.setSelectDepth(selectDepth);
    }

    /**
     * Starts each branch of a conditional group with the blocks that are open
     * at its <code>/IF</code> directive and continues after the
     * <code>/ENDIF</code> directive with the blocks that are open at the end of
     * the first branch. All directives of a group get the indent level of the
     * <code>/IF</code> directive. Directives without a matching
     * <code>/IF</code> directive are ignored.
     * <p>
     * If the branches end with different blocks, the <code>/ENDIF</code>
     * directive refers to the <code>/IF</code> directive of the group, see
     * {@link CollectedStatement#getUnbalancedIfDirective()}.
     * </p>
     *
     * @param conditionalGroups the conditional groups that enclose the
     *        directive
     * @param parents the blocks that enclose the directive
     * @param directive the compiler directive
     * @param directiveType the type of the compiler directive
     */
    private static void nestConditionalGroup(Stack<ConditionalGroup> conditionalGroups, Stack<CollectedStatement> parents,
        CollectedStatement directive, DirectiveType directiveType) throws RpgleFormatterException {

        switch (directiveType) {
        case IF:
            conditionalGroups.push(new ConditionalGroup(directive, parents));
            break;
        case ELSEIF:
        case ELSE:
            if (!conditionalGroups.isEmpty()) {
                ConditionalGroup group = conditionalGroups.peek();
                group.startNextBranch(parents, directiveType == DirectiveType.ELSE);
                directive.setIndentLevel(group.getIndentLevel());
                directive.setSelectDepth(group.getIfDirective().getSelectDepth());
            }
            break;
        case ENDIF:
            if (!conditionalGroups.isEmpty()) {
                ConditionalGroup group = conditionalGroups.pop();
                if (!group.end(parents)) {
                    directive.setUnbalancedIfDirective(group.getIfDirective());
                }
                directive.setIndentLevel(group.getIndentLevel());
                directive.setSelectDepth(group.getIfDirective().getSelectDepth());
            }
            break;
        default:
            break;
        }
    }
}
