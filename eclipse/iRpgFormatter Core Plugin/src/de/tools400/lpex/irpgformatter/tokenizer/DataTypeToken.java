/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.tokenizer;

public class DataTypeToken extends AbstractToken {

    public DataTypeToken(String value, String rawValue, int offset) {
        super(TokenType.DATA_TYPE, value, rawValue, offset);
    }

    public boolean hasChildren() {
        return true;
    }
}
