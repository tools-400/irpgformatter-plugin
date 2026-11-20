/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.iagentbridge.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import de.tools400.iagentbridge.mcp.McpToolResult;
import de.tools400.lpex.irpgformatter.api.FileFormatProblem;
import de.tools400.lpex.irpgformatter.api.FileFormatProblem.Severity;
import de.tools400.lpex.irpgformatter.api.FileFormatResult;
import de.tools400.lpex.irpgformatter.iagentbridge.FormatRpgReport;

public class FormatRpgReportTest {

    private static final List<FileFormatProblem> NO_PROBLEMS = Collections.emptyList();

    @Test
    public void testFormatted_workspaceFile() {

        FormatRpgReport report = new FormatRpgReport();
        report.addResult("/c/ws/p/qrpglesrc/a.rpgle", "p", FileFormatResult.create(1, 0, false, true, true, NO_PROBLEMS));

        JsonObject result = onlyResult(report);
        assertEquals("/c/ws/p/qrpglesrc/a.rpgle", result.get("file").getAsString());
        assertEquals("p", result.get("project").getAsString());
        assertEquals("project", result.get("settings").getAsString());
        assertEquals("formatted", result.get("status").getAsString());
        assertTrue(result.get("changed").getAsBoolean());
        assertFalse(result.has("problems"));
        assertFalse(result.has("error"));
        assertFalse(report.isFailed());
    }

    @Test
    public void testFormatted_localFile() {

        FormatRpgReport report = new FormatRpgReport();
        report.addResult("C:/temp/a.rpgle", null, FileFormatResult.create(1, 0, false, false, false, NO_PROBLEMS));

        JsonObject result = onlyResult(report);
        assertFalse(result.has("project"));
        assertEquals("workspace", result.get("settings").getAsString());
        assertFalse(result.get("changed").getAsBoolean());
        assertFalse(report.isFailed());
    }

    @Test
    public void testFormatted_withProblems() {

        List<FileFormatProblem> problems = Arrays.asList(new FileFormatProblem(Severity.ERROR, 12, "Error on line #12: x"),
            new FileFormatProblem(Severity.WARNING, 30, "Statement not closed."));

        FormatRpgReport report = new FormatRpgReport();
        report.addResult("a.rpgle", null, FileFormatResult.create(1, 0, false, true, false, problems));

        JsonArray json = onlyResult(report).getAsJsonArray("problems");
        assertEquals(2, json.size());
        assertProblem(json.get(0).getAsJsonObject(), "ERROR", 12, "Error on line #12: x");
        assertProblem(json.get(1).getAsJsonObject(), "WARNING", 30, "Statement not closed.");
        assertFalse(report.isFailed());
    }

    @Test
    public void testSkipped() {

        FormatRpgReport report = new FormatRpgReport();
        report.addResult("a.rpgle", null, FileFormatResult.create(0, 1, false, false, false,
            Arrays.asList(new FileFormatProblem(Severity.INFORMATION, 0, "Source is not in **free format."))));

        JsonObject result = onlyResult(report);
        assertEquals("skipped", result.get("status").getAsString());
        JsonObject problem = result.getAsJsonArray("problems").get(0).getAsJsonObject();
        assertEquals("INFORMATION", problem.get("severity").getAsString());
        assertFalse("A problem without line has no line number", problem.has("line"));
        assertEquals("Source is not in **free format.", problem.get("message").getAsString());
        assertFalse("A skipped file is no error", report.isFailed());
    }

    @Test
    public void testFailed() {

        FormatRpgReport report = new FormatRpgReport();
        report.addResult("a.rpgle", null,
            FileFormatResult.create(0, 1, false, false, false, Arrays.asList(new FileFormatProblem(Severity.ERROR, 2, "DCL-DS x is not closed."))));

        assertEquals("failed", onlyResult(report).get("status").getAsString());
        assertTrue(report.isFailed());
    }

    @Test
    public void testCanceled() {

        FormatRpgReport report = new FormatRpgReport();
        report.addResult("a.rpgle", null, FileFormatResult.create(0, 0, true, false, false, NO_PROBLEMS));
        report.addCanceled("b.rpgle");

        JsonArray results = results(report);
        assertEquals("canceled", results.get(0).getAsJsonObject().get("status").getAsString());
        JsonObject second = results.get(1).getAsJsonObject();
        assertEquals("b.rpgle", second.get("file").getAsString());
        assertEquals("canceled", second.get("status").getAsString());
        assertTrue(report.isFailed());
    }

    @Test
    public void testError() {

        FormatRpgReport report = new FormatRpgReport();
        report.addError("x.rpgle", "File not found.");

        JsonObject result = onlyResult(report);
        assertEquals("x.rpgle", result.get("file").getAsString());
        assertEquals("File not found.", result.get("error").getAsString());
        assertFalse(result.has("status"));
        assertTrue(report.isFailed());
    }

    @Test
    public void testOrderAndWorkspace() {

        FormatRpgReport report = new FormatRpgReport();
        report.addError("1.rpgle", "File not found.");
        report.addResult("2.rpgle", null, FileFormatResult.create(1, 0, false, false, false, NO_PROBLEMS));

        JsonObject json = report.toJson("C:\\workspaces\\ws");
        assertEquals("C:\\workspaces\\ws", json.get("workspace").getAsString());
        assertEquals("1.rpgle", json.getAsJsonArray("results").get(0).getAsJsonObject().get("file").getAsString());
        assertEquals("2.rpgle", json.getAsJsonArray("results").get(1).getAsJsonObject().get("file").getAsString());
    }

    @Test
    public void testToolResult() {

        FormatRpgReport report = new FormatRpgReport();
        report.addResult("a<b>.rpgle", null, FileFormatResult.create(1, 0, false, false, false, NO_PROBLEMS));

        McpToolResult result = report.toToolResult("ws");

        assertFalse(result.isError());
        assertTrue(result.getText(), result.getText().contains("\"file\": \"a<b>.rpgle\""));
        assertEquals("ws", new JsonParser().parse(result.getText()).getAsJsonObject().get("workspace").getAsString());
    }

    @Test
    public void testToolResult_failed() {

        FormatRpgReport report = new FormatRpgReport();
        report.addError("a.rpgle", "File not found.");

        assertTrue(report.toToolResult("ws").isError());
    }

    private static JsonArray results(FormatRpgReport report) {
        return report.toJson("ws").getAsJsonArray("results");
    }

    private static JsonObject onlyResult(FormatRpgReport report) {

        JsonArray results = results(report);
        assertEquals(1, results.size());

        return results.get(0).getAsJsonObject();
    }

    private static void assertProblem(JsonObject problem, String severity, int line, String message) {
        assertEquals(severity, problem.get("severity").getAsString());
        assertEquals(line, problem.get("line").getAsInt());
        assertEquals(message, problem.get("message").getAsString());
    }
}
