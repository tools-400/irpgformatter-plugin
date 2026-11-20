/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.iagentbridge;

import java.util.Locale;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import de.tools400.iagentbridge.mcp.McpToolResult;
import de.tools400.lpex.irpgformatter.api.FileFormatProblem;
import de.tools400.lpex.irpgformatter.api.FileFormatResult;

/**
 * The result of the tool <code>format_rpg</code>, that is returned to the AI
 * agent: for each file the status, whether it changed, the settings and the
 * problems, or the reason, why it was not formatted.
 * <p>
 * The class has no Eclipse dependencies, so that it can be tested outside of
 * RDi.
 */
public final class FormatRpgReport {

    private final JsonArray results;
    private boolean failed;

    /**
     * Creates an empty report.
     */
    public FormatRpgReport() {
        results = new JsonArray();
        failed = false;
    }

    /**
     * Adds a file, that was not formatted, because it cannot be formatted by
     * the tool, e.g. because it does not exist.
     *
     * @param path - the path, that the agent passed
     * @param error - the reason
     */
    public void addError(String path, String error) {

        JsonObject result = new JsonObject();
        result.addProperty("file", path); //$NON-NLS-1$
        result.addProperty("error", error); //$NON-NLS-1$
        results.add(result);

        failed = true;
    }

    /**
     * Adds a file, that iRPGFormatter processed.
     *
     * @param path - the path, that the agent passed
     * @param project - the project of the file in the workspace or
     *        <code>null</code>, if the file is not part of the workspace
     * @param formatResult - the result of iRPGFormatter
     */
    public void addResult(String path, String project, FileFormatResult formatResult) {

        JsonObject result = new JsonObject();
        result.addProperty("file", path); //$NON-NLS-1$
        if (project != null) {
            result.addProperty("project", project); //$NON-NLS-1$
        }
        result.addProperty("settings", formatResult.isProjectSettings() ? "project" : "workspace"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        result.addProperty("status", formatResult.getStatus().name().toLowerCase(Locale.ENGLISH)); //$NON-NLS-1$
        result.addProperty("changed", Boolean.valueOf(formatResult.isChanged())); //$NON-NLS-1$

        if (!formatResult.getProblems().isEmpty()) {
            JsonArray problems = new JsonArray();
            for (FileFormatProblem problem : formatResult.getProblems()) {
                problems.add(toJson(problem));
            }
            result.add("problems", problems); //$NON-NLS-1$
        }

        results.add(result);

        if (formatResult.getStatus() == FileFormatResult.Status.FAILED || formatResult.getStatus() == FileFormatResult.Status.CANCELED) {
            failed = true;
        }
    }

    /**
     * Adds a file, that was not formatted, because the formatting of a file
     * before was canceled.
     *
     * @param path - the path, that the agent passed
     */
    public void addCanceled(String path) {

        JsonObject result = new JsonObject();
        result.addProperty("file", path); //$NON-NLS-1$
        result.addProperty("status", "canceled"); //$NON-NLS-1$ //$NON-NLS-2$
        results.add(result);

        failed = true;
    }

    /**
     * Returns <code>true</code>, if a file could not be formatted. A file, that
     * iRPGFormatter skipped, e.g. because it is not in free format, does not
     * count.
     */
    public boolean isFailed() {
        return failed;
    }

    /**
     * Returns the report as JSON object.
     *
     * @param workspace - the location of the RDi workspace
     * @return the report
     */
    public JsonObject toJson(String workspace) {

        JsonObject report = new JsonObject();
        report.addProperty("workspace", workspace); //$NON-NLS-1$
        report.add("results", results); //$NON-NLS-1$

        return report;
    }

    /**
     * Returns the report as result of the tool. The result is an error, if a
     * file could not be formatted.
     *
     * @param workspace - the location of the RDi workspace
     * @return the result of the tool
     */
    public McpToolResult toToolResult(String workspace) {

        String text = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(toJson(workspace));
        if (failed) {
            return McpToolResult.error(text);
        }

        return McpToolResult.success(text);
    }

    private static JsonObject toJson(FileFormatProblem problem) {

        JsonObject json = new JsonObject();
        json.addProperty("severity", problem.getSeverity().name()); //$NON-NLS-1$
        if (problem.getLineNumber() > 0) {
            json.addProperty("line", Integer.valueOf(problem.getLineNumber())); //$NON-NLS-1$
        }
        json.addProperty("message", problem.getMessage()); //$NON-NLS-1$

        return json;
    }
}
