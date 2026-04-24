/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.handlers;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rse.core.filters.SystemFilterReference;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.SystemMessageObject;
import org.eclipse.rse.core.subsystems.SubSystem;

import com.ibm.etools.iseries.comm.filters.ISeriesMemberFilterString;
import com.ibm.etools.iseries.comm.filters.ISeriesObjectFilterString;
import com.ibm.etools.iseries.comm.filters.ISeriesObjectTypeAttrList;
import com.ibm.etools.iseries.rse.ui.ResourceTypeUtil;
import com.ibm.etools.iseries.services.qsys.api.IQSYSFile;
import com.ibm.etools.iseries.services.qsys.api.IQSYSMember;
import com.ibm.etools.iseries.services.qsys.api.IQSYSObject;
import com.ibm.etools.iseries.services.qsys.api.IQSYSResource;
import com.ibm.etools.iseries.subsystems.qsys.api.IBMiConnection;
import com.ibm.etools.iseries.subsystems.qsys.objects.IRemoteObjectContextProvider;
import com.ibm.etools.iseries.subsystems.qsys.objects.QSYSObjectSubSystem;
import com.ibm.etools.iseries.subsystems.qsys.objects.QSYSRemoteSourceMember;

public class SourceMembersResolver {

    private List<Object> sourceMembers;

    public SourceMembersResolver() {
        this.sourceMembers = new LinkedList<>();
    }

