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

    private final int indentLevel;
    private final List<CollectedStatement> parentsAtIf;
    private List<CollectedStatement> parentsAtEndOfFirstBranch;

    /**
     * Starts a conditional group at an <code>/IF</code> directive.
     *
     * @param indentLevel the indent level of the <code>/IF</code> directive
     * @param parents the blocks that are open at the <code>/IF</code>
     *        directive
     */
    ConditionalGroup(int indentLevel, Stack<CollectedStatement> parents) {
        this.indentLevel = indentLevel;
        this.parentsAtIf = new ArrayList<>(parents);
        this.parentsAtEndOfFirstBranch = null;
    }

    /**
     * Returns the indent level of the directives of the group.
     */
    int getIndentLevel() {
        return indentLevel;
    }

    /**
     * Ends the current branch at an <code>/ELSEIF</code> or <code>/ELSE</code>
     * directive and starts the next branch with the blocks that are open at the
     * <code>/IF</code> directive.
     *
     * @param parents the blocks that are open at the end of the current branch;
     *        replaced by the blocks that are open at the <code>/IF</code>
     *        directive
     */
    void startNextBranch(Stack<CollectedStatement> parents) {
        endBranch(parents);
        restore(parents, parentsAtIf);
    }

    /**
     * Ends the group at the <code>/ENDIF</code> directive and continues with
     * the blocks that are open at the end of the first branch.
     *
     * @param parents the blocks that are open at the end of the last branch;
     *        replaced by the blocks that are open at the end of the first
     *        branch
     */
    void end(Stack<CollectedStatement> parents) {
        endBranch(parents);
        restore(parents, parentsAtEndOfFirstBranch);
    }

    private void endBranch(Stack<CollectedStatement> parents) {
        if (parentsAtEndOfFirstBranch == null) {
            parentsAtEndOfFirstBranch = new ArrayList<>(parents);
        }
    }

    private static void restore(Stack<CollectedStatement> parents, List<CollectedStatement> blocks) {
        parents.clear();
        parents.addAll(blocks);
    }
}
