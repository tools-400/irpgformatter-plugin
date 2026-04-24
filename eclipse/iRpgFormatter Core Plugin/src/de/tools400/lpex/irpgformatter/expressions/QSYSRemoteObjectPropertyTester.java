/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.expressions;

import org.eclipse.core.expressions.PropertyTester;

import com.ibm.etools.iseries.comm.interfaces.ISeriesHostObjectBrief;

public class QSYSRemoteObjectPropertyTester extends PropertyTester {

    public static final String PROPERTY_NAMESPACE = "de.tools400.lpex.irpgformatter.expressions.hostobjectbrief";

    public static final String PROPERTY_TYPE = "type";

    public static final String PROPERTY_SUBTYPE = "subtype";

    public boolean test(Object aReceiver, String aProperty, Object[] anArgs, Object anExpectedValue) {

        if (!(aReceiver instanceof ISeriesHostObjectBrief)) {
            return false;
        }

        ISeriesHostObjectBrief remoteObject = (ISeriesHostObjectBrief)aReceiver;

        if (anExpectedValue instanceof String) {
            String expectedValue = (String)anExpectedValue;
            if (PROPERTY_TYPE.equals(aProperty)) {
                return expectedValue.equalsIgnoreCase(remoteObject.getType());
            } else if (PROPERTY_SUBTYPE.equals(aProperty)) {
                return expectedValue.equalsIgnoreCase(remoteObject.getSubType());
            }
        }

        return false;
    }

}
