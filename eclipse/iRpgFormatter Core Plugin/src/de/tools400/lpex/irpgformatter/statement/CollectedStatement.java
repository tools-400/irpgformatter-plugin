/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.statement;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import de.tools400.lpex.irpgformatter.formatter.RpgleFormatterException;
import de.tools400.lpex.irpgformatter.formatter.UnexpectedStatementTypeException;
import de.tools400.lpex.irpgformatter.parser.StatementIdentifier;
import de.tools400.lpex.irpgformatter.parser.StatementType;
import de.tools400.lpex.irpgformatter.rules.RpgleSourceConstants;
import de.tools400.lpex.irpgformatter.utils.StringUtils;

public class CollectedStatement implements Iterable<String>, RpgleSourceConstants {

    private CollectedStatement parent;
    private CollectedStatement matchingDclProc;

    private List<String> collectedLines;
    private List<String> embeddedComments;
    private boolean isComplete;
    private boolean leftTrimNextLine;

    private boolean insideLiteral;
    private StringBuilder statement;

    private StatementType statementType;
    private int indentLevel;

    private List<CollectedStatement> children;
    private int keywordAlignColumn = -1; // -1 = not yet computed
    private int startLineNumber;

    public CollectedStatement(CollectedStatement parent) {
        this();

        this.parent = parent;
    }

    public CollectedStatement() {

        this.collectedLines = new LinkedList<>();
        this.embeddedComments = new LinkedList<>();
        this.isComplete = false;
        this.leftTrimNextLine = true;

        this.statement = new StringBuilder();

        this.statementType = null;
        this.indentLevel = 0;

        this.children = new LinkedList<>();
    }

    public int getStartLineNumber() {
        return startLineNumber;
    }

    public void setStartLineNumber(int startLineNumber) {
        this.startLineNumber = startLineNumber;
    }

    public int getEndLineNumber() {
        return startLineNumber + numLines() - 1;
    }

    public int getIndentLevel() {
        return indentLevel;
    }

    public void setIndentLevel(int indentLevel) {
        this.indentLevel = indentLevel;
    }

    public CollectedStatement getParent() {
        return parent;
    }

    /**
     * For an END-PROC statement, returns the matching DCL-PROC statement that
     * this END-PROC closes. Returns {@code null} for any other statement type
     * or when no matching DCL-PROC was found.
     */
    public CollectedStatement getMatchingDclProc() {
        return matchingDclProc;
    }

    public boolean isComplete() {
        return isComplete;
    }

    public StatementType getType() throws RpgleFormatterException {

        if (!isComplete) {
            throw new RpgleFormatterException("Unknown statement type. Statement is not complete.");
        }

        return statementType;
    }

    public void add(String line) throws RpgleFormatterException {

        collectedLines.add(line);

        if (isComplete) {
            throw new RpgleFormatterException("Cannot add line segment. Line is already complete.");
        }

        if (isSingleLineStatement(line)) {
            appendLineSegment(line);
            isComplete = true;
            setStatementComplete();
        } else {
            if (!StringUtils.isNullOrEmpty(line)) {
                buildStatement(line);
            }
        }
    }

    public String getStatement() {
        return statement.toString();
    }

    public List<String> getOriginalStatements() {
        return collectedLines;
    }

    public boolean haveChildren() {
        return !children.isEmpty();
    }

    public void addChild(CollectedStatement child) throws RpgleFormatterException {
        if (child.getType() != StatementType.DCL_SUBF) {
            throw new UnexpectedStatementTypeException(child.getType());
        }
        children.add(child);
    }

    public List<CollectedStatement> getChildren() {
        return children;
    }

    public int getKeywordAlignColumn() {
        return keywordAlignColumn;
    }

    public void setKeywordAlignColumn(int column) {
        this.keywordAlignColumn = column;
    }

    private boolean isSingleLineStatement(String line) {

        // **FREE directive
        // Comments
        // Compiler directives (start with /)

        if (isEmpty() && (isFreeFormatMarker(line) || isLineComment(line) || isCompilerDirective(line) || isBlankLine(line))) {
            return true;
        }
        return false;
    }

    private boolean isFreeFormatMarker(String line) {
        return "**FREE".equals(StringUtils.trimR(line).toUpperCase());
    }

    private boolean isLineComment(String line) {
        return line.trim().startsWith(COMMENT);
    }

    private boolean isCompilerDirective(String line) {

        String trimmedLine = line.trim();
        if (!isLineComment(trimmedLine)) {
            if (trimmedLine.startsWith(COMPILER_DIRECTIVE)) {
                return true;
            }
        }

        return false;
    }

