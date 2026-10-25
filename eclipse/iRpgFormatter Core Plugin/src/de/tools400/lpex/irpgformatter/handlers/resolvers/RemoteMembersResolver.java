/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.handlers.resolvers;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
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

import de.tools400.lpex.irpgformatter.IRpgleFormatterPlugin;
import de.tools400.lpex.irpgformatter.formatter.RpgleFormatter;
import de.tools400.lpex.irpgformatter.handlers.FormatRemoteMemberHandler;
import de.tools400.lpex.irpgformatter.handlers.SourceMember;

/**
 * Flattens a selection of the QSYS file system in the <i>Remote Systems</i>
 * view (libraries, source files, source members or filter references) into an
 * array of {@link SourceMember}. A library is resolved to its source files, a
 * source file to its members. Only members of a source type, that the
 * formatter supports, are returned. Used by {@link FormatRemoteMemberHandler}.
 */
public class RemoteMembersResolver extends AbstractResolver {

    private List<Object> sourceMembers;

    public RemoteMembersResolver(IProgressMonitor monitor) {
        super(monitor);
        this.sourceMembers = new LinkedList<>();
    }

    public SourceMember[] resolveRemoteMembers(IStructuredSelection selection) {

        Iterator<?> iterator = selection.iterator();
        while (iterator.hasNext() && !isCanceled()) {
            Object element = iterator.next();
            resolveElement(element);
        }

        return sourceMembers.toArray(new SourceMember[sourceMembers.size()]);
    }

    private void resolveElement(Object element) {

        try {

            if ((element instanceof IQSYSResource)) {
                IHost host = ((IRemoteObjectContextProvider)element).getRemoteObjectContext().getObjectSubsystem().getHost();
                addElement(host, element);
            } else if ((element instanceof SystemFilterReference)) {
                SystemFilterReference systemFilterReference = (SystemFilterReference)element;
                IHost host = ((SubSystem)systemFilterReference.getFilterPoolReferenceManager().getProvider()).getHost();
                String[] filterStrings = systemFilterReference.getReferencedFilter().getFilterStrings();
                addElementsFromFilterString(host, filterStrings);
            }

        } catch (Exception e) {
            IRpgleFormatterPlugin.logError("Failed to resolve source members.", e);
            setError(e);
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

        for (int idx2 = 0; idx2 < sourceFiles.length && !isCanceled(); idx2++) {
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

        for (int idx = 0; idx < filterStrings.length && !isCanceled(); idx++) {

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

    private void addSourceMember(IHost host, Object element) {

        IBMiConnection connection = IBMiConnection.getConnection(host);
        String connectionName = connection.getConnectionName();
        String profileName = connection.getProfileName();
        String library = getLibraryName(element);
        String file = getFileName(element);
        String member = getMemberName(element);
        String type = getMemberType(element).toUpperCase();

        if (RpgleFormatter.isSupportedSourceType(type)) {
            SourceMember sourceMember = new SourceMember(connectionName, profileName, library, file, member, type);
            sourceMembers.add(sourceMember);
        }
    }

    protected Object[] resolveFilterString(IHost host, String filterString) throws Exception {

        IBMiConnection connection = IBMiConnection.getConnection(host);
        QSYSObjectSubSystem objectSubSystem = connection.getQSYSObjectSubSystem();

        return objectSubSystem.resolveFilterString(filterString, getMonitor());
    }

    /*
     * Filter string producers
     */

    @SuppressWarnings("unused")
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

    @SuppressWarnings("unused")
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

    private String getLibraryName(Object resource) {
        if (resource instanceof QSYSRemoteSourceMember) {
            return ((QSYSRemoteSourceMember)resource).getLibrary();
        } else if (resource instanceof IQSYSObject) {
            return ((IQSYSResource)resource).getLibrary();
        } else {
            throw new IllegalArgumentException("Illegal object type: " + resource.getClass().getName());
        }
    }

    private String getFileName(Object resource) {
        if (resource instanceof IQSYSMember) {
            return ((IQSYSMember)resource).getFile();
        } else if (resource instanceof IQSYSFile) {
            return ((IQSYSObject)resource).getName();
        } else {
            throw new IllegalArgumentException("Illegal object type: " + resource.getClass().getName());
        }
    }

    private String getMemberName(Object resource) {
        if (resource instanceof IQSYSMember) {
            return ((IQSYSMember)resource).getName();
        } else {
            throw new IllegalArgumentException("Illegal object type: " + resource.getClass().getName());
        }
    }

    private String getMemberType(Object resource) {
        if (resource instanceof IQSYSMember) {
            return ((IQSYSMember)resource).getType();
        } else {
            throw new IllegalArgumentException("Illegal object type: " + resource.getClass().getName());
        }
    }
}
