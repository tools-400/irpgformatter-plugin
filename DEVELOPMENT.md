# iRPGFormatter – Developer notes

How the plug-in works and how it is built. For its use, see [README.md](README.md).

## Setup

* Clone the repository into the RDi workspace. The target platform is the running platform (RDi).
* Projects in `eclipse`:
  * `iRpgFormatter Core Plugin` with `iRpgFormatter Core Feature` is the product.
  * `Lpex Menu Extension Plugin` adds menu entries and actions to the LPEX editors. The core plug-in requires it
    (`de.tools400.lpex.menuextension.plugin`, `[1.1.0,2.0.0)`).
  * `iRpgFormatter Update Site` builds the update site.
  * `iRpgFormatter JUnit` and `Lpex Menu Extension JUnit` are plain Java projects with the unit tests, see [Tests](#tests).
* Java 8 byte code, so that the plug-in runs in RDi 9.6.0.13 and later.
* Launch configurations for German, English, Italian and Dutch: `eclipse/iRpgFormatter Core Plugin/launches`.
* Release: `build/iRpgFormatter Build/build/build.xml` (the same script as the other tools-400 plug-ins, settings in
  `build.properties`). The home page is `build/iRpgFormatter Homepage`.

## Architecture

All classes are below `de.tools400.lpex.irpgformatter` in `iRpgFormatter Core Plugin`.

| Package                    | Role                                                                                                   |
| -------------------------- | ------------------------------------------------------------------------------------------------------ |
| (root)                     | `IRpgleFormatterPlugin` (activator, logging), `Messages`, `Preload` (LPEX menu entry and format on save) |
| `actions`                  | `FormatLpexSourceAction` (action `iRPGFormatter.Format`, **Source > iFormat**), `FormatOnSaveListener` |
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
     `END-PROC`) or at the end of the source, are closed. A declaration block, that is still open at the end, is an error.
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

### Embedded SQL

`EmbeddedSqlFormatter` formats `EXEC SQL` statements with the SQL formatter of IBM i Access Client Solutions.
`AcsSqlFormatter` is the only class, that refers to the bundle `com.ibm.ibmi.sql.parser`, which `MANIFEST.MF` requires with
`resolution:=optional`. RDi 9.6 does not provide the formatter to plug-ins (checked with RDi 9.6.0.12); the `LinkageError` is
caught and the statement is only indented.

The formatted statement is used only, if it has the same literals and delimited identifiers and differs otherwise only in
blanks and casing (`isSameStatement()`). Otherwise, and if the SQL formatter reports a syntax error, a literal is continued,
a comment follows the semicolon or a line exceeds the end column, the statement is only indented.

### Format on save

LPEX has no general pre-save hook in RDi 9.8: `LpexAbstractTextEditor.performSave()` does not call
`LpexViewListener.saving()`. `Preload` therefore registers an `IExecutionListener` for the commands
`org.eclipse.ui.file.save` and `org.eclipse.ui.file.saveAll`. Saving an editor, when it is closed, is not covered.

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

* `iRpgFormatter JUnit`: JUnit 4, *Run As > JUnit Test* in RDi. The tests extend `AbstractTestCase`, which switches
  `PreferenceStoreProvider` to its test mode; they do not need OSGi.
* Outside of RDi (`org.junit.runner.JUnitCore`), the classpath needs the classes and `resources` of the core plug-in and the
  bundles of RDi. `com.ibm.ibmi.sql.parser` and `lpg.runtime` contain nested jars (`runtime/sql.tools.jar`,
  `runtime/sqlparser.jar`, `lpgruntime.jar`), which have to be extracted. Without them `EmbeddedSqlTest` fails, because
  `AcsSqlFormatter` falls back as in RDi 9.6.
* Code coverage: see `eclipse/iRpgFormatter JUnit/CODE_COVERAGE.md`.

## Verified facts

* RDi 9.6.0.12 does not provide the SQL formatter of IBM i Access Client Solutions to plug-ins.
* IBM i 7.5, 2026-10-28: a continued line, that starts with `/count;`, compiles as a division (severity 00). Within a
  statement, only `/IF`, `/ELSEIF`, `/ELSE` and `/ENDIF` are taken as directives.
* IBM i 7.5, 2026-10-28: `if(found) and x;` is rejected with RNF5048 (operation extender not valid). A parenthesis directly
  after the operation code is an operation extender.
* RDi 9.8: `LpexView.elementText()` returns `null` behind the last element.
