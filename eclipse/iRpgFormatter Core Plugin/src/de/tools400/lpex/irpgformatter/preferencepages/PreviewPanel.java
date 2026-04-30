/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.preferencepages;

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.MarginPainter;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.text.WhitespaceCharacterPainter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.LineStyleEvent;
import org.eclipse.swt.custom.LineStyleListener;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.themes.IThemeRegistry;
import org.eclipse.ui.progress.UIJob;

import de.tools400.lpex.irpgformatter.IRpgleFormatterPlugin;
import de.tools400.lpex.irpgformatter.Messages;
import de.tools400.lpex.irpgformatter.formatter.FormattedResult;
import de.tools400.lpex.irpgformatter.formatter.RpgleFormatter;
import de.tools400.lpex.irpgformatter.input.TextLinesInput;
import de.tools400.lpex.irpgformatter.preferences.FormatterConfig;
import de.tools400.lpex.irpgformatter.preferences.Preferences;
import de.tools400.lpex.irpgformatter.utils.UIUtils;

/**
 * Encapsulates the preview panel UI and formatting logic used in the
 * iRPGFormatter preference page.
 */
public class PreviewPanel {

    public static final String PREVIEW_COMMENT_KEY = "previewComment";

    private final Supplier<FormatterConfig> configSupplier;
    private final Consumer<String> errorHandler;

    /* UI controls */
    private Group previewGroup;
    private TextViewer previewViewer;
    private Button viewEditButton;
    private Button customPreviewButton;
    private Button showWhitespacesCheckbox;
    private Spinner previewVerticalRulerSpinner;

    /* Painters and resources */
    private WhitespaceCharacterPainter whitespacePainter;
    private MarginPainter marginPainter;
    private Color marginColor;
    private Font monoFont;
    private Color overflowColor;
    private Color cursorLineColor;

    /* State */
    private boolean isEditMode;
    private boolean isCustomContent;
    private IDocumentListener previewDocumentListener;
    private int previewCursorLine = -1;
    private boolean isUpdatingPreview;
    private FormattedResult previousFormattedResult;

    private String rawDefaultSource = "";
    private String rawCustomSource = "";

    private VerticalRulerUpdaterJob rulerUpdateJob;

    /**
     * Creates the preview panel inside the given parent composite.
     *
     * @param parent the parent composite (typically the main container of the
     *        preference page)
     * @param configSupplier supplies the current {@link FormatterConfig} from
     *        UI controls; returns {@code null} when the UI is not yet fully
     *        initialised
     * @param errorHandler receives error messages (or {@code null} to clear)
     * @param preferences used only for initial default values
     */
    public PreviewPanel(Composite parent, Supplier<FormatterConfig> configSupplier, Consumer<String> errorHandler, Preferences preferences) {

        this.configSupplier = configSupplier;
        this.errorHandler = errorHandler;

        createPreviewPanel(parent, preferences);
    }

    // ------------------------------------------------------------------
    // Public API
    // ------------------------------------------------------------------

    /**
     * Returns the top-level control of this panel (for layout purposes).
     */
    public Composite getControl() {
        return previewGroup;
    }

