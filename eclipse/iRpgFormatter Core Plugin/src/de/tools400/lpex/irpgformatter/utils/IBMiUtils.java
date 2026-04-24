/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.utils;

import java.io.IOException;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.AS400Exception;
import com.ibm.as400.access.AS400FileRecordDescription;
import com.ibm.as400.access.AS400SecurityException;
import com.ibm.as400.access.FieldDescription;
import com.ibm.as400.access.QSYSObjectPathName;
import com.ibm.as400.access.RecordFormat;

public class IBMiUtils {

    private IBMiUtils() {
        // Utility class
    }

    public static RecordFormat getRecordFormat(AS400 system, String libraryName, String fileName)
        throws AS400Exception, AS400SecurityException, InterruptedException, IOException {
        return getRecordFormat(system, getFilePath(libraryName, fileName));
    }

    public static RecordFormat getRecordFormat(AS400 system, String path)
        throws AS400Exception, AS400SecurityException, InterruptedException, IOException {

        AS400FileRecordDescription desc = new AS400FileRecordDescription(system, path);
        RecordFormat[] formats = desc.retrieveRecordFormat();

        return formats[0];
    }

    public static QSYSObjectPathName getQSYSLibraryPath(String libraryName) {
        return new QSYSObjectPathName("QSYS", libraryName, "LIB");
    }

    public static QSYSObjectPathName getQSYSFilePath(String libraryName, String fileName) {
        return new QSYSObjectPathName(libraryName, fileName, "FILE");
    }

    public static QSYSObjectPathName getQSYSMemberPath(String libraryName, String fileName, String memberName) {
        return new QSYSObjectPathName(libraryName, fileName, memberName, "MBR");
    }

    public static String getLibraryPath(String libraryName) {
        return getQSYSLibraryPath(libraryName).getPath();
    }

    public static String getFilePath(String libraryName, String fileName) {
        return getQSYSFilePath(libraryName, fileName).getPath();
    }

    public static String getMemberPath(String libraryName, String fileName, String memberName) {
        return getQSYSMemberPath(libraryName, fileName, memberName).getPath();
    }

    public static int getRecordLength(AS400 system, String libraryName, String fileName)
        throws AS400Exception, AS400SecurityException, InterruptedException, IOException {

        String path = getFilePath(libraryName, fileName);

        AS400FileRecordDescription recordDescription = new AS400FileRecordDescription(system, path);
        RecordFormat[] format = recordDescription.retrieveRecordFormat();
        FieldDescription[] fieldDescriptions = format[0].getFieldDescriptions();

        int recordLength = 0;
        for (FieldDescription fieldDescription : fieldDescriptions) {
            recordLength += fieldDescription.getLength();
        }

        return recordLength;
    }

    public static int getSourceLength(AS400 system, String libraryName, String fileName) throws Exception {

        RecordFormat recordFormat = getRecordFormat(system, libraryName, fileName);

        FieldDescription fieldDescription = recordFormat.getFieldDescription("SRCDTA");

        return fieldDescription.getLength();
    }
}
