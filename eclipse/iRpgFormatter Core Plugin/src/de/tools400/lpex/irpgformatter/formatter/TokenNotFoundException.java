/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.formatter;

public class TokenNotFoundException extends RpgleFormatterException {

    private static final long serialVersionUID = 1L;

    public TokenNotFoundException(String tokenId) {
        super(String.format("Token of type %s not found.", tokenId));
    }
}
