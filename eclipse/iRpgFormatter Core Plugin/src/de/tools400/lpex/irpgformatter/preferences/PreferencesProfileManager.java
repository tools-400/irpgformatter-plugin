/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.preferences;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Handles import and export of iRPGFormatter preference profiles as XML files.
 * <p>
 * The XML format follows the Java Code Style Formatter profile convention:
 *
 * <pre>
 * &lt;profiles version="1"&gt;
 *   &lt;profile kind="iRPGFormatterProfile" name="iRPGFormatter" version="1"&gt;
 *     &lt;setting id="parameterSpacingStyle" value="BEFORE"/&gt;
 *     &lt;setting id="dataTypes"&gt;
 *       &lt;entry key="CHAR" value="Char"/&gt;
 *     &lt;/setting&gt;
 *   &lt;/profile&gt;
 * &lt;/profiles&gt;
 * </pre>
 */
public final class PreferencesProfileManager {

    private static final String PROFILE_KIND = "iRPGFormatterProfile";
    private static final String PROFILE_NAME = "iRPGFormatter";
    private static final String PROFILE_VERSION = "1";

    private static final String TAG_PROFILES = "profiles";
    private static final String TAG_PROFILE = "profile";
    private static final String TAG_SETTING = "setting";
    private static final String TAG_ENTRY = "entry";

    private static final String ATTR_VERSION = "version";
    private static final String ATTR_KIND = "kind";
    private static final String ATTR_NAME = "name";
    private static final String ATTR_ID = "id";
    private static final String ATTR_VALUE = "value";
    private static final String ATTR_KEY = "key";

    /*
     * IDs of the settings in the profile file. They are part of the file format
     * of exported profiles and must not change.
     */
    private static final String SETTING_EXECUTE_IBM_FORMATTER = "executeIbmFormatter";
    private static final String SETTING_USE_CONST_KEYWORD = "useConstKeyword";
    private static final String SETTING_DELIMITER_BEFORE_PARAMETER = "delimiterBeforeParameter";
    private static final String SETTING_PARAMETER_SPACING_STYLE = "parameterSpacingStyle";
    private static final String SETTING_ALIGN_SUB_FIELDS = "alignSubFields";
    private static final String SETTING_BREAK_BETWEEN_CASE_CHANGE = "breakBetweenCaseChange";
    private static final String SETTING_BREAK_BEFORE_KEYWORD = "breakBeforeKeyword";
    private static final String SETTING_SORT_CONST_VALUE_TO_END = "sortConstValueToEnd";
    private static final String SETTING_REPLACE_PI_NAME = "replacePiName";
    private static final String SETTING_REMOVE_END_PROC_NAME = "removeEndProcName";
    private static final String SETTING_UNINDENT_COMPILER_DIRECTIVES = "unindentCompilerDirectives";
    private static final String SETTING_MAX_NAME_LENGTH = "maxNameLength";
    private static final String SETTING_MIN_NAME_LENGTH = "minNameLength";
    private static final String SETTING_EXECUTE_IRPG_FORMATTER = "executeIrpgFormatter";
    private static final String SETTING_FORMAT_ON_SAVE = "formatOnSave";
    private static final String SETTING_REMOVE_EMPTY_COMMENT_LINES = "removeEmptyCommentLines";
    private static final String SETTING_REMOVE_EMPTY_LINES_BEFORE_DCL_PI = "removeEmptyLinesBeforeDclPi";
    private static final String SETTING_DATA_TYPES = "dataTypes";
    private static final String SETTING_DECLARATION_TYPES = "declarationTypes";
    private static final String SETTING_KEYWORDS = "keywords";
    private static final String SETTING_SPECIAL_WORDS = "specialWords";
    private static final String SETTING_CUSTOM_PREVIEW_SOURCE = "customPreviewSource";

    private PreferencesProfileManager() {
        // Utility class
    }

