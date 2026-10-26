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
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.MarginPainter;
import org.eclipse.jface.text.WhitespaceCharacterPainter;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.LineNumberRulerColumn;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
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
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.ui.themes.IThemeManager;

import de.tools400.lpex.irpgformatter.IRpgleFormatterPlugin;
import de.tools400.lpex.irpgformatter.Messages;
import de.tools400.lpex.irpgformatter.formatter.FormattedResult;
import de.tools400.lpex.irpgformatter.formatter.RpgleFormatter;
import de.tools400.lpex.irpgformatter.formatter.RpgleFormatterException;
import de.tools400.lpex.irpgformatter.input.TextLinesInput;
import de.tools400.lpex.irpgformatter.preferences.FormatterConfig;
import de.tools400.lpex.irpgformatter.preferences.PreferenceStoreProvider;
import de.tools400.lpex.irpgformatter.preferences.Preferences;
import de.tools400.lpex.irpgformatter.utils.ColorUtils;
import de.tools400.lpex.irpgformatter.utils.WidgetFactory;

/**
 * Encapsulates the preview panel UI and formatting logic used in the
 * iRPGFormatter preference page.
 */
public class PreviewPanel implements IPropertyChangeListener {

    private final Supplier<FormatterConfig> configSupplier;
    private final Consumer<String> errorHandler;

    /* UI controls */
    private Group previewGroup;
    private SourceViewer previewViewer;
    private LineNumberRulerColumn lineNumberRuler;
    private Button viewEditButton;
    private Button customPreviewButton;
    private Button showWhitespacesCheckbox;
    private Spinner previewVerticalRulerSpinner;

    /* Painters */
    private WhitespaceCharacterPainter whitespacePainter;
    private MarginPainter marginPainter;

    /* State */
    private boolean isEditMode;
    private boolean isCustomContent;
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

