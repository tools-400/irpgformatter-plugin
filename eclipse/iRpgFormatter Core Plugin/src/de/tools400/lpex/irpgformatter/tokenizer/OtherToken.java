/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.tokenizer;

public class OtherToken extends AbstractToken {

    public OtherToken(String value, String rawValue, int offset) {
        super(TokenType.OTHER, value, rawValue, offset);
    }
}
