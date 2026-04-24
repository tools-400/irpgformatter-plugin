/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.tokenizer;

import java.util.LinkedList;

public class AbstractToken implements IToken {

    private TokenType type;
    private String rawValue;
    private int rawLength;
    private String value;
    private LinkedList<IToken> children;
    private int offset;

    private IToken parent;

    // public AbstractToken(TokenType type, String value) {
    // this(type, value, value);
    // }

    public AbstractToken(TokenType type, String value, String rawValue, int offset) {
        this.type = type;
        this.rawValue = rawValue;
        this.rawLength = rawValue.length();
        this.value = value.trim();
        this.children = new LinkedList<>();
        this.offset = offset;
    }

    @Override
    public IToken getParent() {
        return parent;
    }

    @Override
    public void setParent(IToken parent) {
        this.parent = parent;
    }

    @Override
    public TokenType getType() {
        return type;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public int getOffset() {
        return offset;
    }

    @Override
    public String getRawValue() {
        return rawValue;
    }

    @Override
    public int getRawLength() {
        return rawLength;
    }

    public boolean haveChildren() {
        return children.size() > 0;
    }

    public int getNumChildren() {
        return children.size();
    }

    public IToken[] getChildren() {
        return children.toArray(new IToken[0]);
    }

    public IToken getChild(int index) {
        return children.get(index);
    }

    public void addChild(IToken child) {
        child.setParent(this);
        children.add(child);
    }

    public boolean hasChildren() {
        return false;
    }

    @Override
    public String toString() {
        return type.name() + ": " + value + " (rawLength: " + rawLength + ", length: " + value.length() + ")";
    }
}
