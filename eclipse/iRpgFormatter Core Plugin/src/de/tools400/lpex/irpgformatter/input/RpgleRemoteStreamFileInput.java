/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.input;

import java.io.InputStreamReader;
import java.io.Reader;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.IFSFileInputStream;

import de.tools400.lpex.irpgformatter.utils.FileUtils;

/**
 * {@link IRpgleInput} implementation that wraps a remote IFS stream file
 * accessed via JT400.
 */
public class RpgleRemoteStreamFileInput extends AbstractStreamFileInput {

    private final AS400 system;
    private final String path;

    RpgleRemoteStreamFileInput(AS400 system, String path) {
        this.system = system;
        this.path = path;
    }

    @Override
    protected Reader openReader() throws Exception {
        return new InputStreamReader(new IFSFileInputStream(system, path), "UTF-8");
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
