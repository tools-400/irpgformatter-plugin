/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.tokenizer;

public class LiteralToken extends AbstractToken {

    public LiteralToken(String value, String rawValue, int offset) {
        super(TokenType.LITERAL, value, rawValue, offset);
    }

    /**
     * Returns the number of digits of one byte of a hexadecimal literal, e.g.
     * <code>x'C1C2'</code>, or of one character of a UCS-2 literal, e.g.
     * <code>u'00410042'</code>.
     *
     * @param text - the text, that starts with the literal
     * @return 2 for a hexadecimal literal, 4 for a UCS-2 literal or 0, if the
     *         text does not start with one of them
     */
    public static int getDigitsPerCharacter(String text) {

        if (text.length() < 2 || text.charAt(1) != '\'') {
            return 0;
        }

        switch (Character.toUpperCase(text.charAt(0))) {
        case 'X':
            return 2;
        case 'U':
            return 4;
        default:
            return 0;
        }
    }
}
