/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.utils;

import java.util.List;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.AS400Message;
import com.ibm.as400.access.CommandCall;
import com.ibm.as400.access.QSYSObjectPathName;

import de.tools400.lpex.irpgformatter.IRpgleFormatterPlugin;

public class ClCommandUtils {

    public static final String TEMP_LIBRARY_NAME = "QTEMP";
    public static final String TEMP_FILE_NAME = "$TOOLS400";
    public static final String TEMP_MEMBER_NAME = "$TLS400$$$";

    private ClCommandUtils() {
        // Utility class
    }

    public static boolean executeCommand(AS400 as400, String command) throws Exception {
        return executeCommand(as400, command, null);
    }

    public static boolean executeCommand(AS400 as400, String command, List<Message> messages) throws Exception {

        boolean isSuccess = true;

        CommandCall commandCall = new CommandCall(as400);
        commandCall.run(command);
        AS400Message[] messageList = commandCall.getMessageList();
        if (messageList.length > 0) {
            for (int idx = 0; idx < messageList.length; idx++) {
                if (messageList[idx].getType() == AS400Message.ESCAPE) {
                    isSuccess = false;
                }
                if (messages != null) {
                    messages.add(new Message(messageList[idx].getID(), messageList[idx].getText()));
                }
            }
        }

        return isSuccess;
    }

    public static boolean executeCommandChecked(AS400 system, String command) {

        try {
            if (executeCommand(system, command)) {
                return true;
            }
        } catch (Exception e) {
            IRpgleFormatterPlugin.logError("Could not execute command: " + command, e);
        }

        return false;
    }

    public static boolean ensureTempSourceFile(AS400 system, int minRecordLength) {

        try {

            String libraryName = TEMP_LIBRARY_NAME;
            String fileName = TEMP_FILE_NAME;

            if (!libraryExists(system, libraryName)) {
                return false;
            }

            if (!fileExists(system, libraryName, fileName)) {
                if (!createSourceFileChecked(system, libraryName, fileName, 240, "Temporary working file")) {
                    return false;
                }
            } else {
                int recordLength = IBMiUtils.getRecordLength(system, libraryName, fileName);
                if (recordLength < minRecordLength) {
                    if (!deleteFileChecked(system, libraryName, fileName)) {
                        return false;
                    }
                    ensureTempSourceFile(system, minRecordLength);
                }

            }

        } catch (Exception e) {
            IRpgleFormatterPlugin.logError("Could not ensure that the temporary working file exists.", e);
        }

        return true;
    }

    public static boolean ensureTempMember(AS400 system) {

        String libraryName = TEMP_LIBRARY_NAME;
        String fileName = TEMP_FILE_NAME;
        String memberName = TEMP_MEMBER_NAME;

        if (!fileExists(system, libraryName, fileName)) {
            return false;
        }

        if (!memberExists(system, libraryName, fileName, memberName)) {
            if (!addMemberChecked(system, libraryName, fileName, memberName, "TMP", "Temporary working member")) {
                return false;
            }
        }

        return true;
    }

    public static boolean removeTempMember(AS400 system) {

        String libraryName = TEMP_LIBRARY_NAME;
        String fileName = TEMP_FILE_NAME;
        String memberName = TEMP_MEMBER_NAME;

        return removeMemberChecked(system, libraryName, fileName, memberName);
    }

    public static boolean createSourceFileChecked(AS400 system, String libraryName, String fileName, int recordLength, String description) {

        String command = String.format("CRTSRCPF FILE(%s/%s) RCDLEN(%s) TEXT('%s')", libraryName, fileName, recordLength, description);
        return executeCommandChecked(system, command);
    }

    public static boolean deleteFileChecked(AS400 system, String libraryName, String fileName) {

        String command = String.format("DLTF-X FILE(%s/%s)", libraryName, fileName);
        return executeCommandChecked(system, command);
    }

    public static boolean addMemberChecked(AS400 system, String libraryName, String fileName, String memberName, String sourceType,
        String description) {

        String command = String.format("ADDPFM FILE(%s/%s) MBR(%s) TEXT('%s') SRCTYPE(%s)", libraryName, fileName, memberName, description,
            sourceType);
        return executeCommandChecked(system, command);
    }

    public static boolean removeMemberChecked(AS400 system, String libraryName, String fileName, String memberName) {

        String command = String.format("RMVM FILE(%s/%s) MBR(%s)", libraryName, fileName, memberName);
        return executeCommandChecked(system, command);
    }

    public static boolean libraryExists(AS400 system, String library) {
        return checkObject(system, IBMiUtils.getQSYSLibraryPath(library));
    }

    public static boolean fileExists(AS400 system, String library, String file) {
        return checkObject(system, IBMiUtils.getQSYSFilePath(library, file));
    }

    public static boolean memberExists(AS400 system, String library, String file, String member) {
        return checkObject(system, IBMiUtils.getQSYSMemberPath(library, file, member));
    }

    private static boolean checkObject(AS400 system, QSYSObjectPathName pathName) {

        StringBuilder command = new StringBuilder();
        command.append("CHKOBJ OBJ("); //$NON-NLS-1$

        command.append(pathName.getLibraryName());
        command.append("/"); //$NON-NLS-1$
        command.append(pathName.getObjectName());

        command.append(") OBJTYPE("); //$NON-NLS-1$
        if (isMember(pathName)) {
            command.append("*FILE"); //$NON-NLS-1$
        } else {
            command.append("*"); //$NON-NLS-1$
            command.append(pathName.getObjectType());
        }
        command.append(")"); //$NON-NLS-1$

        if (isMember(pathName)) {
            command.append(" MBR(");
            command.append(pathName.getMemberName());
            command.append(")");
        }

        return executeCommandChecked(system, command.toString());
    }

    private static boolean isMember(QSYSObjectPathName pathName) {

        if ("MBR".equals(pathName.getObjectType())) {
            if (!StringUtils.isNullOrEmpty(pathName.getMemberName())) {
                return true;
            }
        }

        return false;
    }
}
