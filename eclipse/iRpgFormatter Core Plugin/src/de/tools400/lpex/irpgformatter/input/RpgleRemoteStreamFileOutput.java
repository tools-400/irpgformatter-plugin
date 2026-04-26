/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.input;

import java.io.OutputStreamWriter;
import java.io.Writer;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.IFSFileOutputStream;

import de.tools400.lpex.irpgformatter.Messages;
import de.tools400.lpex.irpgformatter.formatter.RpgleFormatterException;

/**
 * Writes formatted source lines back to a remote IFS stream file via JT400.
 */
public class RpgleRemoteStreamFileOutput implements IRpgleOutput {

    private final AS400 system;
    private final String path;

    RpgleRemoteStreamFileOutput(AS400 system, String path) {
        this.system = system;
        this.path = path;
    }

    @Override
    public boolean writeSourceLines(String[] lines) throws RpgleFormatterException {

        try {

            String content = String.join("\n", lines);
            IFSFileOutputStream outputStream = new IFSFileOutputStream(system, path);

            try {

                Writer writer = new OutputStreamWriter(outputStream, "UTF-8");

                try {
                    writer.write(content);
                } finally {
                    writer.close();
                }

            } finally {
                outputStream.close();
            }

            return true;

        } catch (Exception e) {
            throw new RpgleFormatterException(Messages.bind(Messages.Error_Failed_writing_file_A, path), e);
        }
    }
}
