/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import com.ibm.lpex.core.LpexView;

import de.tools400.lpex.irpgformatter.utils.LpexViewUtils;

/**
 * Utility class for finding declaration block boundaries (DCL-DS/END-DS,
 * DCL-PR/END-PR, DCL-PI/END-PI).
 */
public final class BlockFinder {

    private BlockFinder() {
        // Utility class
    }

    /**
     * Represents a block type with its start and end patterns.
     */
    public enum BlockType {
        DCL_DS (StatementType.DCL_DS, StatementType.END_DS),
        DCL_PROC (StatementType.DCL_PROC, StatementType.END_PROC),
        DCL_PR (StatementType.DCL_PR, StatementType.END_PR),
        DCL_PI (StatementType.DCL_PI, StatementType.END_PI),
        DCL_ENUM (StatementType.DCL_ENUM, StatementType.END_ENUM);

        private final StatementType startPattern;
        private final StatementType endPattern;

        BlockType(StatementType startPattern, StatementType endPattern) {
            this.startPattern = startPattern;
            this.endPattern = endPattern;
        }

        public Pattern getStartPattern() {
            return startPattern.pattern();
        }

        public Pattern getEndPattern() {
            return endPattern.pattern();
        }
    }

    /**
     * Represents a block range with start and end line indices.
     */
    public static class BlockRange {
        private final int startLine;
        private final int endLine;
        private final BlockType blockType;

        public BlockRange(int startLine, int endLine, BlockType blockType) {
            this.startLine = startLine;
            this.endLine = endLine;
            this.blockType = blockType;
        }

        public int getStartLine() {
            return startLine;
        }

        public int getEndLine() {
            return endLine;
        }

        public BlockType getBlockType() {
            return blockType;
        }

        @Override
        public int hashCode() {
            return Objects.hash(blockType, endLine, startLine);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null) return false;
            if (getClass() != obj.getClass()) return false;
            BlockRange other = (BlockRange)obj;
            return blockType == other.blockType && endLine == other.endLine && startLine == other.startLine;
        }

