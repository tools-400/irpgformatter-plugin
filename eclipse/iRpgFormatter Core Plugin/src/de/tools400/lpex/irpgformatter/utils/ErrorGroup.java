/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.utils;

/**
 * A labeled group of error detail messages, used by master/detail error
 * dialogs (e.g. one group per source member, with the statement-level errors
 * inside). The label typically identifies the affected resource (member,
 * file, ...), the details describe what went wrong inside that resource.
 */
public class ErrorGroup {

    private final String label;
    private final String[] details;

    public ErrorGroup(String label, String[] details) {
        this.label = label;
        this.details = details;
    }

    public String getLabel() {
        return label;
    }

    public String[] getDetails() {
        return details;
    }

    public int getDetailCount() {
        return details == null ? 0 : details.length;
    }
}
