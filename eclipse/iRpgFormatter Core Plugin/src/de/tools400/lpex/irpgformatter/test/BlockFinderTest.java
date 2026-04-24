/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.tools400.lpex.irpgformatter.parser.BlockFinder;
import de.tools400.lpex.irpgformatter.parser.BlockFinder.BlockRange;
import de.tools400.lpex.irpgformatter.parser.BlockFinder.BlockType;

/**
 * Unit tests for {@link BlockFinder}.
 */
public class BlockFinderTest extends AbstractTestCase {

    // --- findBlockStart tests ---

    @Test
    public void findBlockStart_fromBlockStart() {
        String[] lines = { "dcl-ds myDs;", "  field1 char(10);", "end-ds;" };
        assertEquals(0, BlockFinder.findBlockStart(lines, 0));
    }

    @Test
    public void findBlockStart_fromSubfield() {
        String[] lines = { "dcl-ds myDs;", "  field1 char(10);", "end-ds;" };
        assertEquals(0, BlockFinder.findBlockStart(lines, 1));
    }

    @Test
    public void findBlockStart_fromBlockEnd() {
        String[] lines = { "dcl-ds myDs;", "  field1 char(10);", "end-ds;" };
        assertEquals(0, BlockFinder.findBlockStart(lines, 2));
    }

    @Test
    public void findBlockStart_notInBlock() {
        String[] lines = { "dcl-s myVar char(10);", "dcl-s otherVar int;" };
        assertEquals(-1, BlockFinder.findBlockStart(lines, 0));
    }

    @Test
    public void findBlockStart_nestedBlocks() {
        String[] lines = { "dcl-ds outer;", "  dcl-ds inner;", "    field1 char(10);", "  end-ds;", "end-ds;" };
        // From inner subfield, should find inner block start
        assertEquals(1, BlockFinder.findBlockStart(lines, 2));
    }

    // --- findBlockEnd tests ---

    @Test
    public void findBlockEnd_fromBlockStart() {
        String[] lines = { "dcl-ds myDs;", "  field1 char(10);", "end-ds;" };
        assertEquals(2, BlockFinder.findBlockEnd(lines, 0));
    }

    @Test
    public void findBlockEnd_fromSubfield() {
        String[] lines = { "dcl-ds myDs;", "  field1 char(10);", "end-ds;" };
        assertEquals(2, BlockFinder.findBlockEnd(lines, 1));
    }

    @Test
    public void findBlockEnd_fromBlockEnd() {
        String[] lines = { "dcl-ds myDs;", "  field1 char(10);", "end-ds;" };
        assertEquals(2, BlockFinder.findBlockEnd(lines, 2));
    }

    @Test
    public void findBlockEnd_notInBlock() {
        String[] lines = { "dcl-s myVar char(10);", "dcl-s otherVar int;" };
        assertEquals(-1, BlockFinder.findBlockEnd(lines, 0));
    }

    // --- findEnclosingBlock tests ---

    @Test
    public void findEnclosingBlock_dclDs() {
        String[] lines = { "dcl-ds myDs;", "  field1 char(10);", "end-ds;" };
        BlockRange block = BlockFinder.findEnclosingBlock(lines, 1);
        assertNotNull(block);
        assertEquals(0, block.getStartLine());
        assertEquals(2, block.getEndLine());
        assertEquals(BlockType.DCL_DS, block.getBlockType());
    }

    @Test
    public void findEnclosingBlock_dclPr() {
        String[] lines = { "dcl-pr myProc;", "  parm1 char(10);", "end-pr;" };
        BlockRange block = BlockFinder.findEnclosingBlock(lines, 1);
        assertNotNull(block);
        assertEquals(0, block.getStartLine());
        assertEquals(2, block.getEndLine());
        assertEquals(BlockType.DCL_PR, block.getBlockType());
    }

    @Test
    public void findEnclosingBlock_dclPi() {
        String[] lines = { "dcl-pi myProc;", "  parm1 int;", "end-pi;" };
        BlockRange block = BlockFinder.findEnclosingBlock(lines, 1);
        assertNotNull(block);
        assertEquals(0, block.getStartLine());
        assertEquals(2, block.getEndLine());
        assertEquals(BlockType.DCL_PI, block.getBlockType());
    }

    @Test
    public void findEnclosingBlock_notInBlock() {
        String[] lines = { "dcl-s myVar char(10);" };
        BlockRange block = BlockFinder.findEnclosingBlock(lines, 0);
        assertNull(block);
    }

    // --- isInsideBlock tests ---

    @Test
    public void isInsideBlock_subfield() {
        String[] lines = { "dcl-ds myDs;", "  field1 char(10);", "end-ds;" };
        assertTrue(BlockFinder.isInsideBlock(lines, 1));
    }

    @Test
    public void isInsideBlock_blockStart() {
        String[] lines = { "dcl-ds myDs;", "  field1 char(10);", "end-ds;" };
        // Block start line itself is NOT inside the block
        assertFalse(BlockFinder.isInsideBlock(lines, 0));
    }

    @Test
    public void isInsideBlock_blockEnd() {
        String[] lines = { "dcl-ds myDs;", "  field1 char(10);", "end-ds;" };
        // Block end line itself is NOT inside the block
        assertFalse(BlockFinder.isInsideBlock(lines, 2));
    }

    @Test
    public void isInsideBlock_outsideBlock() {
        String[] lines = { "dcl-s myVar char(10);", "dcl-ds myDs;", "  field1 char(10);", "end-ds;" };
        assertFalse(BlockFinder.isInsideBlock(lines, 0));
    }