    private boolean isBlankLine(String line) {
        return line.trim().length() == 0;
    }

    public boolean isEmpty() {
        return statement.length() == 0;
    }

    private void buildStatement(String line) throws RpgleFormatterException {

        String currentChar;
        String nextChar;
        String nameContinuation;
        boolean isLastChar;

        String lineTrimmed;
        if (!leftTrimNextLine) {
            lineTrimmed = line;
        } else {
            lineTrimmed = StringUtils.trimL(line);
        }

        for (int i = 0; i < lineTrimmed.length(); i++) {

            isLastChar = (i == lineTrimmed.length() - 1);

            currentChar = lineTrimmed.substring(i, i + 1);
            if (i + 1 < lineTrimmed.length()) {
                nextChar = lineTrimmed.substring(i + SINGLE_QUOTE.length());
            } else {
                nextChar = null;
            }

            if (SINGLE_QUOTE.equals(currentChar)) {
                if (SINGLE_QUOTE.equals(nextChar)) {
                    // skip 2 single quotes in a row
                    i++;
                } else {
                    insideLiteral = !insideLiteral;
                }
            }

            if (!insideLiteral) {
                // Outside a literal the is no continuation character or there
                // are three dots indicating that a name is continued in the
                // next line.
                if (SEMICOLON.equals(currentChar)) {
                    appendLineSegment(lineTrimmed);
                    setStatementComplete();
                    return;
                } else if (".".equals(currentChar)) {
                    if (i + 2 < lineTrimmed.length()) {
                        nameContinuation = lineTrimmed.substring(i, i + ELLIPSIS.length());
                        if (nameContinuation.equals(ELLIPSIS)) {
                            appendLineSegment(lineTrimmed, i);
                            collectComment(lineTrimmed, i + ELLIPSIS.length());
                            return;
                        }
                    }
                }
            } else {
                // Inside a literal we expect PLUS and HYPHEN as continuation
                // characters.
                if (isLastChar) {
                    if (PLUS.equals(currentChar)) {
                        appendLineSegment(lineTrimmed, i);
                        return;
                    } else if (HYPHEN.equals(currentChar)) {
                        appendLineSegment(lineTrimmed, i);
                        leftTrimNextLine = false;
                        return;
                    }
                }
            }
        }

        if (isLineComment(lineTrimmed)) {
            // embedded line comment
            collectComment(lineTrimmed.trim());
        } else {
            // simple continued line
            if (lineTrimmed.endsWith(OPEN_BRACKET)) {
                appendLineSegment(lineTrimmed);
            } else {
                appendLineSegment(lineTrimmed + " ");
            }
        }
    }

    private void setStatementComplete() throws RpgleFormatterException {

        statementType = StatementIdentifier.identifyStatementTypeInContext(this);

        if (parent != null) {
            StatementType parentEndOfBLockType = parent.getType().getEndOfBlockType();
            if (statementType == parentEndOfBLockType) {
                // Remember the matching DCL-PROC for END-PROC, before
                // replacing the parent reference.
                if (statementType == StatementType.END_PROC && parent.getType() == StatementType.DCL_PROC) {
                    matchingDclProc = parent;
                }
                // Replace parent with super parent on END-* statements
                parent = parent.getParent();
            }
        }

        isComplete = true;
    }

    private void collectComment(String line) {
        embeddedComments.add(line.trim());
    }

    private void collectComment(String line, int i) {

        String lineSegment = line.substring(i).trim();
        if (isLineComment(lineSegment)) {
            collectComment(lineSegment);
        }
    }

    private void appendLineSegment(String line) {
        appendLineSegment(line, line.length());
    }

    private void appendLineSegment(String line, int i) {

        String lineSegment = line.substring(0, i);
        if (!leftTrimNextLine) {
            leftTrimNextLine = true;
        }

        statement.append(lineSegment);
    }

    public String[] getLines() {
        return collectedLines.toArray(new String[collectedLines.size()]);
    }

    public String getLine(int index) {
        return collectedLines.get(index);
    }

    public int numLines() {
        return collectedLines.size();
    }

    public String[] getEmbeddedComments() {
        return embeddedComments.toArray(new String[embeddedComments.size()]);
    }

    @Override
    public Iterator<String> iterator() {
        return collectedLines.iterator();
    }

    @Override
    public String toString() {

        StringBuilder buffer = new StringBuilder();

        buffer.append(getStatement() + "\n");
        for (CollectedStatement child : children) {
            buffer.append(" - " + child.getStatement() + "\n");
        }

        return buffer.toString();
    }
}
