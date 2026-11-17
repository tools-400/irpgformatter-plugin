/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.problems;

import java.io.File;
import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.rse.files.ui.resources.SystemEditableRemoteFile;
import org.eclipse.rse.subsystems.files.core.SystemIFileProperties;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IURIEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

import com.ibm.etools.iseries.rse.ui.resources.QSYSEditableRemoteSourceFileMember;
import com.ibm.etools.iseries.services.qsys.api.IQSYSMember;
import com.ibm.etools.iseries.subsystems.qsys.api.IBMiConnection;
import com.ibm.lpex.alef.LpexAbstractTextEditor;
import com.ibm.lpex.core.LpexView;
import com.ibm.lpex.core.LpexViewAdapter;

import de.tools400.lpex.irpgformatter.handlers.SourceMember;

/**
 * Knows the problem source of each LPEX editor, in which a source was
 * formatted. The source is the member, the stream file or the file, that the
 * editor shows, so that formatting it in the editor replaces the problems of
 * formatting it from Remote Systems or the Project Explorer, and vice versa.
 * These problems stay, when the editor is closed, and the view can open the
 * source again. Only, if the source of an editor cannot be determined, the
 * editor itself is the source ({@link EditorSource}); its problems are removed,
 * when the editor is closed.
 */
public final class EditorSources {

    private static final Map<LpexView, IProblemSource> SOURCES = new WeakHashMap<>();

    private EditorSources() {
        // Utility class
    }

    /**
     * Returns the problem source of the source in the given editor view.
     *
     * @param view - the view of the LPEX editor
     * @return the source
     */
    public static synchronized IProblemSource get(LpexView view) {

        IProblemSource source = SOURCES.get(view);
        if (source == null) {
            source = createSource(view);
            SOURCES.put(view, source);
            addDisposeListener(view);
        }

        return source;
    }

    /**
     * Returns the source, that the editor of the view shows, e.g. the member,
     * so that its problems have the same key, as if it was formatted from Remote
     * Systems or the Project Explorer. If the source cannot be determined, the
     * editor itself is the source.
     */
    private static IProblemSource createSource(LpexView view) {

        IEditorPart editor = findEditor(view);
        if (editor != null) {
            IProblemSource source = getSource(editor.getEditorInput());
            if (source != null) {
                return source;
            }
        }

        return new EditorSource(view);
    }

    /**
     * Returns the source of an editor input: a member or a stream file of the
     * IFS, that Remote Systems downloaded into a local copy, a file of the
     * workspace or a file of the local file system.
     *
     * @param input - the input of the editor
     * @return the source or <code>null</code>, if it cannot be determined
     */
    private static IProblemSource getSource(IEditorInput input) {

        IFile file = (IFile)input.getAdapter(IFile.class);
        if (file != null) {
            SystemIFileProperties properties = new SystemIFileProperties(file);
            Object remoteObject = properties.getRemoteFileObject();
            if (remoteObject instanceof QSYSEditableRemoteSourceFileMember) {
                return getMemberSource((QSYSEditableRemoteSourceFileMember)remoteObject);
            } else if (remoteObject instanceof SystemEditableRemoteFile) {
                IRemoteFile remoteFile = ((SystemEditableRemoteFile)remoteObject).getRemoteFile();
                return remoteFile != null ? new RemoteStreamFileSource(remoteFile) : null;
            } else if (remoteObject == null && properties.getRemoteFilePath() == null) {
                // not a local copy of Remote Systems
                return new WorkspaceFileSource(file);
            }
            return null;
        }

        if (input instanceof IURIEditorInput) {
            URI uri = ((IURIEditorInput)input).getURI();
            if (uri != null && "file".equals(uri.getScheme())) { //$NON-NLS-1$
                return new LocalFileSource(new File(uri));
            }
        }

        return null;
    }

    private static IProblemSource getMemberSource(QSYSEditableRemoteSourceFileMember editableMember) {

        IQSYSMember member = editableMember.getMember();
        IBMiConnection connection = editableMember.getISeriesConnection();
        if (member == null || connection == null) {
            return null;
        }

        // the source type like RemoteMembersResolver
        String sourceType = member.getType() != null ? member.getType().toUpperCase() : ""; //$NON-NLS-1$

        return new MemberSource(new SourceMember(connection.getConnectionName(), connection.getProfileName(), member.getLibrary(),
            member.getFile(), member.getName(), sourceType));
    }

    /**
     * Remembers the problem source of a source, that was opened from the view.
     * The problems, that were reported for the editor before, are removed.
     *
     * @param view - the view of the LPEX editor
     * @param source - the source, that was opened
     */
    public static synchronized void put(LpexView view, IProblemSource source) {

        IProblemSource previousSource = SOURCES.put(view, source);
        if (previousSource == null) {
            addDisposeListener(view);
        } else if (previousSource instanceof EditorSource) {
            FormatterProblems.getInstance().remove(Collections.singletonList(previousSource.getKey()));
        }
    }

    /**
     * Returns the editor, that shows the given LPEX view.
     *
     * @param view - the view of the LPEX editor
     * @return the editor or <code>null</code>, if it is not open
     */
    public static IEditorPart findEditor(LpexView view) {

        for (IWorkbenchWindow window : PlatformUI.getWorkbench().getWorkbenchWindows()) {
            for (IWorkbenchPage page : window.getPages()) {
                for (IEditorReference reference : page.getEditorReferences()) {
                    IEditorPart editor = reference.getEditor(false);
                    if (editor instanceof LpexAbstractTextEditor && showsView((LpexAbstractTextEditor)editor, view)) {
                        return editor;
                    }
                }
            }
        }

        return null;
    }

    /**
     * Returns the editor of a local copy, e.g. of a member, that Remote Systems
     * opened.
     *
     * @param page - the workbench page
     * @param localResource - the local copy or <code>null</code>
     * @return the editor or <code>null</code>
     */
    public static IEditorPart findEditor(IWorkbenchPage page, IFile localResource) {

        if (localResource == null) {
            return null;
        }

        return page.findEditor(new FileEditorInput(localResource));
    }

    /**
     * Returns the LPEX view of an editor.
     *
     * @param editor - the editor
     * @return the view or <code>null</code>, if it is no LPEX editor
     */
    public static LpexView getView(IEditorPart editor) {

        if (editor instanceof LpexAbstractTextEditor) {
            return ((LpexAbstractTextEditor)editor).getActiveLpexView();
        }

        return null;
    }

    private static boolean showsView(LpexAbstractTextEditor editor, LpexView view) {
        return editor.getFirstLpexView() == view || editor.getActiveLpexView() == view || editor.getLpexView() == view;
    }

    private static void addDisposeListener(LpexView view) {

        view.addLpexViewListener(new LpexViewAdapter() {
            @Override
            public void disposed(LpexView disposedView) {
                removeEditor(disposedView);
            }
        });
    }

    private static void removeEditor(LpexView view) {

        IProblemSource source;
        synchronized (EditorSources.class) {
            source = SOURCES.remove(view);
        }

        if (source instanceof EditorSource) {
            FormatterProblems.getInstance().remove(Collections.singletonList(source.getKey()));
        }
    }
}
