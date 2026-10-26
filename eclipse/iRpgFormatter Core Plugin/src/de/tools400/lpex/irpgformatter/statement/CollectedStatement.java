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

import de.tools400.lpex.irpgformatter.formatter.CannotAddLineSegmentException;
import de.tools400.lpex.irpgformatter.formatter.IncompleteStatementException;
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

    public void enforceComplete() throws RpgleFormatterException {
        setStatementComplete();
    }

    public StatementType getType() throws RpgleFormatterException {

        if (!isComplete) {
            throw new IncompleteStatementException(startLineNumber, getStatement());
        }

        return statementType;
    }

    public void add(String line) throws RpgleFormatterException {

        collectedLines.add(line);

        if (isComplete) {
            throw new CannotAddLineSegmentException(getStartLineNumber());
        }

        if (isSingleLineStatement(line)) {
            appendLineSegment(line);
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
            throw new UnexpectedStatementTypeException(child.getType(), getStartLineNumber(), child.getStatement());
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
        // Compile Time Arrays (start with **CDATA)

        if (isEmpty()
            && (isFreeFormatMarker(line) || isLineComment(line) || isCompilerDirective(line) || isCompileTimeArray(line) || isBlankLine(line))) {
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

    private boolean isCompileTimeArray(String line) {

        if (line.startsWith(COMPILE_TIME_ARRAY)) {
            return true;
        }

        return false;
    }

    private boolean isBlankLine(String line) {
        return line.trim().length() == 0;
    }

    public boolean isEmpty() {
        return statement.length() == 0;
    }

    /**
     * Adds a line to the statement. Outside a literal a semicolon ends the
     * statement and three dots continue a name on the next line; inside a
     * literal a plus or hyphen at the end of the line continues the literal.
     */
    private void buildStatement(String line) throws RpgleFormatterException {

        String lineTrimmed;
        if (!leftTrimNextLine) {
            lineTrimmed = line;
        } else {
            lineTrimmed = StringUtils.trimL(line);
        }

        for (int i = 0; i < lineTrimmed.length(); i++) {

            if (lineTrimmed.startsWith(SINGLE_QUOTE, i)) {
                if (lineTrimmed.startsWith(SINGLE_QUOTE, i + 1)) {
                    // Two single quotes: an escaped quote
                    i++;
                } else {
                    insideLiteral = !insideLiteral;
                }
            }

            if (!insideLiteral) {
                if (lineTrimmed.startsWith(SEMICOLON, i)) {
                    appendLineSegment(lineTrimmed);
                    setStatementComplete();
                    return;
                }
                if (lineTrimmed.startsWith(ELLIPSIS, i)) {
                    continueName(lineTrimmed, i);
                    return;
                }
                if (lineTrimmed.startsWith(COMMENT, i)) {
                    continueBeforeComment(lineTrimmed, i);
                    return;
                }
            } else if (isLiteralContinuation(lineTrimmed, i)) {
                continueLiteral(lineTrimmed, i);
                return;
            }
        }

        appendLineOrComment(lineTrimmed);
    }

    /**
     * Appends the name up to the three dots, which continue it on the next
     * line, and collects a comment behind them.
     */
    private void continueName(String line, int ellipsis) {
        appendLineSegment(line, ellipsis);
        collectComment(line, ellipsis + ELLIPSIS.length());
    }

    /**
     * Appends the code in front of a comment, which ends the line, and
     * collects the comment. The statement goes on in the next line, also if the
     * comment contains a semicolon or a single quote. Like a whole comment
     * line, the comment is put in front of the formatted statement.
     */
    private void continueBeforeComment(String line, int comment) {

        String code = StringUtils.trimR(line.substring(0, comment));
        if (!code.isEmpty()) {
            appendLineOrComment(code);
        }
        collectComment(line.substring(comment));
    }

    /**
     * Returns <code>true</code>, if the character inside a literal is a plus
     * or hyphen, which continues the literal: the last character of the line
     * except blanks, which the compiler ignores there.
     */
    private boolean isLiteralContinuation(String line, int i) {
        return (line.startsWith(PLUS, i) || line.startsWith(HYPHEN, i)) && line.substring(i + 1).trim().isEmpty();
    }

    /**
     * Appends the literal up to the continuation character. A plus continues
     * the literal at the first non-blank character of the next line, a hyphen
     * at its first character.
     */
    private void continueLiteral(String line, int continuation) {

        appendLineSegment(line, continuation);
        if (line.startsWith(HYPHEN, continuation)) {
            leftTrimNextLine = false;
        }
    }

    /**
     * Collects a comment line or appends a line, that continues the statement,
     * with a blank, unless it ends with an opening bracket.
     */
    private void appendLineOrComment(String line) {

        if (isLineComment(line)) {
            collectComment(line.trim());
        } else if (line.endsWith(OPEN_BRACKET)) {
            appendLineSegment(line);
        } else {
            appendLineSegment(line + " ");
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
