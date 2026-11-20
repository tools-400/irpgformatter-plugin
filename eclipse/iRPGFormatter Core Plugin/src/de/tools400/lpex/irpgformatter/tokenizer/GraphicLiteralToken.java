/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.tokenizer;

/**
 * Token of a graphic literal, e.g. <code>G'oK1K2i'</code>. The literal is never
 * split.
 */
public class GraphicLiteralToken extends AbstractToken {

    public GraphicLiteralToken(String value, String rawValue, int offset) {
        super(TokenType.GRAPHIC_LITERAL, value, rawValue, offset);
    }
}