        registerPropertyChangeListeners();
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
     * Lets a settings control of the preference page update the preview. When
     * the control changes, the preview is formatted again (unless the panel is
     * in edit mode) and scrolls to the comment of the preview source, that
     * shows the setting.
     *
     * @param control checkbox, combo or spinner
     * @param previewComment comment line of the preview source, e.g.
     *        <code>// Break before keyword.</code>
     */
    public void addPreviewTrigger(Control control, String previewComment) {

        if (control instanceof Spinner) {
            ((Spinner)control).addModifyListener(new ModifyListener() {
                @Override
                public void modifyText(ModifyEvent e) {
                    formatPreviewUnlessEditMode(previewComment);
                }
            });
            return;
        }

        SelectionListener listener = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                formatPreviewUnlessEditMode(previewComment);
            }
        };

        if (control instanceof Button) {
            ((Button)control).addSelectionListener(listener);
        } else if (control instanceof Combo) {
            ((Combo)control).addSelectionListener(listener);
        } else {
            throw new IllegalArgumentException("Unsupported control: " + control.getClass().getName());
        }
    }

    private void formatPreviewUnlessEditMode(String previewComment) {
        if (!isEditMode) {
            formatPreviewSource(previewComment);
        }
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
        formatPreviewSource();
    }

    /**
     * Triggers a re-format and scrolls to the given comment line.
     */
    public void formatPreview(String scrollToComment) {
        formatPreviewSource(scrollToComment);
    }

    /**
     * Sets focus to the preview text widget and resets the cursor line.
     */
    public void setFocus() {
        previewViewer.getTextWidget().setFocus();
        previewCursorLine = 0;
    }

    /**
     * Removes the listeners and deactivates the painters of this panel. Fonts
     * and colors belong to the registries of JFace.
     */
    public void dispose() {

        disposePropertyChangeListeners();

        if (whitespacePainter != null) {
            whitespacePainter.deactivate(true);
            whitespacePainter = null;
        }

        if (marginPainter != null) {
            marginPainter.deactivate(true);
            marginPainter = null;
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

        createSourceViewer(preferences);
        installCursorLineHighlighting();
        installLineWidthHighlighting();
        installMarginPainter(preferences);
        createButtonBar(preferences);
    }

    /**
     * Creates the source viewer with the line number ruler on the left.
     */
    private void createSourceViewer(Preferences preferences) {

        CompositeRuler ruler = new CompositeRuler();
        lineNumberRuler = new LineNumberRulerColumn() {
            @Override
            public int getWidth() {
                // Add a few pixels of padding so line numbers don't sit
                // flush against the ruler/text boundary.
                return super.getWidth() + 6;
            }
        };
        ruler.addDecorator(0, lineNumberRuler);

        previewViewer = new SourceViewer(previewGroup, ruler, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
        GridData viewerData = new GridData(GridData.FILL_BOTH);
        viewerData.widthHint = 400;
        viewerData.heightHint = 300;
        previewViewer.getControl().setLayoutData(viewerData);

        // Text font of the editors (also on the ruler so digit height matches
        // code lines). The font belongs to the font registry of JFace.
        Font textFont = JFaceResources.getTextFont();
        previewViewer.getTextWidget().setFont(textFont);
        lineNumberRuler.setFont(textFont);
        lineNumberRuler.setForeground(ColorUtils.getLineNumberColor());

        // Set initial content
        IDocument document = new Document(preferences.getDefaultFormatterSource());
        previewViewer.setDocument(document);
        previewViewer.setEditable(false);
    }

    /**
     * Highlights the line of the cursor, regardless of the focus.
     */
    private void installCursorLineHighlighting() {

        StyledText styledText = previewViewer.getTextWidget();
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
                event.lineBackground = ColorUtils.getCurrentLineHighlightColor();
            }
        });
    }

    /**
     * Highlights the lines, that exceed the configured line width.
     */
    private void installLineWidthHighlighting() {

        previewViewer.getTextWidget().addLineStyleListener(new LineStyleListener() {
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
                    range.foreground = ColorUtils.getErrorTextColor();
                    event.styles = new StyleRange[] { range };
                }
            }
        });
    }

    /**
     * Draws the vertical ruler at the line width column.
     */
    private void installMarginPainter(Preferences preferences) {

        marginPainter = new MarginPainter(previewViewer);
        marginPainter.setMarginRulerColor(ColorUtils.getPrintMarginColor());
        marginPainter.setMarginRulerColumn(preferences.getDefaultFormatterPreviewVerticalRulerColumn());
        previewViewer.addPainter(marginPainter);
    }

    private void createButtonBar(Preferences preferences) {

        Composite buttonBar = new Composite(previewGroup, SWT.NONE);
        buttonBar.setLayout(new GridLayout(6, false));
        buttonBar.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        createViewEditButton(buttonBar);
        createCustomPreviewButton(buttonBar);
        createShowWhitespacesCheckbox(buttonBar);
        createLineWidthSpinner(buttonBar);
        createResetLineWidthButton(buttonBar, preferences);
    }

    /**
     * Creates the toggle button 'View/Edit raw code'.
     */
    private void createViewEditButton(Composite buttonBar) {

        viewEditButton = WidgetFactory.createToggleButton(buttonBar, Messages.Label_View_Edit_raw_code, Messages.Tooltip_View_Edit_raw_code);
        viewEditButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                isEditMode = viewEditButton.getSelection();
                previewViewer.setEditable(isEditMode);
                if (isEditMode) {
                    // Entering edit mode: show raw/unformatted source
                    previewGroup.setText(Messages.Label_Preview_Edit);
                    previewViewer.getDocument().set(getCurrentRawSource());
                    // Clear error message
                    errorHandler.accept(null);
                } else {
                    // Leaving edit mode: capture raw source, then format
                    setCurrentRawSource(previewViewer.getDocument().get());
                    formatPreviewSource();
                    previewGroup.setText(Messages.Label_Preview);
                }
            }
        });
    }

    /**
     * Creates the toggle button 'Custom preview contents'.
     */
    private void createCustomPreviewButton(Composite buttonBar) {

        customPreviewButton = WidgetFactory.createToggleButton(buttonBar, Messages.Label_Custom_preview_contents,
            Messages.Tooltip_Custom_preview_contents);
        customPreviewButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                switchPreviewContent();
            }
        });
    }

    /**
     * Creates the checkbox 'Show whitespaces'.
     */
    private void createShowWhitespacesCheckbox(Composite buttonBar) {

        showWhitespacesCheckbox = WidgetFactory.createCheckbox(buttonBar, Messages.Label_Show_whitespaces, Messages.Tooltip_Show_whitespaces);
        showWhitespacesCheckbox.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                toggleWhitespaceDisplay();
            }
        });
    }

    /**
     * Creates the label 'Preview line width:' and its spinner. The vertical
     * ruler follows the spinner with a delay.
     */
    private void createLineWidthSpinner(Composite buttonBar) {

        previewVerticalRulerSpinner = WidgetFactory.createSpinner(buttonBar, Messages.Label_Preview_line_width, Messages.Tooltip_Preview_line_width,
            0, 500);
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
    }

    /**
     * Creates the button, that resets the preview line width to its default.
     */
    private void createResetLineWidthButton(Composite buttonBar, Preferences preferences) {

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

    private void formatPreviewSource() {
        formatPreviewSource(null);
    }

    private void formatPreviewSource(String scrollToComment) {

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

            FormattedResult newResult = formatSource(rawSource, config);
            showFormattedResult(newResult, scrollToComment);

            previousFormattedResult = newResult;

        } catch (Exception e) {
            errorHandler.accept(e.getLocalizedMessage());
        }
    }

    /**
     * Formats the raw source with the line width of the preview and shows the
     * first error or clears the error message.
     */
    private FormattedResult formatSource(String rawSource, FormatterConfig config) throws RpgleFormatterException {

        int defaultIndent = Preferences.getInstance().getStartColumn() - 1;

        RpgleFormatter previewFormatter = new RpgleFormatter(config);
        previewFormatter.setSourceLength(previewVerticalRulerSpinner.getSelection());

        String[] unformatted = rawSource.split("\n");
        FormattedResult newResult = previewFormatter.format(new TextLinesInput(unformatted), defaultIndent);

        if (previewFormatter.getErrorCount() == 0) {
            errorHandler.accept(null);
        } else {
            // Surface the concrete reason inline (no modal dialog —
            // would be intrusive while the user adjusts the spinner).
            errorHandler.accept(previewFormatter.getErrors().get(0).getMessage());
        }

        return newResult;
    }

    /**
     * Shows the formatted result in the preview. Scrolls to the given comment
     * line, if there is one, otherwise keeps the cursor on its statement or
     * keeps the scroll position.
     */
    private void showFormattedResult(FormattedResult newResult, String scrollToComment) {

        String[] lines = newResult.toLines();
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
                int commentLine = PreviewComments.findLine(lines, scrollToComment);
                if (commentLine >= 0) {
                    int visibleLines = styledText.getClientArea().height / styledText.getLineHeight();
                    moveCursorTo(styledText, commentLine, commentLine - visibleLines / 3);
                }
            } else if (previousFormattedResult != null && previewCursorLine >= 0) {
                int cursorLine = previousFormattedResult.mapLineTo(previewCursorLine, newResult);
                moveCursorTo(styledText, cursorLine, cursorLine - cursorVisualOffset);
            } else {
                styledText.setTopIndex(topIndex);
            }
        } finally {
            isUpdatingPreview = false;
        }
    }

    /**
     * Moves the cursor to the given line and makes the given line the first
     * visible line of the preview.
     *
     * @param styledText the text widget of the preview
     * @param line the line of the cursor
     * @param topIndex the first visible line; values below 0 show the first
     *        line
     */
    private void moveCursorTo(StyledText styledText, int line, int topIndex) {

        previewCursorLine = line;
        styledText.setTopIndex(Math.max(0, topIndex));
        if (line < styledText.getLineCount()) {
            styledText.setCaretOffset(styledText.getOffsetAtLine(line));
        }
    }

    // ------------------------------------------------------------------
    // Helper methods
    // ------------------------------------------------------------------

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
            formatPreviewSource();
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
            formatPreviewSource();
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
    // Property change listener - Color Settings
    // ------------------------------------------------------------------

    private void updateColorSettings() {

        marginPainter.setMarginRulerColor(ColorUtils.getPrintMarginColor());
        lineNumberRuler.setForeground(ColorUtils.getLineNumberColor());
        formatPreview();
        previewViewer.refresh();
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {

        String property = event.getProperty();

        // ----------------------------------------------------
        // General -> Appearance -> Colors and Fonts -> Basics
        // ----------------------------------------------------

        if (ColorUtils.ERROR_COLOR.equals(property)) {
            updateColorSettings();
        }

        // ----------------------------------------------------
        // General -> Editors -> Text Editors
        // ----------------------------------------------------

        if (ColorUtils.CURRENT_LINE_COLOR.equals(property) || ColorUtils.LINE_NUMBER_COLOR.equals(property)
            || ColorUtils.PRINT_MARGIN_COLOR.equals(property) || ColorUtils.LINE_NUMBER_RULER_ENABLED.equals(property)) {
            updateColorSettings();
        }

        // ----------------------------------------------------
        // General -> Appearance
        // ----------------------------------------------------

        if (IThemeManager.CHANGE_CURRENT_THEME.equals(property)) {
            updateColorSettings();
        }

        // ----------------------------------------------------
        // Remote Systems -> Remote Systems LPEX Editor ->
        // IBM i Parsers -> ILE RPG -> Formatter
        // ----------------------------------------------------

        if (property.startsWith("RPGLE.FORMATTING.start") || property.startsWith("RPGLE.FORMATTING.end")) {
            formatPreview();
        }

        // ----------------------------------------------------
        // Remote Systems -> Remote Systems LPEX Editor ->
        // IBM i Parsers -> ILE RPG -> Key Behavior
        // ----------------------------------------------------

        if (property.startsWith("com.ibm.etools.iseries.edit.preferences.parser.ilerpg.enter.autoclosecontrol")) {
            formatPreview();
        }
    }

    private void registerPropertyChangeListeners() {

        JFaceResources.getColorRegistry().addListener(this);

        EditorsUI.getPreferenceStore().addPropertyChangeListener(this);

        PlatformUI.getWorkbench().getThemeManager().addPropertyChangeListener(this);

        PreferenceStoreProvider.getIbmPreferenceStore().addPropertyChangeListener(this);
    }

    private void disposePropertyChangeListeners() {

        JFaceResources.getColorRegistry().removeListener(this);

        EditorsUI.getPreferenceStore().removePropertyChangeListener(this);

        PlatformUI.getWorkbench().getThemeManager().removePropertyChangeListener(this);

        PreferenceStoreProvider.getIbmPreferenceStore().removePropertyChangeListener(this);
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
                    formatPreviewSource();
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
