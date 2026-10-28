/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.statement;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import de.tools400.lpex.irpgformatter.formatter.RpgleFormatterException;

/**
 * Block structure of a conditional group (<code>/IF</code> ...
 * <code>/ELSEIF</code> ... <code>/ELSE</code> ... <code>/ENDIF</code>).
 * <p>
 * The compiler reads only one branch of the group, therefore each branch starts
 * with the blocks that are open at the <code>/IF</code> directive. After the
 * <code>/ENDIF</code> directive the formatter continues with the blocks that
 * are open at the end of the first branch.
 * </p>
 */
class ConditionalGroup {

    private final CollectedStatement ifDirective;
    private final List<CollectedStatement> parentsAtIf;
    private List<CollectedStatement> parentsAtEndOfFirstBranch;
    private boolean haveSameBlocks;
    private boolean hasElse;

    /**
     * Starts a conditional group at an <code>/IF</code> directive.
     *
     * @param ifDirective the <code>/IF</code> directive
     * @param parents the blocks that are open at the <code>/IF</code>
     *        directive
     */
    ConditionalGroup(CollectedStatement ifDirective, Stack<CollectedStatement> parents) {
        this.ifDirective = ifDirective;
        this.parentsAtIf = new ArrayList<>(parents);
        this.parentsAtEndOfFirstBranch = null;
        this.haveSameBlocks = true;
        this.hasElse = false;
    }

    /**
     * Returns the <code>/IF</code> directive of the group.
     */
    CollectedStatement getIfDirective() {
        return ifDirective;
    }

    /**
     * Returns the indent level of the directives of the group.
     */
    int getIndentLevel() {
        return ifDirective.getIndentLevel();
    }

    /**
     * Ends the current branch at an <code>/ELSEIF</code> or <code>/ELSE</code>
     * directive and starts the next branch with the blocks that are open at the
     * <code>/IF</code> directive.
     *
     * @param parents the blocks that are open at the end of the current branch;
     *        replaced by the blocks that are open at the <code>/IF</code>
     *        directive
     * @param isElse <code>true</code> for an <code>/ELSE</code> directive
     */
    void startNextBranch(Stack<CollectedStatement> parents, boolean isElse) throws RpgleFormatterException {
        endBranch(parents);
        restore(parents, parentsAtIf);
        if (isElse) {
            hasElse = true;
        }
    }

    /**
     * Ends the group at the <code>/ENDIF</code> directive and continues with
     * the blocks that are open at the end of the first branch.
     *
     * @param parents the blocks that are open at the end of the last branch;
     *        replaced by the blocks that are open at the end of the first
     *        branch
     * @return <code>true</code>, if all branches end with the same blocks
     */
    boolean end(Stack<CollectedStatement> parents) throws RpgleFormatterException {
        endBranch(parents);
        if (!hasElse) {
            // Without /ELSE the compiler may skip all branches.
            endBranch(parentsAtIf);
        }
        restore(parents, parentsAtEndOfFirstBranch);

        return haveSameBlocks;
    }

    private void endBranch(List<CollectedStatement> parents) throws RpgleFormatterException {
        if (parentsAtEndOfFirstBranch == null) {
            parentsAtEndOfFirstBranch = new ArrayList<>(parents);
        } else if (!haveSameBlocks(parentsAtEndOfFirstBranch, parents)) {
            haveSameBlocks = false;
        }
    }

    /**
     * Returns <code>true</code>, if both lists contain blocks of the same types,
     * such as the alternative headers of a prototype in the branches of a
     * group.
     */
    private static boolean haveSameBlocks(List<CollectedStatement> blocks, List<CollectedStatement> otherBlocks)
        throws RpgleFormatterException {

        if (blocks.size() != otherBlocks.size()) {
            return false;
        }

        for (int i = 0; i < blocks.size(); i++) {
            CollectedStatement block = blocks.get(i);
            CollectedStatement otherBlock = otherBlocks.get(i);
            if (block == otherBlock) {
                continue;
            }
            if (block == null || otherBlock == null || block.getType() != otherBlock.getType()) {
                return false;
            }
        }

        return true;
    }

    private static void restore(Stack<CollectedStatement> parents, List<CollectedStatement> blocks) {
        parents.clear();
        parents.addAll(blocks);
    }
}