    /**
     * Exports the given profile data to an XML file.
     *
     * @param filePath the target file path
     * @param data the profile data to export
     * @throws Exception if writing fails
     */
    public static void exportProfile(String filePath, ProfileData data) throws Exception {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();

        // <profiles version="1">
        Element profiles = doc.createElement(TAG_PROFILES);
        profiles.setAttribute(ATTR_VERSION, PROFILE_VERSION);
        doc.appendChild(profiles);

        // <profile kind="iRPGFormatterProfile" name="iRPGFormatter"
        // version="1">
        Element profile = doc.createElement(TAG_PROFILE);
        profile.setAttribute(ATTR_KIND, PROFILE_KIND);
        profile.setAttribute(ATTR_NAME, PROFILE_NAME);
        profile.setAttribute(ATTR_VERSION, PROFILE_VERSION);
        profiles.appendChild(profile);

        // Scalar settings: IBM Settings
        addScalarSetting(doc, profile, SETTING_EXECUTE_IBM_FORMATTER, String.valueOf(data.isExecuteIbmFormatter()));

        // Scalar settings: General Settings
        addScalarSetting(doc, profile, SETTING_USE_CONST_KEYWORD, String.valueOf(data.isUseConstKeyword()));
        addScalarSetting(doc, profile, SETTING_DELIMITER_BEFORE_PARAMETER, String.valueOf(data.isDelimiterBeforeParameter()));
        addScalarSetting(doc, profile, SETTING_PARAMETER_SPACING_STYLE, data.getParameterSpacingStyle());
        addScalarSetting(doc, profile, SETTING_ALIGN_SUB_FIELDS, String.valueOf(data.isAlignSubFields()));
        addScalarSetting(doc, profile, SETTING_BREAK_BETWEEN_CASE_CHANGE, String.valueOf(data.isBreakBetweenCaseChange()));
        addScalarSetting(doc, profile, SETTING_BREAK_BEFORE_KEYWORD, String.valueOf(data.isBreakBeforeKeyword()));
        addScalarSetting(doc, profile, SETTING_SORT_CONST_VALUE_TO_END, String.valueOf(data.isSortConstValueToEnd()));
        addScalarSetting(doc, profile, SETTING_REPLACE_PI_NAME, String.valueOf(data.isReplacePiName()));
        addScalarSetting(doc, profile, SETTING_REMOVE_END_PROC_NAME, String.valueOf(data.isRemoveEndProcName()));
        addScalarSetting(doc, profile, SETTING_UNINDENT_COMPILER_DIRECTIVES, String.valueOf(data.isUnindentCompilerDirectives()));
        addScalarSetting(doc, profile, SETTING_MAX_NAME_LENGTH, String.valueOf(data.getMaxNameLength()));
        addScalarSetting(doc, profile, SETTING_MIN_NAME_LENGTH, String.valueOf(data.getMinNameLength()));
        addScalarSetting(doc, profile, SETTING_EXECUTE_IRPG_FORMATTER, String.valueOf(data.isExecuteIrpgFormatter()));

        // Scalar settings: Save Actions
        addScalarSetting(doc, profile, SETTING_FORMAT_ON_SAVE, String.valueOf(data.isFormatOnSave()));

        // Scalar settings: Experimental Settings
        addScalarSetting(doc, profile, SETTING_REMOVE_EMPTY_COMMENT_LINES, String.valueOf(data.isRemoveEmptyCommentLines()));
        addScalarSetting(doc, profile, SETTING_REMOVE_EMPTY_LINES_BEFORE_DCL_PI, String.valueOf(data.isRemoveEmptyLinesBeforeDclPi()));

        // Map settings
        addMapSetting(doc, profile, SETTING_DATA_TYPES, data.getDataTypes());
        addMapSetting(doc, profile, SETTING_DECLARATION_TYPES, data.getDeclarationTypes());
        addMapSetting(doc, profile, SETTING_KEYWORDS, data.getKeywords());
        addMapSetting(doc, profile, SETTING_SPECIAL_WORDS, data.getSpecialWords());

        // Text content settings (CDATA for multi-line content)
        if (data.getCustomPreviewSource() != null && !data.getCustomPreviewSource().isEmpty()) {
            addTextSetting(doc, profile, SETTING_CUSTOM_PREVIEW_SOURCE, data.getCustomPreviewSource());
        }

        // Write to file with pretty-printing
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(new File(filePath));
        transformer.transform(source, result);
    }

