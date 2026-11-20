# iRPGFormatter – Developer notes

How the plug-in works and how it is built. For its use, see [USAGE.md](USAGE.md), for its installation [README.md](README.md).

## Setup

* Clone the repository into the RDi workspace. The target platform is the running platform (RDi).
* Projects in `eclipse`:
  * `iRPGFormatter Core Plugin` with `iRPGFormatter Core Feature` is the product.
  * `Lpex Menu Extension Plugin` adds menu entries and actions to the LPEX editors. The core plug-in requires it
    (`de.tools400.lpex.menuextension.plugin`, `[1.1.0,2.0.0)`).
  * `iRPGFormatter iAgentBridge Plugin` with `iRPGFormatter iAgentBridge Feature` is the optional feature, that adds the
    tool `format_rpg` to the MCP server of iAgentBridge, see [iAgentBridge](#iagentbridge). It requires the plug-in
    `de.tools400.iagentbridge.core.plugin` (`[1.0.0.b002,2.0.0)`), whose projects (repository `iagentbridge-plugin`)
    must be in the workspace, too. Both projects are in `build.projects` of `build.properties` and in `site.xml`.
  * `iRPGFormatter Update Site` builds the update site.
  * `iRPGFormatter JUnit` and `Lpex Menu Extension JUnit` are plain Java projects with the unit tests, see [Tests](#tests).
    `iRPGFormatter iAgentBridge JUnit` is a fragment of `iRPGFormatter iAgentBridge Plugin`.
* Java 8 byte code, so that the plug-in runs in RDi 9.6.0.13 and later.
* Launch configurations for German, English, Italian and Dutch: one project per RDi version,
  `eclipse/iRPGFormatter Launches (RDi 9.6)`, `(RDi 9.8)` and `(RDi 9.9)`, because the selected bundles depend on the
  version. Import only the project of the running RDi version, then *Run Configurations* does not show the
  configurations of the other versions. They contain iAgentBridge and `iRPGFormatter iAgentBridge Plugin` and set
  `-Diagentbridge.port=39196`, `39198` or `39199`, so that the server of a test instance does not take the port of the
  RDi it was started from.
* Release: `build/iRPGFormatter Build/build/build.xml` (the same script as the other tools-400 plug-ins, settings in
  `build.properties`). The home page is `build/iRPGFormatter Homepage`.
* Tags: pushing a commit to `main`, that sets a release version (`x.y.z.r`) in `build.version`, tags this commit with
  `vx.y.z.r` (`.github/workflows/tag-release.yml`). The repository `irpgformatter` tags its commit `Release vx.y.z.r.` and
  creates the GitHub release the same way, when the update site ZIP is pushed.

## Architecture

All classes are below `de.tools400.lpex.irpgformatter` in `iRPGFormatter Core Plugin`.

| Package                    | Role                                                                                                   |
| -------------------------- | ------------------------------------------------------------------------------------------------------ |
| (root)                     | `IRpgleFormatterPlugin` (activator, logging), `Messages`, `Preload` (LPEX menu entry and format on save) |
| `actions`                  | `FormatLpexSourceAction` (action `iRPGFormatter.Format`, **Source > iFormat**), `FormatOnSaveListener` |
| `api`                      | Exported API for other plug-ins: `FileFormatter` formats a file like **Source > iFormat**, see [iAgentBridge](#iagentbridge) |
| `handlers`, `handlers/jobs`, `handlers/resolvers` | **Source > iFormat** in the Remote Systems view and in the Project Explorer: the resolvers expand the selection to sources, a job formats them in the background |
| `input`                    | Reading and writing a source: LPEX view (whole or selected lines), source member, stream file, local file |
| `parser`                   | `StatementType`, `StatementIdentifier`, `DirectiveType`, `BlockFinder` (expands a selection to whole blocks) |
| `statement`                | Collecting the lines into statements and nesting them into blocks                                      |
| `tokenizer`                | Tokens of a declaration                                                                                |
| `formatter`                | `RpgleFormatter` and the formatters of the parts of a source                                           |
| `rules`, `rules/statements`, `rules/casing` | The formatting rules                                                                  |
| `preferences`              | Preferences, `FormatterConfig`, profiles                                                               |
| `preferencepages`          | Preference page with preview, proxy page at the root of the preferences, editor of the word lists      |
| `menu`, `expressions`, `utils` | LPEX menu extension, property testers of `plugin.xml`, helpers                                     |

### Formatting a source

1. **Statements.** `ContinuationHandler.collectStatements()` joins the source lines into `CollectedStatement`s. A statement
   keeps its original lines, the text without comments, the comments and the compiler directives between its lines, and
   whether a literal is continued. In an `EXEC SQL` statement, SQL comments and delimited identifiers are respected.
   `StatementIdentifier` classifies a statement by `StatementType`, which also tells blocks, their end and branches.
2. **Nesting.** A stack of open blocks gives each statement its indent level and the number of enclosing `SELECT` groups.
   * `ConditionalGroup` handles `/IF` groups: each branch starts with the blocks that are open at `/IF`, after `/ENDIF` the
     blocks of the end of the first branch are open. If the branches end with different blocks, the `/ENDIF` refers to its
     `/IF` and `RpgleFormatter` reports a warning.
   * Lines after an `/EOF` outside of an `/IF` group are marked and not formatted.
   * Calculation blocks, that are still open at the end of the block that encloses them (e.g. `IF` without `ENDIF` at
     `END-PROC`) or at the end of the source, are closed.
   * Declaration blocks, that an end statement leaves open (e.g. `DCL-DS` without `END-DS` at `END-PROC`), and declaration
     blocks, that are still open at the end of the source, are errors. The end statement closes the open blocks together
     with its own block, so that one error does not cause errors in the following statements. An end statement without its
     block is ignored, because a selection may start within the block. All errors are thrown together as
     `BlockStructureException`, ordered by line number, and the source is not formatted.
   * A comment line gets the level of the statement that follows it.
3. **Formatting.** `RpgleFormatter.format()` goes over the statements. The lines in front of a selection only tell the
   nesting. **Format declarations** and **Format calculations** decide, which statements are formatted; a comment line
   follows the statement after it.
   * Declarations are tokenized and rebuilt by `FormatterUtils` with the rules. A declaration with `/IF` ... `/ENDIF`
     between its lines is formatted section by section.
   * Calculations are formatted by `CalculationStatementFormatter`: embedded SQL, outer parentheses, casing, indentation.
   * If a statement cannot be formatted (`LineOverflowException`, `RpgleFormatterException`), it keeps its original lines
     and becomes a `FormatError`. Unexpected exceptions are logged with their stack trace.
4. **Writing.** The output writes the `FormattedResult` statement by statement from the bottom to the top, so that inserted
   and deleted lines do not move the statements above.

### Problems view

The package `problems` and the view `views.FormatterProblemsView` show the problems of the formatted sources.

* `FormatterProblems` is the model: per `IProblemSource` the `FormatterProblem`s (severity, line, message), in the order of
  the sources. Formatting a source replaces its problems (`replace`), an empty list removes the source. The batch jobs change
  it in their thread, so it is synchronized; the view refreshes with `asyncExec`. `getSummary()` holds the result of the
  last batch job, the view shows it as its content description.
* `ProblemFactory` builds the problems: `BlockStructureException` (lines unchanged, the source was not written), the errors
  and warnings of `RpgleFormatter` (lines mapped with `FormattedResult.getFormattedLineNumber()`, because the formatted lines
  were written), a single error without line and the message of `RpgleFormatter.validateInput()` (not free format,
  unsupported source type) as an information (`Severity.INFORMATION`), because such a source is skipped, not wrong.
* `ProblemSeverityFilter` hides the severities, that are switched off in the view menu **Show Severity**; the view keeps
  the choice in its dialog settings (`hide.<SEVERITY>`) and shows the number of hidden entries in its content description.
* `IProblemSource` opens a source: `MemberSource` (`QSYSEditableRemoteSourceFileMember.open`, connects an offline
  connection), `RemoteStreamFileSource` (`SystemEditableRemoteFile.open`), `WorkspaceFileSource` (`IDE.openEditor`),
  `LocalFileSource` (`IDE.openEditorOnFileStore`) and `EditorSource` (the LPEX view of the format action and of
  **Format on save**). `EditorSources` maps an LPEX view to the source of its editor, so that the editor and the batch jobs
  use the same key: the local copy of Remote Systems knows its `QSYSEditableRemoteSourceFileMember` or
  `SystemEditableRemoteFile` (`SystemIFileProperties.getRemoteFileObject()`), other files are `WorkspaceFileSource` or, for
  an `IURIEditorInput`, `LocalFileSource`. Only if the source cannot be determined, the editor is an `EditorSource`, whose
  problems are removed, when the LPEX view is disposed.
* `ProblemNavigator.goTo()` opens a problem with a marker like the Problems view (`IDE.openEditor(page, marker)`), which
  opens the file of the marker and goes to it, also for the local copy of a member. Without a marker it opens the source
  (`IProblemSource.open()`) and puts the cursor on the line (`locate line` and `screenShow view` in LPEX, `selectAndReveal`
  in other text editors). `QSYSEditableRemoteSourceFileMember.open()` returns, before the editor has loaded the member, so
  the line could not be set right after it.
* `ProblemMarkers` (registered by the activator) keeps the markers in line with `FormatterProblems`: a job replaces the
  markers of the type `de.tools400.lpex.irpgformatter.core.plugin.problem` (`plugin.xml`: problem and text marker, not
  persistent like the problem markers of RDi) of all files, that have or had problems. The file of a source is
  `IProblemSource.getMarkerFile()` (`AbstractProblemSource`): the workspace file, the local copy of the editor
  (`EditorSources`) or the local copy of a member, that the batch job sets after the download
  (`IRpgleInput.getLocalResource()`). A stream file of the IFS, that a batch job reads without a local copy, has none.
* The annotation model of the LPEX editor does not take over the markers, that a file has, when the editor opens it, so it
  neither shows them nor goes to them. `EditorMarkerRefresher` (registered in `Preload`) changes an attribute of these
  markers, when an LPEX editor opens, at once and again after 200 ms; the model adds a changed marker, that it does not
  know (`AbstractMarkerAnnotationModel.modifyMarkerAnnotation`). Deleting and creating them again would break
  `IDE.openEditor(page, marker)`, which goes to the original marker.
* The format action and **Format on save** show the view with `VIEW_VISIBLE` (the editor keeps the focus) and select the
  first problem of the source (`FormatterProblemsView.show(IProblemSource)`), because a new source is added at the end of
  the list. The batch handlers show it with `VIEW_ACTIVATE`. `plugin.xml` stacks the view on the Problems view.

### Embedded SQL

`EmbeddedSqlFormatter` formats `EXEC SQL` statements with the SQL formatter of IBM i Access Client Solutions.
`AcsSqlFormatter` is the only class, that refers to its classes (`com.ibm.ibmi.sql.formatter.impl`). RDi 9.6 provides them in
the bundle `com.ibm.ibmi.sql.parser.lpg`, RDi 9.9 in `com.ibm.ibmi.sql.parser` and RDi 9.8 in both bundles, with the newer
formatter in `com.ibm.ibmi.sql.parser`. `MANIFEST.MF` requires both bundles with `resolution:=optional` and
`com.ibm.ibmi.sql.parser` first. `Require-Bundle` searches the bundles in this order, so RDi 9.8 uses the newer formatter;
keep the order. If neither bundle is installed, the `LinkageError` is caught and the statement is only indented.

The formatter of RDi 9.6 prints its parser statistics and the syntax tree to `System.out`. In RDi this output is not
visible, in the JUnit output it is.

The formatted statement is used only, if it has the same literals and delimited identifiers and differs otherwise only in
blanks and casing (`isSameStatement()`). Otherwise, and if the SQL formatter reports a syntax error, a literal is continued,
a comment follows the semicolon or a line exceeds the end column, the statement is only indented.

### Format on save

LPEX has no general pre-save hook in RDi 9.8: `LpexAbstractTextEditor.performSave()` does not call
`LpexViewListener.saving()`. `Preload` therefore registers an `IExecutionListener` for the commands
`org.eclipse.ui.file.save` and `org.eclipse.ui.file.saveAll`. Saving an editor, when it is closed, is not covered.

### Format jobs

RDi's `QSYSEditableRemoteSourceFileMember.download()` ends the monitor, that it gets. `AbstractFormatJob` therefore gives
each source a `SubMonitor` of its own.

### iAgentBridge

The package `de.tools400.lpex.irpgformatter.api` is the only exported package of the core plug-in:

* `FileFormatter.formatWorkspaceFile(IFile)` runs `FormatStreamFileJob`, `formatLocalFile(File)` runs
  `FormatJavaIoStreamFileJob`, each with one file: the job is scheduled and joined, so the methods block and must not be
  called in the UI thread. The counts of `IFormatPostRun.postRun()` tell, whether the file was formatted; the problems
  are read from `FormatterProblems` with the key of the `IProblemSource` of the job. `changed` compares the bytes of the
  file before and after the job. Like the menu, the job replaces the problems in the view and the markers; unlike the
  handlers, `FileFormatter` shows no dialog and does not activate the view.
* `FileFormatter.isLocalCopyOfRemoteFile(IFile)` is `SourcePreferences.isLocalCopyOfRemoteFile()`.
* `FileFormatResult` (status `FORMATTED`, `SKIPPED`, `FAILED`, `CANCELED`, `changed`, project settings, problems) and
  `FileFormatProblem` (severity, line, message) have no Eclipse types; `FileFormatResult.create()` derives the status
  from the counts of the job: a file, that was not formatted and has only information messages, is skipped.
* The two methods have different names, because a test, that calls an overloaded method, of which one variant takes an
  Eclipse type, does not compile in the JUnit project in RDi.

`iRPGFormatter iAgentBridge Plugin` contributes `FormatRpgTool` to the extension point
`de.tools400.iagentbridge.core.plugin.tools` (see `EXTENDING.md` of iAgentBridge). The tool maps each path to a file of
an open project (`IWorkspaceRoot.findFilesForLocationURI()`) or else to a local file, refuses local copies of Remote
Systems and files of editors with unsaved changes (checked in the UI thread), refreshes the workspace file and calls
`FileFormatter`. `FormatRpgReport` builds the JSON of the result without Eclipse types.

## Rules

* `rules/statements`: rules that change the tokens of a declaration (`IStatementRule`) and rules that work on the list of
  all statements before formatting (`IStatementListRule`, e.g. `RemoveEmptyCommentLinesRule`,
  `RemoveEmptyLinesBeforeDclPiRule`), and `ConditionParenthesesRule` for calculations.
* `rules/casing`: the casing of keywords, data types, declaration types, special words, directives and compile time arrays
  by the word lists, and `CalculationCasingRule` for calculations.
* A new rule is a `*Rule` class of its own, integrated via `IStatementRule` or `IStatementListRule`.

### Empty comment lines

`RemoveEmptyCommentLinesRule` works in three steps on the list of statements:

1. Every empty `//` line is marked for suppression (it is written as an empty line).
2. At a structural marker - a separator line such as `//---` or the ILEDoc delimiter `///` - it scans backward and restores
   the suppressed lines, as long as the line in front is a comment.
3. From the code line found in step 2 it scans forward and suppresses the leading empty `//` lines again, up to the first
   non-empty comment.

Lines within an ILEDoc block and within `@formatter:off` regions are not touched. `RemoveEmptyLinesBeforeDclPiRule` runs
after it, so that the lines suppressed by it are removed in front of a `dcl-pi` as well.

### Special words in calculations

`CalculationCasingRule` scans a statement and remembers across its lines, whether the last token was an operand. An asterisk
after an operand (a name, a number, a literal, a built-in function or `)`) is a multiplication. A special word starts at the
start of the statement, after an operator or `(`, after the operation code and its extender (only, if the first word is in
the list of operation codes), after `AND`, `OR`, `NOT`, `TO`, `DOWNTO` and `BY` and after another special word.

### Outer parentheses of conditions

`ConditionParenthesesRule` leaves a statement unchanged, if the condition does not start on the line of the operation code,
if an opening parenthesis ends its line or a closing parenthesis starts its line, if a literal is continued, or if a
parenthesis follows the operation code without a blank: then it is an operation extender, see [Verified facts](#verified-facts).

### Casing

The casing rules of the calculations change a line only, if it equals the original line except for the casing and has the
same literals (`CalculationCasingRule.isSameExceptCasing()`).

## Preferences

### IBM settings

iRPGFormatter reads these preferences of RDi (plug-in `com.ibm.etools.iseries.edit`) and links their pages:

| Key                                                                                  | Page (ID)                                                                            | Setting                                        |
| ------------------------------------------------------------------------------------ | ------------------------------------------------------------------------------------ | ---------------------------------------------- |
| `RPGLE.FORMATTING.start`, `.end`, `.length`                                          | Formatter (`com.ibm.etools.iseries.edit.preferences.formatter.ISeriesFormatterPreferencePageRPGILE`) | Start column, end column, indentation |
| `RPGLE.FORMATTING.select`, `.multiple`                                               | Formatter                                                                            | Indent of WHEN/OTHER, relative indentation of continued lines |
| `SQLRPGLE.FORMATTING.base`, `.onComma`, `.onAndOr`, `SQLRPGLE.INDENT.S1_Blanks`      | Free-form SQL Formatting (`com.ibm.etools.iseries.edit.preferences.sql.ParserPreferencePageSQLRPGLEFormatting`) | Formatting, line breaks, SQL indent |
| `SQLRPGLE.UPPERCASING.base`, `.base.value`, `.keywords`, `.identifiers`              | Free-form SQL Formatting                                                             | Automatic uppercasing                          |
| `com.ibm.etools.iseries.edit.preferences.parser.ilerpg.enter.autoclosecontrol.autoclosecontrolvalue` | Key Behavior (`com.ibm.etools.iseries.edit.ui.preferences.ParserPreferencePageRPGILEKeyBehavior.RPGILE`) | Style of **Automatic closure of control block** = keyword casing style |

The order of `SqlBreakStyle` and `SqlCasingStyle` matches the values of the IBM preferences.

### Own settings

* The keys of the preferences, the IDs of the profile settings and the IDs in `plugin.xml` are persisted. Never rename them.
* **Format declarations** has the key `executeIrpgFormatter` of the former **Execute iRPG formatter**.
* **Format calculations** (`formatCalculations`) replaces **Execute IBM formatter** (`executeIbmFormatter`): as long as
  `formatCalculations` has its default value and `executeIbmFormatter` was switched off, the value is `OFF`.
  `setCalculationFormatter()` resets `executeIbmFormatter` to its default. A profile with `executeIbmFormatter` is converted
  the same way (`true` becomes `IRPG_FORMATTER`, `false` becomes `OFF`).
* `FormatterConfig` is a snapshot of the preferences for one run. The preference page builds one from its controls for the
  preview.

### Project specific settings

* `Preferences` has one instance per preference store: `getInstance()` for the workspace and `forProject()` for the
  `ProjectScope` of a project (file `.settings/de.tools400.lpex.irpgformatter.core.plugin.prefs`). The IBM settings always
  come from the workspace.
* A project has no word lists of its own: the getters of the lists return those of the workspace, the setters throw an
  `IllegalStateException`. Different lists per project would only be half a feature, because the keyword casing style is
  an IBM setting of the workspace; `StatementIdentifier` and `FormatCompilerDirectiveRule` tokenize with the lists of the
  workspace; and a changed list is stored as one line, that does not get the new entries of later versions.
* The key `projectSpecificSettings` switches the settings of a project on. Then all settings come from the project, a key,
  that the project does not contain, has the default value. Keys are never mixed with the workspace: `ScopedPreferenceStore`
  removes a value, that equals the default value, so that a search in the workspace would return the value of the
  workspace instead. `removeProjectSpecificSettings()` resets all keys of the project.
* `SourcePreferences` finds the preferences of a source for `FormatLpexSourceAction`, `FormatOnSaveListener` and
  `FormatStreamFileJob` (each source of a batch gets a formatter of its own). Local copies of Remote Systems (properties of
  `SystemIFileProperties`, like `EditorSources`) and files of `FormatJavaIoStreamFileJob` use the workspace.
* The property page is `IRPGFormatterPreferencePage` as `IWorkbenchPropertyPage`, without the tabs of the word lists
  (`hasWordLists()`). Without project specific settings it shows the settings of the workspace and disables them with
  `ControlEnableState`. The state of the preview is always stored in the workspace.

### Word lists

The defaults are in `resources`: `data_types`, `declaration_types`, `keywords`, `special_words`, `operation_codes` and
`built_in_functions.properties`. The key is the word in upper case, the value its spelling in UpperCamelCase. The lists
correspond to the ILE RPG Reference for IBM i 7.6 as of 2026-10-28.

* The list of special words is used for the declarations and for the calculations. A word added for the calculations is
  formatted in the declarations, too.
* Once a user changed a list, the preference store holds the whole list. New entries of the defaults do not reach it.

## Translations

`messages*.properties` (English, German, Italian, Dutch) in package `de.tools400.lpex.irpgformatter` and
`plugin*.properties`:

* ISO-8859-1 with direct umlauts, no `\uXXXX` escapes. Eclipse reads `.properties` as ISO-8859-1, whatever the project
  encoding says; a file written in UTF-8 shows garbled umlauts on Java 8 (RDi 9.6).
* Every key in all four files with the same placeholders.
* An apostrophe in a message with placeholders is written as `''`.

## Tests

* `iRPGFormatter JUnit`: JUnit 4, *Run As > JUnit Test* in RDi. The tests extend `AbstractTestCase`, which switches
  `PreferenceStoreProvider` to its test mode; they do not need OSGi.
* Outside of RDi (`org.junit.runner.JUnitCore`), the classpath needs the classes and `resources` of the core plug-in and the
  bundles of RDi. In RDi 9.8 and 9.9, `com.ibm.ibmi.sql.parser` and `lpg.runtime` contain nested jars
  (`runtime/sql.tools.jar`, `runtime/sqlparser.jar`, `lpgruntime.jar`), which have to be extracted; in RDi 9.6 the
  formatter is in `com.ibm.ibmi.sql.parser.lpg`, which has no nested jars. Without the formatter `EmbeddedSqlTest` fails,
  because `AcsSqlFormatter` falls back (`LinkageError`).
* With the bundles of RDi 9.6, run the tests with its Java 8 (`C:\IBM\RDi_096\jdk`). It does not read `@argfiles`, so put
  the classpath into the `Class-Path` of a jar, that contains only a manifest.
* `FileFormatResultTest` tests the status of `FileFormatResult`. `iRPGFormatter iAgentBridge JUnit` (a fragment, run
  with *Run As > JUnit Test*) tests the JSON of `format_rpg` in `FormatRpgReportTest`; outside of RDi it needs Gson and
  the classes of `iAgentBridge Core Plugin` on the classpath.
* Code coverage: see `eclipse/iRPGFormatter JUnit/CODE_COVERAGE.md`.

## Verified facts

* RDi 9.6, 2026-11-20: `format_rpg` writes the same bytes as **Source > iFormat**: 32 sources of Peek/400 and XMLGEN in a
  project with project specific settings (menu in the Project Explorer) and 32 local files outside of the workspace
  (menu in the Remote Systems view), with the same status and problems. LPEX shows a workspace file, that the tool
  formatted while it was open without unsaved changes, formatted at once; for a file outside of the workspace, opened
  with **File > Open File...**, RDi asks, when the editor is activated, whether to replace its content.
* RDi 9.6, 2026-11-17: the LPEX editor shows the markers of a file in its ruler and goes to them only, if they are created
  or changed, while it is open. A double-click on the marker of the local copy of a member in the Problems view opens the
  member for editing; saving uploads it.
* 2026-11-17: the SQL formatter of IBM i Access Client Solutions (`com.ibm.ibmi.sql.formatter.impl`) is in the bundle
  `com.ibm.ibmi.sql.parser.lpg` 1.0.3 in RDi 9.6.0.12, in `com.ibm.ibmi.sql.parser` 9.8.3 and `com.ibm.ibmi.sql.parser.lpg`
  1.0.3 in RDi 9.8 and in `com.ibm.ibmi.sql.parser` 9.9.0 in RDi 9.9. The classes and methods, that `AcsSqlFormatter`
  uses, are the same in all versions (`javap`).
* IBM i 7.5, 2026-10-28: a continued line, that starts with `/count;`, compiles as a division (severity 00). Within a
  statement, only `/IF`, `/ELSEIF`, `/ELSE` and `/ENDIF` are taken as directives.
* IBM i 7.5, 2026-10-28: `if(found) and x;` is rejected with RNF5048 (operation extender not valid). A parenthesis directly
  after the operation code is an operation extender.
* RDi 9.8: `LpexView.elementText()` returns `null` behind the last element.
* LPEX 4.6.12, 4.23.5 and 4.31.5 (RDi 9.6, 9.8 and 9.9), 2026-11-20: show elements, e.g. the messages of the syntax check
  of RDi, are elements, but no lines of the document. `queryInt("lines")` does not count them, `elementOfLine()` returns
  the element of a line and `lineOfElement()` of a show element the line in front of it. The editor input and output and
  `BlockFinder` therefore work with lines (`LpexViewUtils.getLineText()`), the selection is converted to lines.
