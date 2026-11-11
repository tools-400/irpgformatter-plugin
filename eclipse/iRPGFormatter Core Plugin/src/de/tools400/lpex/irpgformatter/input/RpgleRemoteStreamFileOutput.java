/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.input;

import java.io.Writer;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.IFSFile;
import com.ibm.as400.access.IFSFileWriter;

import de.tools400.lpex.irpgformatter.Messages;
import de.tools400.lpex.irpgformatter.formatter.FormattedResult;
import de.tools400.lpex.irpgformatter.formatter.RpgleFormatterException;

/**
 * Writes formatted source lines back to a remote IFS stream file via JT400, in
 * the CCSID of the file. Each line ends with a line feed, the last one, too.
 */
public class RpgleRemoteStreamFileOutput implements IRpgleOutput {

    private final AS400 system;
    private final String path;

    RpgleRemoteStreamFileOutput(AS400 system, String path) {
        this.system = system;
        this.path = path;
    }

    @Override
    public boolean writeSourceLines(FormattedResult result) throws RpgleFormatterException {

        IFSFile file = new IFSFile(system, path);

        try (Writer writer = new IFSFileWriter(file, RpgleRemoteStreamFileInput.getCcsid(file))) {

            for (String line : result.toLines()) {
                writer.write(line);
                writer.write("\n");
            }

            return true;

        } catch (Exception e) {
            throw new RpgleFormatterException(Messages.bind(Messages.Error_Failed_writing_file_A, path), e);
        }
    }
}