    /**
     * Imports profile data from an XML file.
     * <p>
     * Missing settings leave corresponding fields at their defaults, so partial
     * profiles are supported.
     *
     * @param filePath the source file path
     * @return the imported profile data
     * @throws Exception if reading or parsing fails
     */
    public static ProfileData importProfile(String filePath) throws Exception {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new File(filePath));
        doc.getDocumentElement().normalize();

        // Find <profile kind="iRPGFormatterProfile">
        Element profileElement = findProfileElement(doc);
        if (profileElement == null) {
            throw new IllegalArgumentException("No iRPGFormatterProfile found in file.");
        }

        ProfileData data = new ProfileData();

        // Read all <setting> elements
        NodeList settings = profileElement.getElementsByTagName(TAG_SETTING);
        for (int i = 0; i < settings.getLength(); i++) {
            Element setting = (Element)settings.item(i);

            // getElementsByTagName() returns the <setting> elements of all
            // levels; only the direct children of <profile> are settings.
            if (!setting.getParentNode().equals(profileElement)) {
                continue;
            }

            String id = setting.getAttribute(ATTR_ID);
            String value = setting.getAttribute(ATTR_VALUE);

            switch (id) {
            case SETTING_PARAMETER_SPACING_STYLE:
                data.setParameterSpacingStyle(value);
                break;
            case SETTING_USE_CONST_KEYWORD:
                data.setUseConstKeyword(Boolean.parseBoolean(value));
                break;
            case SETTING_DELIMITER_BEFORE_PARAMETER:
                data.setDelimiterBeforeParameter(Boolean.parseBoolean(value));
                break;
            case SETTING_ALIGN_SUB_FIELDS:
                data.setAlignSubFields(Boolean.parseBoolean(value));
                break;
            case SETTING_BREAK_BETWEEN_CASE_CHANGE:
                data.setBreakBetweenCaseChange(Boolean.parseBoolean(value));
                break;
            case SETTING_BREAK_BEFORE_KEYWORD:
                data.setBreakBeforeKeyword(Boolean.parseBoolean(value));
                break;
            case SETTING_SORT_CONST_VALUE_TO_END:
                data.setSortConstValueToEnd(Boolean.parseBoolean(value));
                break;
            case SETTING_REPLACE_PI_NAME:
                data.setReplacePiName(Boolean.parseBoolean(value));
                break;
            case SETTING_REMOVE_END_PROC_NAME:
                data.setRemoveEndProcName(Boolean.parseBoolean(value));
                break;
            case SETTING_UNINDENT_COMPILER_DIRECTIVES:
                data.setUnindentCompilerDirectives(Boolean.parseBoolean(value));
                break;
            case SETTING_MAX_NAME_LENGTH:
                data.setMaxNameLength(Integer.parseInt(value));
                break;
            case SETTING_MIN_NAME_LENGTH:
                data.setMinNameLength(Integer.parseInt(value));
                break;
            case SETTING_EXECUTE_IBM_FORMATTER:
                data.setExecuteIbmFormatter(Boolean.parseBoolean(value));
                break;
            case SETTING_EXECUTE_IRPG_FORMATTER:
                data.setExecuteIrpgFormatter(Boolean.parseBoolean(value));
                break;
            case SETTING_FORMAT_ON_SAVE:
                data.setFormatOnSave(Boolean.parseBoolean(value));
                break;
            case SETTING_REMOVE_EMPTY_COMMENT_LINES:
                data.setRemoveEmptyCommentLines(Boolean.parseBoolean(value));
                break;
            case SETTING_REMOVE_EMPTY_LINES_BEFORE_DCL_PI:
                data.setRemoveEmptyLinesBeforeDclPi(Boolean.parseBoolean(value));
                break;
            case SETTING_DATA_TYPES:
                data.setDataTypes(readMapEntries(setting));
                break;
            case SETTING_DECLARATION_TYPES:
                data.setDeclarationTypes(readMapEntries(setting));
                break;
            case SETTING_KEYWORDS:
                data.setKeywords(readMapEntries(setting));
                break;
            case SETTING_SPECIAL_WORDS:
                data.setSpecialWords(readMapEntries(setting));
                break;
            case SETTING_CUSTOM_PREVIEW_SOURCE:
                String content = setting.getTextContent().trim();
                content = content.replace("\r\n", "\n").replace("\r", "\n");
                data.setCustomPreviewSource(content);
                break;
            default:
                // Ignore unknown settings for forward compatibility
                break;
            }
        }

