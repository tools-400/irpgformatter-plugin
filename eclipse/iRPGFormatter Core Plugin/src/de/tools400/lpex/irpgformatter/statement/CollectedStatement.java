/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.statement;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import de.tools400.lpex.irpgformatter.formatter.CannotAddLineSegmentException;
import de.tools400.lpex.irpgformatter.formatter.IncompleteStatementException;
import de.tools400.lpex.irpgformatter.formatter.RpgleFormatterException;
import de.tools400.lpex.irpgformatter.formatter.UnexpectedStatementTypeException;
import de.tools400.lpex.irpgformatter.parser.DirectiveType;
import de.tools400.lpex.irpgformatter.parser.StatementIdentifier;
import de.tools400.lpex.irpgformatter.parser.StatementType;
import de.tools400.lpex.irpgformatter.rules.RpgleSourceConstants;
import de.tools400.lpex.irpgformatter.utils.StringUtils;

public class CollectedStatement implements Iterable<String>, RpgleSourceConstants {

    private CollectedStatement parent;
    private CollectedStatement matchingDclProc;
    private CollectedStatement followingStatement;

    private List<String> collectedLines;
    private List<Boolean> leadingBlanksSignificant;
    private List<String> embeddedComments;
    private List<EmbeddedDirective> embeddedDirectives;
    private boolean isComplete;
    private boolean leftTrimNextLine;
    private boolean isAfterEof;
    private CollectedStatement unbalancedIfDirective;

    private boolean insideLiteral;
    private boolean insideSqlBlockComment;
    private boolean insideDelimitedIdentifier;
    private boolean hasContinuedLiteral;
    private StringBuilder statement;

    private StatementType statementType;
    private int indentLevel;
    private int selectDepth;

    private List<CollectedStatement> children;
    private int keywordAlignColumn = -1; // -1 = not yet computed
    private int startLineNumber;

    public CollectedStatement(CollectedStatement parent) {
        this();

        this.parent = parent;
    }

