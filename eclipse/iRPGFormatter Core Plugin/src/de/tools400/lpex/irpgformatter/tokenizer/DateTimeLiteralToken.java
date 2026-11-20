/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.tokenizer;

/**
 * Token of a date, time or timestamp literal, e.g. <code>d'2026-11-20'</code>,
 * <code>t'12:00:00'</code> or <code>z'2026-11-20-12.00.00.000000'</code>. The
 * literal is never split.
 */
public class DateTimeLiteralToken extends AbstractToken {

    public DateTimeLiteralToken(String value, String rawValue, int offset) {
        super(TokenType.DATE_TIME_LITERAL, value, rawValue, offset);
    }
}