    // --- Selection expansion tests (simulated without LpexView) ---

    @Test
    public void expandSelection_selectionStartsInsideBlock() {
        // Simulating: user selects from subfield to end-ds
        // Selection should expand to include dcl-ds
        String[] lines = { "dcl-ds myDs;", "  field1 char(10);", "  field2 int;", "end-ds;" };

        // Selection starts at field1 (line 1), ends at end-ds (line 3)
        // Should expand to include dcl-ds (line 0)
        int startIdx = 1;
        int endIdx = 3;

        int expandedStart = startIdx;
        int expandedEnd = endIdx;

        if (BlockFinder.isInsideBlock(lines, startIdx)) {
            BlockRange block = BlockFinder.findEnclosingBlock(lines, startIdx);
            if (block != null) {
                expandedStart = block.getStartLine();
                expandedEnd = Math.max(expandedEnd, block.getEndLine());
            }
        }

        assertEquals(0, expandedStart);
        assertEquals(3, expandedEnd);
    }

    @Test
    public void expandSelection_selectionEndsInsideBlock() {
        // Simulating: user selects from dcl-ds to subfield
        // Selection should expand to include end-ds
        String[] lines = { "dcl-ds myDs;", "  field1 char(10);", "  field2 int;", "end-ds;" };

        // Selection starts at dcl-ds (line 0), ends at field1 (line 1)
        // Should expand to include end-ds (line 3)
        int startIdx = 0;
        int endIdx = 1;

        int expandedStart = startIdx;
        int expandedEnd = endIdx;

        if (BlockFinder.isInsideBlock(lines, endIdx)) {
            BlockRange block = BlockFinder.findEnclosingBlock(lines, endIdx);
            if (block != null) {
                expandedStart = Math.min(expandedStart, block.getStartLine());
                expandedEnd = block.getEndLine();
            }
        }

        assertEquals(0, expandedStart);
        assertEquals(3, expandedEnd);
    }

    @Test
    public void expandSelection_selectionSpansMultipleBlocks() {
        String[] lines = { "dcl-ds ds1;", "  field1 char(10);", "end-ds;", "dcl-ds ds2;", "  field2 int;", "end-ds;" };

        // Selection from field1 (line 1) to field2 (line 4)
        // Should expand to include both complete blocks (lines 0-5)
        int startIdx = 1;
        int endIdx = 4;

        int expandedStart = startIdx;
        int expandedEnd = endIdx;

        // Check start
        if (BlockFinder.isInsideBlock(lines, startIdx)) {
            BlockRange block = BlockFinder.findEnclosingBlock(lines, startIdx);
            if (block != null) {
                expandedStart = block.getStartLine();
            }
        }

        // Check end
        if (BlockFinder.isInsideBlock(lines, endIdx)) {
            BlockRange block = BlockFinder.findEnclosingBlock(lines, endIdx);
            if (block != null) {
                expandedEnd = block.getEndLine();
            }
        }

        assertEquals(0, expandedStart);
        assertEquals(5, expandedEnd);
    }

    @Test
    public void expandSelection_noExpansionNeeded() {
        String[] lines = { "dcl-s var1 char(10);", "dcl-s var2 int;", "dcl-s var3 packed(5:2);" };

        // Selection of standalone statements - no expansion needed
        int startIdx = 0;
        int endIdx = 2;

        assertFalse(BlockFinder.isInsideBlock(lines, startIdx));
        assertFalse(BlockFinder.isInsideBlock(lines, endIdx));
    }

    // --- DCL-PR subfield range tests ---

    private String[] createDclPrTestSource() {
        // @formatter:off
        return new String[] {
            "dcl-proc myProc pointer;",
            "  dcl-s myPointer pointer;",
            "  dcl-pr myPrototype extproc('MODULE_myPrototype');",
            "    mySubField1 varchar(10);",
            "    mySubField2 varchar(10);",
            "  end-pr;",
            "  dcl-c MY_CONSTANT 1;",
            "  dcl-ds myStructure likeds(refStruct_t);",
            "end-proc;"
        };
        // @formatter:on
    }

    @Test
    public void findEnclosingBlock_start_on_Dcl_Ds() {
        String[] lines = createDclPrTestSource();
        BlockRange blockRange = BlockFinder.findEnclosingBlock(lines, 7);
        assertEquals(new BlockRange(7, 7, BlockType.DCL_DS), blockRange);
    }

    @Test
    public void findEnclosingBlock_start_on_Dcl_s() {
        String[] lines = createDclPrTestSource();
        BlockRange blockRange = BlockFinder.findEnclosingBlock(lines, 1);
        assertEquals(new BlockRange(0, 8, BlockType.DCL_PROC), blockRange);
    }

    @Test
    public void findBlockStart_start_on_Dcl_Pr() {
        String[] lines = createDclPrTestSource();
        assertEquals(2, BlockFinder.findBlockStart(lines, 2));
    }

    @Test
    public void findBlockEnd_start_on_Sub_F() {
        String[] lines = createDclPrTestSource();
        assertEquals(5, BlockFinder.findBlockEnd(lines, 3));
    }

    @Test
    public void findEnclosingBlock_start_on_Sub_F() {
        String[] lines = createDclPrTestSource();
        BlockRange blockRange = BlockFinder.findEnclosingBlock(lines, 3);
        assertEquals(new BlockRange(2, 5, BlockType.DCL_PR), blockRange);
    }

    @Test
    public void isInsideBlock_start_on_Sub_F() {
        String[] lines = createDclPrTestSource();
        assertTrue(BlockFinder.isInsideBlock(lines, 4));
    }
}
