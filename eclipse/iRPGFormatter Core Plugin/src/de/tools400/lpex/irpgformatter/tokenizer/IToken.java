/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.tokenizer;

public interface IToken {

    public IToken getParent();

    public void setParent(IToken parent);

    public TokenType getType();

    public String getValue();

    public int getOffset();

    public String getRawValue();

    public int getRawLength();

    public int getNumChildren();

    public boolean haveChildren();

    public IToken[] getChildren();

    public IToken getChild(int index);

    public void addChild(IToken child);
}
