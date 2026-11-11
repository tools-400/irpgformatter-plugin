/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.utils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class DateUtils {

    private static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyMMdd");

    private DateUtils() {
        // Utility class
    }

    public static int getSourceLineDate(Date date) {
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return Integer.parseInt(localDate.format(dateTimeFormatter));
    }

    public static int getSourceLineDate() {
        return Integer.parseInt(LocalDate.now().format(dateTimeFormatter));
    }
}