    public SourceMember[] resolveSourceMembers(Object element) {

        try {

            if ((element instanceof IStructuredSelection)) {

                Iterator<?> iterator = ((IStructuredSelection)element).iterator();
                while (iterator.hasNext()) {
                    Object elementItem = iterator.next();
                    resolveSourceMembers(elementItem);
                }

            } else if ((element instanceof IQSYSResource)) {

                if ((element instanceof IQSYSResource)) {

                    IHost host = ((IRemoteObjectContextProvider)element).getRemoteObjectContext().getObjectSubsystem().getHost();
                    // Host: GFD400
                    addElement(host, element);

                } else if ((element instanceof SystemFilterReference)) {

                    SystemFilterReference systemFilterReference = (SystemFilterReference)element;
                    IHost host = ((SubSystem)systemFilterReference.getFilterPoolReferenceManager().getProvider()).getHost();
                    // Host: GFD400
                    SystemFilterReference filterReference = (SystemFilterReference)element;
                    String[] _filterStrings = filterReference.getReferencedFilter().getFilterStrings();
                    addElementsFromFilterString(host, _filterStrings);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return sourceMembers.toArray(new SourceMember[sourceMembers.size()]);
    }

    private void addElement(IHost host, Object element) throws Exception {

        if ((ResourceTypeUtil.isLibrary(element))) {
            String library = getLibraryName(element);
            addElementsFromLibrary(host, library);
        } else if ((ResourceTypeUtil.isSourceFile(element))) {
            String library = getLibraryName(element);
            String file = getFileName(element);
            addElementsFromSourceFile(host, library, file);
        } else if (ResourceTypeUtil.isSrcMember(element)) {
            addSourceMember(host, element);
        }
    }

    private void addElementsFromLibrary(IHost host, String library) throws Exception {

        Object[] sourceFiles = null;

        String objectFilterString = produceLibraryFilterString(library);

        sourceFiles = resolveFilterString(host, objectFilterString.toString());

        if ((sourceFiles == null) || (sourceFiles.length == 0)) {
            return;
        }

        Object firstObject = sourceFiles[0];
        if (firstObject instanceof SystemMessageObject) {
            throw new Exception(((SystemMessageObject)firstObject).getMessage());
        }

        for (int idx2 = 0; idx2 < sourceFiles.length; idx2++) {

            Object element = sourceFiles[idx2];
            if (isSourceFile(element)) {
                addElementsFromSourceFile(host, getLibraryName(element), getFileName(element));
            }
        }
    }

    private void addElementsFromSourceFile(IHost host, String library, String sourceFile) throws Exception {

        ISeriesMemberFilterString _memberFilterString = new ISeriesMemberFilterString();
        _memberFilterString.setLibrary(library);
        _memberFilterString.setFile(sourceFile);
        _memberFilterString.setMember("*"); //$NON-NLS-1$
        _memberFilterString.setMemberType("*"); //$NON-NLS-1$

        addElementsFromFilterString(host, _memberFilterString.toString());
    }

    private void addElementsFromFilterString(IHost host, String... filterStrings) throws Exception {

        Object[] children = null;

        for (int idx = 0; idx < filterStrings.length; idx++) {

            children = resolveFilterString(host, filterStrings[idx]);

            if ((children != null) && (children.length != 0)) {

                Object firstObject = children[0];

                if (firstObject instanceof SystemMessageObject) {
                    throw new RuntimeException("Error: " + ((SystemMessageObject)firstObject).getMessage());
                } else {
                    for (int idx2 = 0; idx2 < children.length; idx2++) {
                        Object element = children[idx2];
                        addElement(host, element);
                    }
                }
            }
        }
    }

    private void addSourceMember(IHost host, Object element) {

        String profileName = IBMiConnection.getConnection(host).getConnectionName();
        String connectionName = IBMiConnection.getConnection(host).getProfileName();
        String library = getLibraryName(element);
        String file = getFileName(element);
        String member = getMemberName(element);
        String type = getMemberType(element).toUpperCase();

        if ("RPGLE".equals(type) || "SQLRPGLE".equals(type)) {
            SourceMember sourceMember = new SourceMember(profileName, connectionName, library, file, member, type);
            sourceMembers.add(sourceMember);
        }
    }

    protected Object[] resolveFilterString(IHost host, String filterString) throws Exception {

        IBMiConnection connection = IBMiConnection.getConnection(host);
        QSYSObjectSubSystem objectSubSystem = connection.getQSYSObjectSubSystem();

        return objectSubSystem.resolveFilterString(filterString, null);
    }

    /*
     * Filter string producers
     */

    protected String produceLibraryFilterString(String library) {

        ISeriesObjectFilterString objectFilterString = new ISeriesObjectFilterString();
        objectFilterString.setLibrary(library);
        objectFilterString.setObject("*");
        objectFilterString.setObjectType("*FILE");
        String attributes = "*FILE:PF-SRC";
        objectFilterString.setObjectTypeAttrList(new ISeriesObjectTypeAttrList(attributes));

        return objectFilterString.toString();
    }

    /*
     * Object information provider
     */

    protected boolean isLibrary(Object object) {
        return ResourceTypeUtil.isLibrary(object);
    }

    protected boolean isSourceFile(Object object) {
        return ResourceTypeUtil.isSourceFile(object);
    }

    protected boolean isSourceMember(Object object) {
        return ResourceTypeUtil.isSrcMember(object);
    }

    /*
     * Resource attributes
     */

    protected String getLibraryName(Object resource) {
        if (resource instanceof QSYSRemoteSourceMember) {
            return ((QSYSRemoteSourceMember)resource).getLibrary();
        } else if (resource instanceof IQSYSObject) {
            return ((IQSYSResource)resource).getLibrary();
        } else {
            throw new IllegalArgumentException("Illegal object type: " + resource.getClass().getName());
        }
    }

    protected String getFileName(Object resource) {
        if (resource instanceof IQSYSMember) {
            return ((IQSYSMember)resource).getFile();
        } else if (resource instanceof IQSYSFile) {
            return ((IQSYSObject)resource).getName();
        } else {
            throw new IllegalArgumentException("Illegal object type: " + resource.getClass().getName());
        }
    }

    protected String getMemberName(Object resource) {
        if (resource instanceof IQSYSMember) {
            return ((IQSYSMember)resource).getName();
        } else {
            throw new IllegalArgumentException("Illegal object type: " + resource.getClass().getName());
        }
    }

    protected String getMemberType(Object resource) {
        if (resource instanceof IQSYSMember) {
            return ((IQSYSMember)resource).getType();
        } else {
            throw new IllegalArgumentException("Illegal object type: " + resource.getClass().getName());
        }
    }
}