        return data;
    }

    private static Element findProfileElement(Document doc) {
        NodeList profiles = doc.getElementsByTagName(TAG_PROFILE);
        for (int i = 0; i < profiles.getLength(); i++) {
            Element element = (Element)profiles.item(i);
            if (PROFILE_KIND.equals(element.getAttribute(ATTR_KIND))) {
                return element;
            }
        }
        return null;
    }

    private static void addScalarSetting(Document doc, Element parent, String id, String value) {
        Element setting = doc.createElement(TAG_SETTING);
        setting.setAttribute(ATTR_ID, id);
        setting.setAttribute(ATTR_VALUE, value);
        parent.appendChild(setting);
    }

    private static void addTextSetting(Document doc, Element parent, String id, String text) {
        Element setting = doc.createElement(TAG_SETTING);
        setting.setAttribute(ATTR_ID, id);
        String normalized = text.trim().replace("\r\n", "\n").replace("\r", "\n");
        setting.appendChild(doc.createCDATASection(normalized));
        parent.appendChild(setting);
    }

    private static void addMapSetting(Document doc, Element parent, String id, Map<String, String> map) {
        Element setting = doc.createElement(TAG_SETTING);
        setting.setAttribute(ATTR_ID, id);
        if (map != null) {
            for (Map.Entry<String, String> e : map.entrySet()) {
                Element entry = doc.createElement(TAG_ENTRY);
                entry.setAttribute(ATTR_KEY, e.getKey());
                entry.setAttribute(ATTR_VALUE, e.getValue());
                setting.appendChild(entry);
            }
        }
        parent.appendChild(setting);
    }

    private static Map<String, String> readMapEntries(Element settingElement) {
        Map<String, String> map = new LinkedHashMap<>();
        NodeList entries = settingElement.getElementsByTagName(TAG_ENTRY);
        for (int i = 0; i < entries.getLength(); i++) {
            Element entry = (Element)entries.item(i);
            map.put(entry.getAttribute(ATTR_KEY), entry.getAttribute(ATTR_VALUE));
        }
        return map;
    }

    /**
     * POJO holding all exportable formatter settings.
     */
    public static class ProfileData {

        private String parameterSpacingStyle = ParameterSpacingStyle.BEFORE.name();
        private boolean useConstKeyword;
        private boolean delimiterBeforeParameter;
        private boolean alignSubFields = true;
        private boolean breakBetweenCaseChange;
        private boolean breakBeforeKeyword;
        private boolean sortConstValueToEnd;
        private boolean replacePiName = true;
        private boolean removeEndProcName = true;
        private boolean unindentCompilerDirectives = true;
        private int maxNameLength = 60;
        private int minNameLength = 20;
        private boolean executeIbmFormatter = true;
        private boolean executeIrpgFormatter = true;
        private boolean formatOnSave;
        private boolean removeEmptyCommentLines;
        private boolean removeEmptyLinesBeforeDclPi;
        private Map<String, String> dataTypes;
        private Map<String, String> declarationTypes;
        private Map<String, String> keywords;
        private Map<String, String> specialWords;
        private String customPreviewSource;

        public String getParameterSpacingStyle() {
            return parameterSpacingStyle;
        }

        public void setParameterSpacingStyle(String parameterSpacingStyle) {
            this.parameterSpacingStyle = parameterSpacingStyle;
        }

        public boolean isUseConstKeyword() {
            return useConstKeyword;
        }

        public void setUseConstKeyword(boolean useConstKeyword) {
            this.useConstKeyword = useConstKeyword;
        }

        public boolean isDelimiterBeforeParameter() {
            return delimiterBeforeParameter;
        }

        public void setDelimiterBeforeParameter(boolean delimiterBeforeParameter) {
            this.delimiterBeforeParameter = delimiterBeforeParameter;
        }

        public boolean isAlignSubFields() {
            return alignSubFields;
        }

        public void setAlignSubFields(boolean alignSubFields) {
            this.alignSubFields = alignSubFields;
        }

        public boolean isBreakBetweenCaseChange() {
            return breakBetweenCaseChange;
        }

        public void setBreakBetweenCaseChange(boolean breakBetweenCaseChange) {
            this.breakBetweenCaseChange = breakBetweenCaseChange;
        }

        public boolean isBreakBeforeKeyword() {
            return breakBeforeKeyword;
        }

        public void setBreakBeforeKeyword(boolean breakBeforeKeyword) {
            this.breakBeforeKeyword = breakBeforeKeyword;
        }

        public boolean isSortConstValueToEnd() {
            return sortConstValueToEnd;
        }

        public void setSortConstValueToEnd(boolean sortConstValueToEnd) {
            this.sortConstValueToEnd = sortConstValueToEnd;
        }

        public boolean isReplacePiName() {
            return replacePiName;
        }

        public void setReplacePiName(boolean replacePiName) {
            this.replacePiName = replacePiName;
        }

        public boolean isRemoveEndProcName() {
            return removeEndProcName;
        }

        public void setRemoveEndProcName(boolean removeEndProcName) {
            this.removeEndProcName = removeEndProcName;
        }

        public boolean isUnindentCompilerDirectives() {
            return unindentCompilerDirectives;
        }

        public void setUnindentCompilerDirectives(boolean unindentCompilerDirectives) {
            this.unindentCompilerDirectives = unindentCompilerDirectives;
        }

        public int getMaxNameLength() {
            return maxNameLength;
        }

        public void setMaxNameLength(int maxNameLength) {
            this.maxNameLength = maxNameLength;
        }

        public int getMinNameLength() {
            return minNameLength;
        }

        public void setMinNameLength(int minNameLength) {
            this.minNameLength = minNameLength;
        }

        public boolean isExecuteIbmFormatter() {
            return executeIbmFormatter;
        }

        public void setExecuteIbmFormatter(boolean executeIbmFormatter) {
            this.executeIbmFormatter = executeIbmFormatter;
        }

        public boolean isExecuteIrpgFormatter() {
            return executeIrpgFormatter;
        }

        public void setExecuteIrpgFormatter(boolean executeIrpgFormatter) {
            this.executeIrpgFormatter = executeIrpgFormatter;
        }

        public boolean isFormatOnSave() {
            return formatOnSave;
        }

        public void setFormatOnSave(boolean formatOnSave) {
            this.formatOnSave = formatOnSave;
        }

        public boolean isRemoveEmptyCommentLines() {
            return removeEmptyCommentLines;
        }

        public void setRemoveEmptyCommentLines(boolean removeEmptyCommentLines) {
            this.removeEmptyCommentLines = removeEmptyCommentLines;
        }

        public boolean isRemoveEmptyLinesBeforeDclPi() {
            return removeEmptyLinesBeforeDclPi;
        }

        public void setRemoveEmptyLinesBeforeDclPi(boolean removeEmptyLinesBeforeDclPi) {
            this.removeEmptyLinesBeforeDclPi = removeEmptyLinesBeforeDclPi;
        }

        public Map<String, String> getDataTypes() {
            return dataTypes;
        }

        public void setDataTypes(Map<String, String> dataTypes) {
            this.dataTypes = dataTypes;
        }

        public Map<String, String> getDeclarationTypes() {
            return declarationTypes;
        }

        public void setDeclarationTypes(Map<String, String> declarationTypes) {
            this.declarationTypes = declarationTypes;
        }

        public Map<String, String> getKeywords() {
            return keywords;
        }

        public void setKeywords(Map<String, String> keywords) {
            this.keywords = keywords;
        }

        public Map<String, String> getSpecialWords() {
            return specialWords;
        }

        public void setSpecialWords(Map<String, String> specialWords) {
            this.specialWords = specialWords;
        }

        public String getCustomPreviewSource() {
            return customPreviewSource;
        }

        public void setCustomPreviewSource(String customPreviewSource) {
            this.customPreviewSource = customPreviewSource;
        }
    }
}
