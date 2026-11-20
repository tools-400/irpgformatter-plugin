/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

import org.junit.Test;

import de.tools400.lpex.irpgformatter.Messages;
import de.tools400.lpex.irpgformatter.handlers.BatchSourceKind;

/**
 * Unit tests for the summary of a batch, that formats several sources. The view
 * of the problems shows it above the problems.
 */
public class BatchSourceKindTest extends AbstractTestCase {

    /**
     * Without problems there is no summary, so that the view does not keep the
     * summary of a former batch.
     */
    @Test
    public void getSummary_allFormatted_isNull() {
        assertNull(BatchSourceKind.SOURCE_MEMBERS.getSummary(5, 0, 0));
        assertNull(BatchSourceKind.STREAM_FILES.getSummary(0, 0, 0));
    }

    @Test
    public void getSummary_notFormatted_tellsBothNumbers() {
        assertEquals(MessageFormat.format(Messages.Error_A_members_formatted_B_errors, 2, 3), BatchSourceKind.SOURCE_MEMBERS.getSummary(2, 3, 0));
        assertEquals(MessageFormat.format(Messages.Error_A_formatted_B_errors, 0, 3), BatchSourceKind.STREAM_FILES.getSummary(0, 3, 0));
    }

    @Test
    public void getSummary_statementErrors_tellsTheSourcesWithErrors() {
        assertEquals(MessageFormat.format(Messages.Error_A_members_formatted_with_statement_errors_B, 5, 2),
            BatchSourceKind.SOURCE_MEMBERS.getSummary(5, 0, 2));
    }

    @Test
    public void getSummary_notFormattedAndStatementErrors_tellsAllNumbers() {
        assertEquals(MessageFormat.format(Messages.Error_A_files_formatted_B_failed_C_with_statement_errors, 2, 3, 1),
            BatchSourceKind.STREAM_FILES.getSummary(2, 3, 1));
    }

    /**
     * The sources, that were not formatted, also contain sources, that were
     * only skipped, e.g. because they are not free-form, so that the summary
     * must not say, that they could not be written.
     */
    @Test
    public void summaryOfNotFormattedSources_saysNotFormatted() {

        assertEquals("2 members formatted (1 with statement errors or warnings); 3 not formatted:",
            format(Locale.ROOT, "Error_A_members_formatted_B_failed_C_with_statement_errors", 2, 3, 1));
        assertEquals("2 files formatted (1 with statement errors or warnings); 3 not formatted:",
            format(Locale.ROOT, "Error_A_files_formatted_B_failed_C_with_statement_errors", 2, 3, 1));
        assertEquals("2 Member formatiert (1 mit Fehlern oder Warnungen auf Anweisungsebene); 3 nicht formatiert:",
            format(Locale.GERMAN, "Error_A_members_formatted_B_failed_C_with_statement_errors", 2, 3, 1));
    }

    /**
     * Returns the message of the given language, independent of the language,
     * in which the tests run.
     */
    private String format(Locale locale, String key, Object... arguments) {

        ResourceBundle bundle = ResourceBundle.getBundle("de.tools400.lpex.irpgformatter.messages", locale,
            ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_PROPERTIES));

        return MessageFormat.format(bundle.getString(key), arguments);
    }
}
