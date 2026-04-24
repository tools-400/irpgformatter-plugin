/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.input;

import org.eclipse.rse.core.model.IHost;

import com.ibm.as400.access.AS400;
import com.ibm.etools.iseries.services.qsys.api.IQSYSSourceMember;
import com.ibm.etools.iseries.subsystems.qsys.api.IBMiConnection;
import com.ibm.etools.iseries.subsystems.qsys.objects.IRemoteObjectContextProvider;
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
        return new RpgleLpexInput((LpexView)view);
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
     * Creates an {@link IRpgleInput} from a remote IBM i source member.
     *
     * @param sourceMember the remote source member
     * @return an IRpgleInput for the remote member
     * @throws RpgleFormatterException if the connection cannot be established
     */
    private static IRpgleInput createFromQSYSRemoteMember(IQSYSSourceMember sourceMember) throws RpgleFormatterException {
        try {
            IHost host = ((IRemoteObjectContextProvider)sourceMember).getRemoteObjectContext().getObjectSubsystem().getHost();
            IBMiConnection connection = IBMiConnection.getConnection(host);
            return createFromJT400RemoteMember(connection, sourceMember.getLibrary(), sourceMember.getFile(), sourceMember.getName());
        } catch (Exception e) {
            throw new RpgleFormatterException(Messages.bind(Messages.Error_Failed_reading_file_A,
                sourceMember.getLibrary() + "/" + sourceMember.getFile() + "(" + sourceMember.getName() + ")"), e);
        }
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