    /**
     * Returns a {@link SelectionListener} that the preference page should
     * attach to every settings control that affects the preview. When
     * triggered, it re-formats the preview (unless the panel is in edit mode)
     * and scrolls to the comment identified by the widget's
     * {@link #PREVIEW_COMMENT_KEY} data.
     */
    public SelectionListener getPreviewUpdater() {
        return new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (!isEditMode) {
                    String comment = (String)e.widget.getData(PREVIEW_COMMENT_KEY);
                    fromatPreviewSource(comment);
                }
            }
        };
    }

    /**
     * Loads the preview sources and state from the given values (typically read
     * from {@link Preferences} during {@code loadPreferences()}).
     */
    public void loadSources(String defaultSource, String customSource, boolean customContent, int rulerColumn) {
        rawDefaultSource = defaultSource;
        rawCustomSource = customSource;
        previewVerticalRulerSpinner.setSelection(rulerColumn);
        setupPreview(customContent);
    }

    /**
     * Persists the preview state into the given {@link Preferences}.
     */
    public void storeState(Preferences preferences) {
        preferences.setFormatterPreviewVerticalRulerColumn(previewVerticalRulerSpinner.getSelection());
        preferences.setCustomPreviewContent(isCustomContent);

        if (previewViewer != null) {
            if (isEditMode) {
                setCurrentRawSource(previewViewer.getDocument().get());
            }
            preferences.setCustomFormatterSource(rawCustomSource);
        }
    }

    /**
     * Resets the preview to default values (called from
     * {@code performDefaults()}).
     */
    public void resetToDefaults(String defaultSource, String customSource, int rulerColumn) {
        rawDefaultSource = defaultSource;
        rawCustomSource = customSource;
        previewVerticalRulerSpinner.setSelection(rulerColumn);
        setupPreview(false);
    }

    /**
     * Returns {@code true} when the preview is in edit mode (raw source visible
     * and editable).
     */
    public boolean isEditMode() {
        return isEditMode;
    }

    /**
     * Returns the current custom preview source. If in edit mode with custom
     * content, captures the current document text first.
     */
    public String getCustomSource() {
        if (isEditMode && isCustomContent && previewViewer != null) {
            rawCustomSource = previewViewer.getDocument().get();
        }
        return rawCustomSource;
    }

    /**
     * Sets the custom preview source (e.g. from an imported profile).
     */
    public void setCustomSource(String source) {
        rawCustomSource = source;
    }

    /**
     * Triggers a re-format of the preview with the current settings.
     */
    public void formatPreview() {
        fromatPreviewSource();
    }

    /**
     * Triggers a re-format and scrolls to the given comment line.
     */
    public void formatPreview(String scrollToComment) {
        fromatPreviewSource(scrollToComment);
    }

    /**
     * Sets focus to the preview text widget and resets the cursor line.
     */
    public void setFocus() {
        previewViewer.getTextWidget().setFocus();
        previewCursorLine = 0;
    }

    /**
     * Disposes all resources (painters, colors, fonts) owned by this panel.
     */
    public void dispose() {
        if (whitespacePainter != null) {
            whitespacePainter.deactivate(true);
            whitespacePainter = null;
        }

        if (marginPainter != null) {
            marginPainter.deactivate(true);
            marginPainter = null;
        }

        if (marginColor != null && !marginColor.isDisposed()) {
            marginColor.dispose();
        }

        if (overflowColor != null && !overflowColor.isDisposed()) {
            overflowColor.dispose();
        }

        if (monoFont != null && !monoFont.isDisposed()) {
            monoFont.dispose();
        }

        if (cursorLineColor != null && !cursorLineColor.isDisposed()) {
            cursorLineColor.dispose();
        }
    }

    // ------------------------------------------------------------------
    // UI creation
    // ------------------------------------------------------------------

    private void createPreviewPanel(Composite parent, Preferences preferences) {

        previewGroup = new Group(parent, SWT.NONE);
        previewGroup.setText(Messages.Label_Preview);
        previewGroup.setLayout(new GridLayout(1, false));
        previewGroup.setLayoutData(new GridData(GridData.FILL_BOTH));

        // TextViewer for source preview
        previewViewer = new TextViewer(previewGroup, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
        StyledText styledText = previewViewer.getTextWidget();
        GridData viewerData = new GridData(GridData.FILL_BOTH);
        viewerData.widthHint = 400;
        viewerData.heightHint = 300;
        styledText.setLayoutData(viewerData);

        // Set monospace font
        FontData[] fontData = styledText.getFont().getFontData();
        monoFont = new Font(styledText.getDisplay(), "Courier New", fontData[0].getHeight(), SWT.NORMAL);
        styledText.setFont(monoFont);

        // Set initial content
        IDocument document = new Document(preferences.getDefaultFormatterSource());
        previewViewer.setDocument(document);
        previewViewer.setEditable(false);

        // Highlight cursor line (works regardless of focus)
        cursorLineColor = getCurrentLineHighlightColor(styledText);
        styledText.addCaretListener(event -> {
            if (isUpdatingPreview) {
                return;
            }
            int oldLine = previewCursorLine;
            previewCursorLine = styledText.getLineAtOffset(event.caretOffset);
            if (oldLine != previewCursorLine) {
                styledText.redraw();
            }
        });
        styledText.addLineBackgroundListener(event -> {
            int line = styledText.getLineAtOffset(event.lineOffset);
            if (line == previewCursorLine) {
                event.lineBackground = cursorLineColor;
            }
        });

        // Document listener (placeholder for future use)
        previewDocumentListener = new IDocumentListener() {
            @Override
            public void documentAboutToBeChanged(DocumentEvent event) {
                // placeholder
            }

            @Override
            public void documentChanged(DocumentEvent event) {
                // placeholder
            }
        };
        document.addDocumentListener(previewDocumentListener);

        // Highlight lines exceeding the configured line width
        overflowColor = getErrorTextColor(styledText);
        styledText.addLineStyleListener(new LineStyleListener() {
            @Override
            public void lineGetStyle(LineStyleEvent event) {
                if (previewVerticalRulerSpinner == null) {
                    return;
                }
                int maxWidth = previewVerticalRulerSpinner.getSelection();
                if (maxWidth > 0 && event.lineText.length() > maxWidth) {
                    StyleRange range = new StyleRange();
                    range.start = event.lineOffset;
                    range.length = event.lineText.length();
                    range.foreground = overflowColor;
                    event.styles = new StyleRange[] { range };
                }
            }
        });

        // Margin painter (vertical ruler at line width column)
        marginColor = new Color(styledText.getDisplay(), 192, 192, 192);
        marginPainter = new MarginPainter(previewViewer);
        marginPainter.setMarginRulerColor(marginColor);
        marginPainter.setMarginRulerColumn(60);
        previewViewer.addPainter(marginPainter);

        // Button bar
        Composite buttonBar = new Composite(previewGroup, SWT.NONE);
        buttonBar.setLayout(new GridLayout(6, false));
        buttonBar.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        // "View/Edit raw code" toggle button
        viewEditButton = UIUtils.createToggleButton(buttonBar, Messages.Label_View_Edit_raw_code, Messages.Tooltip_View_Edit_raw_code);
        viewEditButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                isEditMode = viewEditButton.getSelection();
                previewViewer.setEditable(isEditMode);
                if (isEditMode) {
                    // Entering edit mode: show raw/unformatted source
                    previewGroup.setText(Messages.Label_Preview_Edit);
                    previewViewer.getDocument().set(getCurrentRawSource());
                } else {
                    // Leaving edit mode: capture raw source, then format
                    setCurrentRawSource(previewViewer.getDocument().get());
                    fromatPreviewSource();
                    previewGroup.setText(Messages.Label_Preview);
                }
            }
        });

        // "Custom preview contents" toggle button
        customPreviewButton = UIUtils.createToggleButton(buttonBar, Messages.Label_Custom_preview_contents, Messages.Tooltip_Custom_preview_contents);
        customPreviewButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                switchPreviewContent();
            }
        });

        // "Show whitespaces" checkbox
        showWhitespacesCheckbox = UIUtils.createCheckbox(buttonBar, Messages.Label_Show_whitespaces, Messages.Tooltip_Show_whitespaces);
        showWhitespacesCheckbox.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                toggleWhitespaceDisplay();
            }
        });

        // "Preview line width:" label + spinner
        previewVerticalRulerSpinner = UIUtils.createSpinner(buttonBar, Messages.Label_Preview_line_width, Messages.Tooltip_Preview_line_width, 0,
            500);
        previewVerticalRulerSpinner.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                if (rulerUpdateJob != null) {
                    rulerUpdateJob.cancel();
                }
                rulerUpdateJob = new VerticalRulerUpdaterJob();
                rulerUpdateJob.schedule(200);
            }
        });

        // "Reset preview line width" button
        Button resetLineWidthButton = new Button(buttonBar, SWT.PUSH);
        resetLineWidthButton.setToolTipText(Messages.Tooltip_Reset_preview_line_width);
        resetLineWidthButton.setImage(IRpgleFormatterPlugin.getDefault().getImage(IRpgleFormatterPlugin.IMAGE_RESET));
        Point resetSize = resetLineWidthButton.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        GridData resetGridData = new GridData();
        resetGridData.widthHint = reduceButtonSize(resetSize.x);
        resetGridData.heightHint = reduceButtonSize(resetSize.y);
        resetLineWidthButton.setLayoutData(resetGridData);
        resetLineWidthButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                previewVerticalRulerSpinner.setFocus();
                previewVerticalRulerSpinner.setSelection(preferences.getDefaultFormatterPreviewVerticalRulerColumn());
            }
        });
    }

    // ------------------------------------------------------------------
    // Preview formatting
    // ------------------------------------------------------------------

    private void fromatPreviewSource() {
        fromatPreviewSource(null);
    }

    private void fromatPreviewSource(String scrollToComment) {

        if (isDisposed()) {
            return;
        }

        FormatterConfig config = configSupplier.get();
        if (config == null) {
            return;
        }

        try {

            String rawSource = getCurrentRawSource();
            if (rawSource == null || rawSource.isEmpty()) {
                previewViewer.getDocument().set("");
                errorHandler.accept(null);
                return;
            }

            RpgleFormatter previewFormatter = new RpgleFormatter(config);
            previewFormatter.setSourceLength(previewVerticalRulerSpinner.getSelection());

            String[] unformatted = rawSource.split("\n");
            FormattedResult newResult = previewFormatter.format(new TextLinesInput(unformatted), 0);
            String[] lines = newResult.toLines();

            if (previewFormatter.getErrorCount() == 0) {
                errorHandler.accept(null);
            } else {
                // Surface the concrete reason inline (no modal dialog —
                // would be intrusive while the user adjusts the spinner).
                errorHandler.accept(previewFormatter.getErrors().get(0).getMessage());
            }
            String formattedSource = String.join("\n", lines);
            StyledText styledText = previewViewer.getTextWidget();
            int topIndex = styledText.getTopIndex();

            int cursorVisualOffset = -1;
            if (previewCursorLine >= 0 && previewCursorLine < styledText.getLineCount()) {
                cursorVisualOffset = previewCursorLine - topIndex;
            }

            isUpdatingPreview = true;
            try {
                previewViewer.getDocument().set(formattedSource);

                if (scrollToComment != null) {
                    int commentLine = findCommentLine(lines, scrollToComment);
                    if (commentLine >= 0) {
                        previewCursorLine = commentLine;
                        int visibleLines = styledText.getClientArea().height / styledText.getLineHeight();
                        int newTopIndex = Math.max(0, commentLine - visibleLines / 3);
                        styledText.setTopIndex(newTopIndex);
                        if (commentLine < styledText.getLineCount()) {
                            styledText.setCaretOffset(styledText.getOffsetAtLine(commentLine));
                        }
                    }
                } else if (previousFormattedResult != null && previewCursorLine >= 0) {
                    previewCursorLine = previousFormattedResult.mapLineTo(previewCursorLine, newResult);
                    int newTopIndex = Math.max(0, previewCursorLine - cursorVisualOffset);
                    styledText.setTopIndex(newTopIndex);
                    if (previewCursorLine < styledText.getLineCount()) {
                        styledText.setCaretOffset(styledText.getOffsetAtLine(previewCursorLine));
                    }
                } else {
                    styledText.setTopIndex(topIndex);
                }
            } finally {
                isUpdatingPreview = false;
            }

            previousFormattedResult = newResult;

        } catch (Exception e) {
            errorHandler.accept(e.getLocalizedMessage());
        }
    }

    // ------------------------------------------------------------------
    // Helper methods
    // ------------------------------------------------------------------

    private int findCommentLine(String[] lines, String comment) {
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].trim().equals(comment)) {
                return i;
            }
        }
        return -1;
    }

    private void switchPreviewContent() {

        // If in edit mode, save current document text before switching
        if (isEditMode) {
            setCurrentRawSource(previewViewer.getDocument().get());
        }

        // Save status of "customPreviewButton"
        isCustomContent = customPreviewButton.getSelection();

        // Show the appropriate raw source in the document.
        previewViewer.getDocument().set(getCurrentRawSource());

        // Format if not in edit mode
        if (!isEditMode) {
            fromatPreviewSource();
        }
    }

    private void toggleWhitespaceDisplay() {
        if (showWhitespacesCheckbox.getSelection()) {
            whitespacePainter = new WhitespaceCharacterPainter(previewViewer);
            previewViewer.addPainter(whitespacePainter);
        } else {
            if (whitespacePainter != null) {
                whitespacePainter.deactivate(true);
                whitespacePainter = null;
            }
        }
    }

    private void updateMarginColumn(int column) {
        if (marginPainter != null) {
            marginPainter.setMarginRulerColumn(column);
            previewViewer.getTextWidget().redraw();
        }
    }

    private Color getErrorTextColor(StyledText styledText) {
        // Returns "RGB {255, 0, 0}" if set to its default value.
        IThemeRegistry themeRegistry = WorkbenchPlugin.getDefault().getThemeRegistry();
        RGB rgb = themeRegistry.findColor("ERROR_COLOR").getValue();
        return new Color(styledText.getDisplay(), rgb);
    }

    private Color getCurrentLineHighlightColor(StyledText styledText) {
        IThemeRegistry themeRegistry = WorkbenchPlugin.getDefault().getThemeRegistry();
        RGB rgb = themeRegistry.findColor("org.eclipse.ui.editors.currentLineColor").getValue();
        return new Color(styledText.getDisplay(), rgb);
    }

    private String getCurrentRawSource() {
        return isCustomContent ? rawCustomSource : rawDefaultSource;
    }

    private void setCurrentRawSource(String source) {
        if (isCustomContent) {
            rawCustomSource = source;
        } else {
            rawDefaultSource = source;
        }
    }

    /* package */ void setupPreview(boolean customContent) {
        if (previewViewer != null) {
            isCustomContent = customContent;
            isEditMode = false;
            previewViewer.getDocument().set(getCurrentRawSource());
            if (customPreviewButton != null) {
                customPreviewButton.setSelection(isCustomContent);
            }
            if (viewEditButton != null) {
                viewEditButton.setSelection(false);
            }
            previewViewer.setEditable(false);
            previewGroup.setText(Messages.Label_Preview);
            fromatPreviewSource();
        }
    }

    private boolean isDisposed() {
        return previewGroup == null || previewGroup.isDisposed();
    }

    private int reduceButtonSize(int size) {
        size = size * 4 / 5;
        return size;
    }

    // ------------------------------------------------------------------
    // Inner class
    // ------------------------------------------------------------------

    private class VerticalRulerUpdaterJob extends UIJob {

        public VerticalRulerUpdaterJob() {
            super("");
        }

        @Override
        public IStatus runInUIThread(IProgressMonitor arg0) {

            if (!isDisposed()) {
                int column = previewVerticalRulerSpinner.getSelection();
                updateMarginColumn(column);
                if (!isEditMode) {
                    boolean spinnerHadFocus = previewVerticalRulerSpinner.isFocusControl();
                    fromatPreviewSource();
                    if (spinnerHadFocus) {
                        // Move focus away and back to re-select the spinner
                        // text, since forceFocus() is a no-op when the spinner
                        // already has focus.
                        previewViewer.getControl().forceFocus();
                        previewVerticalRulerSpinner.forceFocus();
                    }
                }
            }

            return Status.OK_STATUS;
        }
    }
}
