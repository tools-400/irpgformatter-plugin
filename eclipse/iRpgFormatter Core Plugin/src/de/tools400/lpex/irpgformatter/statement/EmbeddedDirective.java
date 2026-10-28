/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.statement;

/**
 * A compiler directive between the lines of a statement, such as
 * <code>/IF</code> within a <code>DCL-S</code> statement.
 */
public class EmbeddedDirective {

    private final String line;
    private final int offset;

    /**
     * Creates an embedded directive.
     *
     * @param line - the source line of the directive
     * @param offset - the position of the directive in the text of the
     *        statement
     */
    public EmbeddedDirective(String line, int offset) {
        this.line = line;
        this.offset = offset;
    }

    /**
     * Returns the source line of the directive.
     */
    public String getLine() {
        return line;
    }

    /**
     * Returns the position of the directive in the text of the statement, see
     * {@link CollectedStatement#getStatement()}.
     */
    public int getOffset() {
        return offset;
    }
}
