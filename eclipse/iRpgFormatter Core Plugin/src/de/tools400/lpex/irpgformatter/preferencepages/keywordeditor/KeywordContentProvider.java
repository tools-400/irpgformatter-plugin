/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.preferencepages.keywordeditor;

import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;

/**
 * Content provider for the keyword TableViewer. Expects input to be a List of
 * KeywordEntry objects.
 */
public class KeywordContentProvider implements IStructuredContentProvider {

    @Override
    public Object[] getElements(Object inputElement) {
        if (inputElement instanceof List<?>) {
            return ((List<?>)inputElement).toArray();
        }
        return new Object[0];
    }
}