    public CollectedStatement() {

        this.collectedLines = new LinkedList<>();
        this.leadingBlanksSignificant = new ArrayList<>();
        this.embeddedComments = new LinkedList<>();
        this.embeddedDirectives = new LinkedList<>();
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

    /**
     * Returns the number of select groups that enclose the statement. The
     * branches of a select group and their statements get the extra indent of
     * the IBM preference "Indent WHEN/OTHER within SELECT statement".
     */
    public int getSelectDepth() {
        return selectDepth;
    }

    public void setSelectDepth(int selectDepth) {
        this.selectDepth = selectDepth;
    }

    public CollectedStatement getParent() {
        return parent;
    }

    /**
     * For a comment line, returns the statement, that follows it. Blank lines
     * and compiler directives in between are skipped. The comment line gets
     * the indent of this statement. Returns <code>null</code> for any other
     * statement or if no statement follows.
     */
    public CollectedStatement getFollowingStatement() {
        return followingStatement;
    }

    public void setFollowingStatement(CollectedStatement followingStatement) {
        this.followingStatement = followingStatement;
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
        leadingBlanksSignificant.add(!leftTrimNextLine);

        if (isComplete) {
            throw new CannotAddLineSegmentException(getStartLineNumber());
        }

        if (isSingleLineStatement(line)) {
            appendLineSegment(line);
            setStatementComplete();
        } else if (!insideLiteral && !insideSqlBlockComment && isConditionalDirective(line)) {
            // /IF, /ELSEIF, /ELSE and /ENDIF may appear within a free-form
            // declaration. The directive must keep its line, therefore it is
            // not added to the statement.
            embeddedDirectives.add(new EmbeddedDirective(line, statement.length()));
        } else {
            if (!StringUtils.isNullOrEmpty(line)) {
                buildStatement(line);
            }
        }
    }

    /**
     * Returns <code>true</code>, if the statement contains compiler directives
     * between its lines, such as <code>/IF</code> ... <code>/ENDIF</code>
     * within a <code>DCL-S</code> statement.
     */
    public boolean hasEmbeddedDirectives() {
        return !embeddedDirectives.isEmpty();
    }

    /**
     * Returns the compiler directives between the lines of the statement in
     * the order of the source.
     */
    public List<EmbeddedDirective> getEmbeddedDirectives() {
        return embeddedDirectives;
    }

    /**
     * Returns <code>true</code>, if the statement follows an
     * <code>/EOF</code> directive outside of a conditional group. The compiler
     * ignores such lines.
     */
    public boolean isAfterEof() {
        return isAfterEof;
    }

    public void setAfterEof() {
        this.isAfterEof = true;
    }

    /**
     * For an <code>/ENDIF</code> directive, returns the <code>/IF</code>
     * directive of the conditional group, if the branches of the group end with
     * different blocks. Then the indentation of the following lines may be
     * wrong. Returns <code>null</code> otherwise.
     */
    public CollectedStatement getUnbalancedIfDirective() {
        return unbalancedIfDirective;
    }

    public void setUnbalancedIfDirective(CollectedStatement ifDirective) {
        this.unbalancedIfDirective = ifDirective;
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

    /**
     * Returns <code>true</code> for a compiler directive at the start of a
     * statement: a slash followed by a letter.
     */
    private boolean isCompilerDirective(String line) {
        return StatementType.COMPILER_DIRECTIVE.pattern().matcher(line).find();
    }

    /**
     * Returns <code>true</code> for a directive of a conditional group, that
     * appears between the lines of a statement: <code>/IF</code>,
     * <code>/ELSEIF</code>, <code>/ELSE</code> or <code>/ENDIF</code>. Any
     * other line, that starts with a slash, continues the statement, such as
     * an SQL comment or a division like <code>/count;</code>, which the
     * compiler accepts as well.
     */
    private boolean isConditionalDirective(String line) {

        switch (DirectiveType.of(line)) {
        case IF:
        case ELSEIF:
        case ELSE:
        case ENDIF:
            return true;
        default:
            return false;
        }
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
     * <p>
     * In an embedded SQL statement a semicolon, a single quote or a comment
     * does not count within an SQL comment or a delimited identifier.
     * </p>
     */
    private void buildStatement(String line) throws RpgleFormatterException {

        String lineTrimmed;
        if (!leftTrimNextLine) {
            lineTrimmed = line;
        } else {
            lineTrimmed = StringUtils.trimL(line);
        }

        boolean isEmbeddedSql = isEmbeddedSql(lineTrimmed);

        for (int i = 0; i < lineTrimmed.length(); i++) {

            if (insideSqlBlockComment) {
                if (lineTrimmed.startsWith(SQL_BLOCK_COMMENT_END, i)) {
                    insideSqlBlockComment = false;
                    i++;
                }
                continue;
            }

            if (insideDelimitedIdentifier) {
                if (lineTrimmed.startsWith(DOUBLE_QUOTE, i)) {
                    if (lineTrimmed.startsWith(DOUBLE_QUOTE, i + 1)) {
                        // Two double quotes: an escaped quote
                        i++;
                    } else {
                        insideDelimitedIdentifier = false;
                    }
                }
                continue;
            }

            if (lineTrimmed.startsWith(SINGLE_QUOTE, i)) {
                if (lineTrimmed.startsWith(SINGLE_QUOTE, i + 1)) {
                    // Two single quotes: an escaped quote
                    i++;
                } else {
                    insideLiteral = !insideLiteral;
                }
            }

            if (!insideLiteral) {
                if (isEmbeddedSql) {
                    if (lineTrimmed.startsWith(SQL_COMMENT, i)) {
                        continueBeforeSqlComment(lineTrimmed, i);
                        return;
                    }
                    if (lineTrimmed.startsWith(SQL_BLOCK_COMMENT_START, i)) {
                        insideSqlBlockComment = true;
                        i++;
                        continue;
                    }
                    if (lineTrimmed.startsWith(DOUBLE_QUOTE, i)) {
                        insideDelimitedIdentifier = true;
                        continue;
                    }
                }
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
     * Appends the code in front of a comment, which ends the line, and collects
     * the comment. The statement goes on in the next line, also if the comment
     * contains a semicolon or a single quote. Like a whole comment line, the
     * comment is put in front of the formatted statement.
     */
    private void continueBeforeComment(String line, int comment) {

        String code = StringUtils.trimR(line.substring(0, comment));
        if (!code.isEmpty()) {
            appendLineOrComment(code);
        }
        collectComment(line.substring(comment));
    }

    /**
     * Returns <code>true</code>, if the statement, that the line belongs to, is
     * an embedded SQL statement.
     */
    private boolean isEmbeddedSql(String line) {
        return StatementType.EXEC_SQL.pattern().matcher(statement + line).find();
    }

    /**
     * Appends the code in front of an SQL comment, which ends the line. Unlike
     * a comment of RPG, the SQL comment stays in the lines of the statement and
     * a semicolon or a single quote within it does not count.
     */
    private void continueBeforeSqlComment(String line, int comment) {

        String code = StringUtils.trimR(line.substring(0, comment));
        if (!code.isEmpty()) {
            appendLineOrComment(code);
        }
    }

    /**
     * Returns <code>true</code>, if the character inside a literal is a plus or
     * hyphen, which continues the literal: the last character of the line
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

        hasContinuedLiteral = true;
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

    /**
     * Returns <code>true</code>, if the leading blanks of a line are part of a
     * literal that is continued with a hyphen on the previous line. Changing
     * the indent of such a line would change the value of the literal.
     */
    public boolean isLeadingBlanksSignificant(int index) {
        return leadingBlanksSignificant.get(index);
    }

    /**
     * Returns <code>true</code>, if the statement contains a literal, that is
     * continued with a plus or a hyphen on the next line.
     */
    public boolean hasContinuedLiteral() {
        return hasContinuedLiteral;
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
