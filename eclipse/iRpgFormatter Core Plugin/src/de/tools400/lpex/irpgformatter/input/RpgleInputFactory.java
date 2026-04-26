/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.input;

import org.eclipse.core.resources.IFile;

import com.ibm.as400.access.AS400;
import com.ibm.etools.iseries.subsystems.qsys.api.IBMiConnection;
import com.ibm.lpex.core.LpexView;

import de.tools400.lpex.irpgformatter.Messages;
import de.tools400.lpex.irpgformatter.formatter.RpgleFormatterException;
import de.tools400.lpex.irpgformatter.parser.BlockFinder;
import de.tools400.lpex.irpgformatter.utils.LpexViewUtils;

/**
 * Factory for creating {@link IRpgleInput} instances from various input types.
 */
public class RpgleInputFactory {

    /**
     * Creates an {@link IRpgleInput} from an LPEXView for all lines of the
     * view.
     *
     * @param view the LPEX view
     * @return an appropriate IRpgleInput implementation
     * @throws RpgleFormatterException if the input type is not supported
     */
    public static IRpgleInput createFromAll(LpexView view) throws RpgleFormatterException {
        return new RpgleLpexInput(view);
    }

    /**
     * Creates an {@link IRpgleInput} from an LPEX view for the selected range.
     * If no selection exists, returns an input for the entire view.
     * <p>
     * If the selection starts or ends inside a block (DCL-DS, DCL-PR, DCL-PI),
     * the selection is automatically expanded to include the complete block(s).
     * </p>
     *
     * @param view the LPEX view
     * @return an appropriate IRpgleInput implementation
     */
    public static IRpgleInput createFromSelection(LpexView view) {
        if (LpexViewUtils.hasBlockSelection(view)) {
            int[] range = LpexViewUtils.getBlockSelectionRange(view);
            // Expand selection to include complete blocks if selection is
            // inside a block
            int[] expandedRange = BlockFinder.expandSelectionToCompleteBlocks(view, range[0], range[1]);
            return new RpgleLpexInput(view, expandedRange[0], expandedRange[1]);
        }
        return new RpgleLpexInput(view);
    }

    /**
     * Creates an {@link IRpgleInput} from a local workspace stream file.
     *
     * @param file the Eclipse workspace file
     * @return an IRpgleInput for the stream file
     */
    public static IRpgleInput createFromStreamFile(IFile file) {
        return new RpgleStreamFileInput(file);
    }

    /**
     * Creates an {@link IRpgleInput} from a remote IFS stream file.
     *
     * @param system the AS400 connection
     * @param path the absolute IFS path
     * @return an IRpgleInput for the remote stream file
     */
    public static IRpgleInput createFromRemoteStreamFile(AS400 system, String path) {
        return new RpgleRemoteStreamFileInput(system, path);
    }

    /**
     * Creates an {@link IRpgleInput} from a remote IBM i source member.
     *
     * @param connectionName - the remote connection name
     * @param library - the remote library
     * @param file - the remote source file
     * @param member - the remote source member
     * @return an IRpgleInput for the remote member
     * @throws RpgleFormatterException if the connection cannot be established
     */
    public static IRpgleInput createFromJT400RemoteMember(IBMiConnection connection, String library, String file, String member)
        throws RpgleFormatterException {
        try {
            AS400 system = connection.getAS400ToolboxObject();
            return new RpgleJt400MemberInput(system, library, file, member);
        } catch (Exception e) {
            throw new RpgleFormatterException(Messages.bind(Messages.Error_Failed_reading_file_A, library + "/" + file + "(" + member + ")"), e);
        }
    }
}
