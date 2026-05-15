/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.rules.statements;

import de.tools400.lpex.irpgformatter.formatter.RpgleFormatterException;
import de.tools400.lpex.irpgformatter.parser.StatementType;
import de.tools400.lpex.irpgformatter.rules.IStatementListRule;
import de.tools400.lpex.irpgformatter.statement.CollectedStatement;

/**
 * Rule that removes blank lines and empty comment lines ({@code //}) that
 * appear immediately before a {@code dcl-pi} statement.
 * <p>
 * This rule must run after {@link RemoveEmptyCommentLinesRule} so that empty
 * comment lines suppressed by that rule are also considered.
 * </p>
 */
public class RemoveEmptyLinesBeforeDclPiRule implements IStatementListRule {

    @Override
    public boolean[] apply(CollectedStatement[] statements) throws RpgleFormatterException {

        boolean[] result = new boolean[statements.length];
        for (int i = 0; i < statements.length; i++) {
            StatementType type = statements[i].getType();
            if (type == StatementType.COMPILE_TIME_ARRAY) {
                break;
            }

            if (type == StatementType.DCL_PI) {
                int j = i - 1;
                while (j >= 0 && isEffectivelyBlank(statements[j])) {
                    result[j] = true;
                    j--;
                }
            }
        }

        return result;
    }

    private boolean isEffectivelyBlank(CollectedStatement stmt) throws RpgleFormatterException {
        StatementType type = stmt.getType();
        if (type == StatementType.BLANK) {
            return true;
        }
        if (type == StatementType.COMMENT) {
            return stmt.getStatement().trim().equals("//");
        }
        return false;
    }
}