        @Override
        public String toString() {
            return blockType.name() + ": " + startLine + " -> " + endLine;
        }
    }

    /**
     * Finds the start of a declaration block by searching backwards from the
     * given line. 'fromLine' must be inside a block.
     *
     * @param lines the source lines (0-based array)
     * @param fromLine the line index to start searching from (0-based)
     * @return the line index of the block start, or -1 if not found
     */
    public static int findBlockStart(String[] lines, int fromLine) {

        int nestingLevel;
        if (isBlockStart(lines[fromLine])) {
            return fromLine;
        } else if (isBlockEnd(lines[fromLine])) {
            nestingLevel = 0;
        } else {
            // Assume we are in a block
            nestingLevel = 1;
        }

        for (int i = fromLine; i >= 0; i--) {
            String line = lines[i];

            // Check for end statements (increase nesting when going backwards)
            if (isBlockEnd(line)) {
                nestingLevel++;
            }
            // Check for start statements
            else if (isBlockStart(line)) {
                nestingLevel--;
                if (nestingLevel == 0) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * Finds the end of a declaration block by searching forwards from the given
     * line. 'fromLine' must be inside a block.
     *
     * @param lines the source lines (0-based array)
     * @param fromLine the line index to start searching from (0-based)
     * @return the line index of the block end, or -1 if not found
     */
    public static int findBlockEnd(String[] lines, int fromLine) {

        int nestingLevel;
        if (isBlockEnd(lines[fromLine])) {
            return fromLine;
        } else if (isBlockStart(lines[fromLine])) {
            nestingLevel = 0;
        } else {
            // Assume we are in a block
            nestingLevel = 1;
        }

        for (int i = fromLine; i < lines.length; i++) {
            String line = lines[i];

            // Check for start statements (increase nesting when going forwards)
            if (isBlockStart(line)) {
                nestingLevel++;
                if (StatementIdentifier.isImplicitlyClosedDclBlock(line)) {
                    nestingLevel--;
                    if (nestingLevel == 0) {
                        return i;
                    }
                }
            }
            // Check for end statements
            else if (isBlockEnd(line)) {
                nestingLevel--;
                if (nestingLevel == 0) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * Finds the enclosing block for a given line that appears to be inside a
     * block (e.g., a subfield definition).
     *
     * @param lines the source lines (0-based array)
     * @param lineIndex the line index to find the enclosing block for (0-based)
     * @return the BlockRange, or null if not inside a block
     */
    public static BlockRange findEnclosingBlock(String[] lines, int lineIndex) {

        int startLine = findBlockStart(lines, lineIndex);
        if (startLine < 0) {
            return null;
        }

        int endLine = findBlockEnd(lines, startLine);
        if (endLine < 0) {
            return null;
        }

        BlockType blockType = getBlockType(lines[startLine]);

        return new BlockRange(startLine, endLine, blockType);
    }

    /**
     * Checks if a line is a block start statement (DCL-DS, DCL-PR, DCL-PI).
     */
    private static boolean isBlockStart(String line) {

        if (line == null) {
            return false;
        }

        for (BlockType type : BlockType.values()) {
            if (type.getStartPattern().matcher(line).find()) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks if a line is a block end statement (END-DS, END-PR, END-PI).
     */
    private static boolean isBlockEnd(String line) {
        if (line == null) {
            return false;
        }
        for (BlockType type : BlockType.values()) {
            if (type.getEndPattern().matcher(line).find()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the block type for a block start line.
     */
    private static BlockType getBlockType(String line) {

        if (line == null) {
            return null;
        }
        for (BlockType type : BlockType.values()) {
            if (type.getStartPattern().matcher(line).find()) {
                return type;
            }
        }

        return null;
    }

    /**
     * Checks if a line is inside a block (between start and end, exclusive).
     *
     * @param lines the source lines (0-based array)
     * @param lineIndex the line index to check (0-based)
     * @return true if the line is inside a block
     */
    public static boolean isInsideBlock(String[] lines, int lineIndex) {
        BlockRange block = findEnclosingBlock(lines, lineIndex);
        if (block == null) {
            return false;
        }
        // Check if we're strictly between start and end
        return lineIndex > block.getStartLine() && lineIndex < block.getEndLine();
    }

    /**
     * Expands a selection to include complete blocks if the selection is inside
     * a block. Parses the source from the beginning to find all blocks.
     * <p>
     * If the selection starts or ends inside a block, the selection is expanded
     * to include the entire block. If the selection is not inside any block,
     * the original selection is returned unchanged.
     * </p>
     *
     * @param view the LPEX view containing the source
     * @param selectionStart the first selected line (1-based)
     * @param selectionEnd the last selected line (1-based)
     * @return an int array [startLine, endLine] with 1-based line numbers
     */
    public static int[] expandSelectionToCompleteBlocks(LpexView view, int selectionStart, int selectionEnd) {

        // Read all source lines from the view
        String[] lines = readSourceLines(view);

        // Convert to 0-based indices
        int startIdx = selectionStart - 1;
        int endIdx = selectionEnd - 1;

        // Parse from beginning to find blocks
        int expandedStart = startIdx;
        int expandedEnd = endIdx;

        // Check if start line is inside a block
        if (isInsideBlock(lines, startIdx)) {
            BlockRange block = findEnclosingBlock(lines, startIdx);
            if (block != null) {
                expandedStart = block.getStartLine();
                expandedEnd = Math.max(expandedEnd, block.getEndLine());
            }
        }

        // Check if end line is inside a block
        if (isInsideBlock(lines, endIdx)) {
            BlockRange block = findEnclosingBlock(lines, endIdx);
            if (block != null) {
                expandedStart = Math.min(expandedStart, block.getStartLine());
                expandedEnd = block.getEndLine();
            }
        }

        // Check if selection spans partial blocks (starts before block, ends
        // inside)
        for (int i = startIdx; i <= endIdx; i++) {
            if (isBlockStart(lines[i])) {
                int blockEnd = findBlockEnd(lines, i);
                if (blockEnd > endIdx) {
                    expandedEnd = blockEnd;
                }
            }
        }

        // Convert back to 1-based line numbers
        return new int[] { expandedStart + 1, expandedEnd + 1 };
    }

    /**
     * Reads all source lines from an LPEX view.
     *
     * @param view the LPEX view
     * @return array of source lines (0-based)
     */
    private static String[] readSourceLines(LpexView view) {
        List<String> lines = new ArrayList<>();
        int totalLines = LpexViewUtils.getNumLines(view);
        for (int i = 1; i <= totalLines; i++) {
            lines.add(LpexViewUtils.getElementText(view, i));
        }
        return lines.toArray(new String[0]);
    }
}
