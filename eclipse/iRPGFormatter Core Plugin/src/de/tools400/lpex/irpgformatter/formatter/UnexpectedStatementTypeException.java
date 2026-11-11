/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.formatter;

import de.tools400.lpex.irpgformatter.Messages;
import de.tools400.lpex.irpgformatter.parser.StatementType;

public class UnexpectedStatementTypeException extends RpgleFormatterException {

    private static final long serialVersionUID = 1L;

    public UnexpectedStatementTypeException(StatementType statementType, int lineNumber, String statement) {
        super(Messages.bind(Messages.Error_Error_on_line_A_Unexpected_statement_of_type_B, new Object[] { lineNumber, statementType, statement }));
    }
}
