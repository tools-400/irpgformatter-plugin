/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.handlers;

import com.ibm.as400.access.AS400;
import com.ibm.etools.iseries.subsystems.qsys.api.IBMiConnection;

import de.tools400.lpex.irpgformatter.utils.IBMiUtils;

public class SourceMember {

    private String connectionName;
    private String profileName;
    private String libraryName;
    private String fileName;
    private String memberName;
    private String sourceType;

    public SourceMember(String connectionName, String profileName, String libraryName, String fileName, String memberName, String sourceType) {
        this.connectionName = connectionName;
        this.profileName = profileName;
        this.libraryName = libraryName;
        this.fileName = fileName;
        this.memberName = memberName;
        this.sourceType = sourceType;
    }

    public String getConnectionName() {
        return connectionName;
    }

    public String getProfileName() {
        return profileName;
    }

    public String getLibraryName() {
        return libraryName;
    }

    public String getFileName() {
        return fileName;
    }

    public String getMemberName() {
        return memberName;
    }

    public String getSourceType() {
        return sourceType;
    }

    public int getRecordLength() throws Exception {

        AS400 system = IBMiConnection.getConnection(profileName, connectionName).getAS400ToolboxObject();
        int sourceLength = IBMiUtils.getSourceLength(system, libraryName, fileName);

        return sourceLength;
    }

    @Override
    public String toString() {
        return libraryName + "/" + fileName + "(" + memberName + ") - " + sourceType;
    }
}
