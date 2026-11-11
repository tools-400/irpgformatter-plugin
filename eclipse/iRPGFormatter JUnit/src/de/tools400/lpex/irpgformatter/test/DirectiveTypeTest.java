/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.tools400.lpex.irpgformatter.parser.DirectiveType;

/**
 * Unit tests for {@link DirectiveType}.
 */
public class DirectiveTypeTest extends AbstractTestCase {

    @Test
    public void of_if() {
        assertEquals(DirectiveType.IF, DirectiveType.of("/if defined(X)"));
    }

    @Test
    public void of_elseif() {
        // must not be identified as /ELSE
        assertEquals(DirectiveType.ELSEIF, DirectiveType.of("/elseif defined(Y)"));
    }

    @Test
    public void of_else() {
        assertEquals(DirectiveType.ELSE, DirectiveType.of("/else"));
    }

    @Test
    public void of_endif() {
        assertEquals(DirectiveType.ENDIF, DirectiveType.of("/endif"));
    }

    @Test
    public void of_eof() {
        assertEquals(DirectiveType.EOF, DirectiveType.of("/eof"));
    }

    @Test
    public void of_uppercase_and_indented() {
        assertEquals(DirectiveType.EOF, DirectiveType.of("   /EOF"));
    }

    @Test
    public void of_endFree_isOther() {
        // must not be identified as /ENDIF
        assertEquals(DirectiveType.OTHER, DirectiveType.of("/end-free"));
    }

    @Test
    public void of_include_isOther() {
        // must not be identified as /IF
        assertEquals(DirectiveType.OTHER, DirectiveType.of("/include qcpysrc,protos"));
    }

    @Test
    public void of_copy_isOther() {
        assertEquals(DirectiveType.OTHER, DirectiveType.of("/copy qcpysrc,protos"));
    }

    @Test
    public void of_comment_isNone() {
        assertEquals(DirectiveType.NONE, DirectiveType.of("// /eof"));
    }

    @Test
    public void of_statement_isNone() {
        assertEquals(DirectiveType.NONE, DirectiveType.of("dcl-s eof ind;"));
    }

    @Test
    public void of_null_isNone() {
        assertEquals(DirectiveType.NONE, DirectiveType.of(null));
    }
}
