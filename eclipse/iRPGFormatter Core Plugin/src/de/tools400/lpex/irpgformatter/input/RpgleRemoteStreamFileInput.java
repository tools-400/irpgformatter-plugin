/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.input;

import java.io.IOException;
import java.io.Reader;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.IFSFile;
import com.ibm.as400.access.IFSFileReader;

import de.tools400.lpex.irpgformatter.utils.FileUtils;

/**
 * {@link IRpgleInput} implementation that wraps a remote IFS stream file
 * accessed via JT400. The file is read in its CCSID.
 */
public class RpgleRemoteStreamFileInput extends AbstractStreamFileInput {

    private static final int CCSID_NO_CONVERSION = 65535;
    private static final int CCSID_UTF8 = 1208;

    private final AS400 system;
    private final String path;

    RpgleRemoteStreamFileInput(AS400 system, String path) {
        this.system = system;
        this.path = path;
    }

    @Override
    protected Reader openReader() throws Exception {

        IFSFile file = new IFSFile(system, path);

        return new IFSFileReader(file, getCcsid(file));
    }

    /**
     * Returns the CCSID, in which the file is read and written. A file without
     * conversion (65535) is read and written as UTF-8, like before.
     */
    static int getCcsid(IFSFile file) throws IOException {

        int ccsid = file.getCCSID();
        if (ccsid == CCSID_NO_CONVERSION) {
            return CCSID_UTF8;
        }

        return ccsid;
    }

    @Override
    public String getName() {
        return path;
    }

    public String getSourceType() throws Exception {
        return FileUtils.getExtension(path);
    }

    @Override
    public IRpgleOutput getOutput() {
        return new RpgleRemoteStreamFileOutput(system, path);
    }

    @Override
    public String toString() {

        String buffer = String.format("Remote Stream File: %s", getName());

        return buffer;
    }
}
