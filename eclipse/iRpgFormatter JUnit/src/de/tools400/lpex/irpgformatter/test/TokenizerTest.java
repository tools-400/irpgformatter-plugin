/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import de.tools400.lpex.irpgformatter.formatter.RpgleFormatterException;
import de.tools400.lpex.irpgformatter.tokenizer.IToken;
import de.tools400.lpex.irpgformatter.tokenizer.TokenType;
import de.tools400.lpex.irpgformatter.tokenizer.Tokenizer;

public class TokenizerTest extends AbstractTestCase {

    private Tokenizer tokenizer;

    @Before
    public void setUp() {
        super.setUp();
        tokenizer = new Tokenizer();
    }

    /* Basic Tests */

    @Test
    public void test_name() throws RpgleFormatterException {

        IToken[] tokens = tokenizer.tokenize(" name1   name2 ");
        assertEquals(TokenType.NAME, tokens[0].getType());
        assertEquals("name1", tokens[0].getValue());
        assertEquals(TokenType.NAME, tokens[1].getType());
        assertEquals("name2", tokens[1].getValue());
    }

    @Test
    public void test_literal() throws RpgleFormatterException {

        IToken[] tokens = tokenizer.tokenize(" 'literal1'   'literal2' ");
        assertEquals(TokenType.LITERAL, tokens[0].getType());
        assertEquals("'literal1'", tokens[0].getValue());
        assertEquals(TokenType.LITERAL, tokens[1].getType());
        assertEquals("'literal2'", tokens[1].getValue());
    }

    @Test
    public void test_function_keyword() throws RpgleFormatterException {

        IToken[] tokens = tokenizer.tokenize(" options( *string ) ");
        assertEquals(TokenType.KEYWORD, tokens[0].getType());
        assertEquals("options", tokens[0].getValue());
        assertEquals(TokenType.SPECIAL_WORD, tokens[0].getChild(0).getType());
        assertEquals("*string", tokens[0].getChild(0).getValue());
    }

    @Test
    public void test_function_literal() throws RpgleFormatterException {

        IToken[] tokens = tokenizer.tokenize(" copyright( '2026, My Company' ) ");
        assertEquals(TokenType.KEYWORD, tokens[0].getType());
        assertEquals("copyright", tokens[0].getValue());
        assertEquals(TokenType.LITERAL, tokens[0].getChild(0).getType());
        assertEquals("'2026, My Company'", tokens[0].getChild(0).getValue());
    }

    @Test
    public void test_function_eol() throws RpgleFormatterException {

        IToken[] tokens = tokenizer.tokenize(" ; ");
        assertEquals(TokenType.EOL, tokens[0].getType());
        assertEquals(";", tokens[0].getValue());
    }

    @Test
    public void test_specialWord_followedBySemicolon() throws RpgleFormatterException {

        IToken[] tokens = tokenizer.tokenize("dcl-pi *n;");
        assertEquals(TokenType.DCL, tokens[0].getType());
        assertEquals(TokenType.SPECIAL_WORD, tokens[1].getType());
        assertEquals("*n", tokens[1].getValue());
        assertEquals(TokenType.EOL, tokens[2].getType());
    }

    @Test
    public void test_function_comment() throws RpgleFormatterException {

        IToken[] tokens = tokenizer.tokenize(" // Just a comment ");
        assertEquals(TokenType.COMMENT, tokens[0].getType());
        assertEquals("// Just a comment", tokens[0].getValue());
    }

    @Test
    public void test_function_mixed() throws RpgleFormatterException {

        IToken[] tokens = tokenizer.tokenize(" foo( '2026, My Company' : *STRING ) ");
        assertTrue("Expected exactly 1 token", tokens.length == 1);
        assertEquals(TokenType.FUNCTION, tokens[0].getType());
        assertEquals("foo", tokens[0].getValue());
        assertTrue("Expected exactly 2 child tokens", tokens[0].getNumChildren() == 2);
        assertEquals(TokenType.LITERAL, tokens[0].getChild(0).getType());
        assertEquals("'2026, My Company'", tokens[0].getChild(0).getValue());
        assertEquals(TokenType.SPECIAL_WORD, tokens[0].getChild(1).getType());
        assertEquals("*STRING", tokens[0].getChild(1).getValue());
    }

    /* Complex Tests */

    @Test
    public void test_dcl_s() throws RpgleFormatterException {

        IToken[] tokens = tokenizer.tokenize("dcl-s myParameter varchar(10); // field info");
        assertTrue("Expected exactly 5 tokens", tokens.length == 5);
        assertEquals(TokenType.DCL, tokens[0].getType());
        assertEquals(TokenType.NAME, tokens[1].getType());

        assertEquals(TokenType.DATA_TYPE, tokens[2].getType());
        assertTrue("Expected exactly 1 child token", tokens[2].getNumChildren() == 1);
        assertEquals(TokenType.OTHER, tokens[2].getChild(0).getType());
        assertEquals("10", tokens[2].getChild(0).getValue());

        assertEquals(TokenType.EOL, tokens[3].getType());
        assertEquals(TokenType.COMMENT, tokens[4].getType());
    }

    @Test
    public void test_dcl_subF() throws RpgleFormatterException {

        IToken[] tokens = tokenizer.tokenize("myParameter varchar(10); // parameter info");
        assertTrue("Expected exactly 4 tokens", tokens.length == 4);
        assertEquals(TokenType.NAME, tokens[0].getType());

        assertEquals(TokenType.DATA_TYPE, tokens[1].getType());
        assertTrue("Expected exactly 1 child token", tokens[1].getNumChildren() == 1);
        assertEquals(TokenType.OTHER, tokens[1].getChild(0).getType());
        assertEquals("10", tokens[1].getChild(0).getValue());

        assertEquals(TokenType.EOL, tokens[2].getType());
        assertEquals(TokenType.COMMENT, tokens[3].getType());
    }

    @Test
    public void test_dcl_pr() throws RpgleFormatterException {

        String line = "dcl-pr myProcedure pointer(*proc) extproc('MODULE_myProcedure'); // test comment";
        IToken[] tokens = tokenizer.tokenize(line);
        assertTrue("Expected exactly 6 tokens", tokens.length == 6);
        assertEquals(TokenType.DCL, tokens[0].getType());
        assertEquals(TokenType.NAME, tokens[1].getType());

        assertEquals(TokenType.DATA_TYPE, tokens[2].getType());
        assertEquals(TokenType.SPECIAL_WORD, tokens[2].getChild(0).getType());
        assertEquals("*proc", tokens[2].getChild(0).getValue());

        assertEquals(TokenType.KEYWORD, tokens[3].getType());
        assertEquals("'MODULE_myProcedure'", tokens[3].getChild(0).getValue());
        assertEquals(TokenType.LITERAL, tokens[3].getChild(0).getType());

        assertEquals(TokenType.EOL, tokens[4].getType());
        assertEquals(TokenType.COMMENT, tokens[5].getType());
        assertEquals("// test comment", tokens[5].getValue());

        int rawLength = 0;
        for (IToken iToken : tokens) {
            rawLength += iToken.getRawLength();
        }
        assertEquals(line.length(), rawLength);
    }
}
