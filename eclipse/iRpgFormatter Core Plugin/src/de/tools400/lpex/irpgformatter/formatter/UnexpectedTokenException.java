/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.formatter;

import de.tools400.lpex.irpgformatter.tokenizer.IToken;

public class UnexpectedTokenException extends RpgleFormatterException {

    private static final long serialVersionUID = 1L;

    public UnexpectedTokenException(IToken token) {
        super(String.format("Unexpected token of type: %s", token != null ? token.getType().name() : "[null]"));
    }
}
