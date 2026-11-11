/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.formatter;

import de.tools400.lpex.irpgformatter.Messages;

public class CannotAddLineSegmentException extends RpgleFormatterException {

    private static final long serialVersionUID = 1L;

    public CannotAddLineSegmentException(int lineNumber) {
        super(Messages.bind(Messages.Error_Error_on_line_A_Cannot_add_line_segment_Line_is_already_complete, lineNumber));
    }
}
