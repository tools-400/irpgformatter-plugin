/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.preferences;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.ObjIntConsumer;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

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
import org.xml.sax.helpers.DefaultHandler;

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

    private static final String FEATURE_DISALLOW_DOCTYPE_DECL = "http://apache.org/xml/features/disallow-doctype-decl";

    /*
     * The settings of the profile file in the order of the export. Their IDs
     * are part of the file format of exported profiles and must not change.
     */
    private static final List<Setting<String>> SCALAR_SETTINGS = Arrays.asList(
        // IBM Settings
        booleanSetting("executeIbmFormatter", ProfileData::isExecuteIbmFormatter, ProfileData::setExecuteIbmFormatter),

        // General Settings
        booleanSetting("useConstKeyword", ProfileData::isUseConstKeyword, ProfileData::setUseConstKeyword),
        booleanSetting("delimiterBeforeParameter", ProfileData::isDelimiterBeforeParameter, ProfileData::setDelimiterBeforeParameter),
        stringSetting("parameterSpacingStyle", ProfileData::getParameterSpacingStyle, ProfileData::setParameterSpacingStyle),
        booleanSetting("alignSubFields", ProfileData::isAlignSubFields, ProfileData::setAlignSubFields),
        booleanSetting("breakBetweenCaseChange", ProfileData::isBreakBetweenCaseChange, ProfileData::setBreakBetweenCaseChange),
        booleanSetting("breakBeforeKeyword", ProfileData::isBreakBeforeKeyword, ProfileData::setBreakBeforeKeyword),
        booleanSetting("sortConstValueToEnd", ProfileData::isSortConstValueToEnd, ProfileData::setSortConstValueToEnd),
        booleanSetting("replacePiName", ProfileData::isReplacePiName, ProfileData::setReplacePiName),
        booleanSetting("removeEndProcName", ProfileData::isRemoveEndProcName, ProfileData::setRemoveEndProcName),
        booleanSetting("unindentCompilerDirectives", ProfileData::isUnindentCompilerDirectives, ProfileData::setUnindentCompilerDirectives),
        intSetting("maxNameLength", ProfileData::getMaxNameLength, ProfileData::setMaxNameLength),
        intSetting("minNameLength", ProfileData::getMinNameLength, ProfileData::setMinNameLength),
        booleanSetting("executeIrpgFormatter", ProfileData::isExecuteIrpgFormatter, ProfileData::setExecuteIrpgFormatter),

        // Save Actions
        booleanSetting("formatOnSave", ProfileData::isFormatOnSave, ProfileData::setFormatOnSave),

        // Experimental Settings
        booleanSetting("removeEmptyCommentLines", ProfileData::isRemoveEmptyCommentLines, ProfileData::setRemoveEmptyCommentLines),
        booleanSetting("removeEmptyLinesBeforeDclPi", ProfileData::isRemoveEmptyLinesBeforeDclPi, ProfileData::setRemoveEmptyLinesBeforeDclPi));

    private static final List<Setting<Map<String, String>>> MAP_SETTINGS = Arrays.asList(
        new Setting<>("dataTypes", ProfileData::getDataTypes, ProfileData::setDataTypes),
        new Setting<>("declarationTypes", ProfileData::getDeclarationTypes, ProfileData::setDeclarationTypes),
        new Setting<>("keywords", ProfileData::getKeywords, ProfileData::setKeywords),
        new Setting<>("specialWords", ProfileData::getSpecialWords, ProfileData::setSpecialWords));

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

        for (Setting<String> setting : SCALAR_SETTINGS) {
            addScalarSetting(doc, profile, setting.getId(), setting.get(data));
        }

        for (Setting<Map<String, String>> setting : MAP_SETTINGS) {
            addMapSetting(doc, profile, setting.getId(), setting.get(data));
        }

        // Text content settings (CDATA for multi-line content)
        if (data.getCustomPreviewSource() != null && !data.getCustomPreviewSource().isEmpty()) {
            addTextSetting(doc, profile, SETTING_CUSTOM_PREVIEW_SOURCE, data.getCustomPreviewSource());
        }

        writeDocument(doc, filePath);
    }

    /**
     * Writes the document to the file with pretty-printing.
     */
    private static void writeDocument(Document doc, String filePath) throws Exception {

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
     * Imports the settings of an XML file into the given profile data.
     * <p>
     * Settings, that are missing in the file, keep their values in the profile
     * data, so partial profiles are supported. The custom preview source is not
     * required, because it is only exported, if there is one.
     *
     * @param filePath the source file path
     * @param data the profile data, that receives the settings of the file
     * @return <code>true</code>, if the file contains all settings, otherwise
     *         <code>false</code>
     * @throws Exception if reading or parsing fails
     */
    public static boolean importProfile(String filePath, ProfileData data) throws Exception {

        Element profileElement = readProfileElement(filePath);

        Set<String> ids = new HashSet<>();

        // Read all <setting> elements
        NodeList settings = profileElement.getElementsByTagName(TAG_SETTING);
        for (int i = 0; i < settings.getLength(); i++) {
            Element setting = (Element)settings.item(i);

            // getElementsByTagName() returns the <setting> elements of all
            // levels; only the direct children of <profile> are settings.
            if (setting.getParentNode().equals(profileElement)) {
                readSetting(setting, data);
                ids.add(setting.getAttribute(ATTR_ID));
            }
        }

        return containsAll(ids, SCALAR_SETTINGS) && containsAll(ids, MAP_SETTINGS);
    }

    private static boolean containsAll(Set<String> ids, List<? extends Setting<?>> settings) {
        for (Setting<?> setting : settings) {
            if (!ids.contains(setting.getId())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Reads the file and returns the <code>profile</code> element of the
     * iRPGFormatter profile.
     */
    private static Element readProfileElement(String filePath) throws Exception {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        // A profile has no DOCTYPE. Without a DOCTYPE the parser cannot resolve
        // external entities, which would read local files or URLs.
        factory.setFeature(FEATURE_DISALLOW_DOCTYPE_DECL, true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        // DefaultHandler throws fatal errors and ignores the others like the
        // default error handler of the parser, but does not print them on
        // System.err.
        builder.setErrorHandler(new DefaultHandler());
        Document doc = builder.parse(new File(filePath));
        doc.getDocumentElement().normalize();

        // Find <profile kind="iRPGFormatterProfile">
        Element profileElement = findProfileElement(doc);
        if (profileElement == null) {
            throw new IllegalArgumentException("No iRPGFormatterProfile found in file.");
        }

        return profileElement;
    }

    /**
     * Reads a setting into the profile data. Unknown settings are ignored for
     * forward compatibility.
     */
    private static void readSetting(Element setting, ProfileData data) {

        String id = setting.getAttribute(ATTR_ID);

        Setting<String> scalarSetting = findSetting(SCALAR_SETTINGS, id);
        if (scalarSetting != null) {
            scalarSetting.set(data, setting.getAttribute(ATTR_VALUE));
            return;
        }

        Setting<Map<String, String>> mapSetting = findSetting(MAP_SETTINGS, id);
        if (mapSetting != null) {
            mapSetting.set(data, readMapEntries(setting));
            return;
        }

        if (SETTING_CUSTOM_PREVIEW_SOURCE.equals(id)) {
            String content = setting.getTextContent().trim();
            content = content.replace("\r\n", "\n").replace("\r", "\n");
            data.setCustomPreviewSource(content);
        }
    }

    private static <T> Setting<T> findSetting(List<Setting<T>> settings, String id) {
        for (Setting<T> setting : settings) {
            if (setting.getId().equals(id)) {
                return setting;
            }
        }
        return null;
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

    private static Setting<String> booleanSetting(String id, Predicate<ProfileData> getter, BiConsumer<ProfileData, Boolean> setter) {
        return new Setting<>(id, data -> String.valueOf(getter.test(data)), (data, value) -> setter.accept(data, Boolean.parseBoolean(value)));
    }

    private static Setting<String> intSetting(String id, ToIntFunction<ProfileData> getter, ObjIntConsumer<ProfileData> setter) {
        return new Setting<>(id, data -> String.valueOf(getter.applyAsInt(data)), (data, value) -> setter.accept(data, Integer.parseInt(value)));
    }

    private static Setting<String> stringSetting(String id, Function<ProfileData, String> getter, BiConsumer<ProfileData, String> setter) {
        return new Setting<>(id, getter, setter);
    }

    /**
     * A setting of the profile file with its ID and the accessors of its value
     * in the profile data.
     */
    private static final class Setting<T> {

        private final String id;
        private final Function<ProfileData, T> getter;
        private final BiConsumer<ProfileData, T> setter;

        private Setting(String id, Function<ProfileData, T> getter, BiConsumer<ProfileData, T> setter) {
            this.id = id;
            this.getter = getter;
            this.setter = setter;
        }

        public String getId() {
            return id;
        }

        public T get(ProfileData data) {
            return getter.apply(data);
        }

        public void set(ProfileData data, T value) {
            setter.accept(data, value);
        }
    }

    /**
     * POJO holding all exportable formatter settings.
     */
    public static class ProfileData {

        private String parameterSpacingStyle;
        private boolean useConstKeyword;
        private boolean delimiterBeforeParameter;
        private boolean alignSubFields;
        private boolean breakBetweenCaseChange;
        private boolean breakBeforeKeyword;
        private boolean sortConstValueToEnd;
        private boolean replacePiName;
        private boolean removeEndProcName;
        private boolean unindentCompilerDirectives;
        private int maxNameLength;
        private int minNameLength;
        private boolean executeIbmFormatter;
        private boolean executeIrpgFormatter;
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
