# iRPGFormatter Plug-in

## Description

Plug-in for formatting RPGLE **\*\*free** source code in RDi (Rational Developer for i).

Further information about the iRpgFormatter are available on the [iRpgFormatter Web Site](https://tools-400.github.io/irpgformatter/).

## Limitations

The formatter does not have an Abstract Syntax Tree (AST), which usually is required for formatting source code. But it has basic
knowledge about functions, keywords, special words, data types and the blocks of the calculations.

Most of the time it produces the expected result. But there are edge cases where the result is unexpected. Unexpected result can
occur if the structure of the source code is not completely visible in the source member, but hidden in includes or manipulated
by conditional compiling.

For example:

```text
/if defined(DEFINE_PROCPTR)
dcl-pr SrvPgmLayout_new ind extproc(g_pLayout_new);
/else
dcl-pr SrvPgmLayout_new ind extproc('SrvPgmLayout_new');
/endif
  i_pSuper   like(LOG4RPG_pLayout_t) const;
  io_pHandle pointer;
  i_propList like(LOG4RPG_pPropertyList_t) const;
  i_prefix   like(LOG4RPG_prefix_t) const;
end-pr;
```

The example above has two `dcl-pr` statements in a row. The formatter handles them like the compiler: each branch of an `/IF`
group starts with the blocks that are open at `/IF`. If the branches end with different blocks, the formatter shows a warning,
because the indentation of the following lines may be wrong. See [Conditional Compilation](#conditional-compilation).

## Usage

### Formatting Source Code

1. Open an RPGLE source member in the LPEX editor
2. Right-click to open the context menu
3. Select **Source > iFormat**

### Formatting Selected Lines

You can format only a portion of the source:

1. Select the lines you want to format
2. Right-click and select **Source > iFormat**
3. Only the selected lines will be formatted

**Note:** If your selection starts or ends inside a block (DCL-DS, DCL-PR, DCL-PI, DCL-PROC, DCL-ENUM), the selection is automatically expanded to include the complete block.
The lines in front of the selection tell the formatter the blocks that enclose it, so the selection is indented like the whole source.

### Keyboard Shortcut

iRPGFormatter does not assign a key by default. To format with a key, map a **User Key Action** to the action `iRPGFormatter.Format`:

1. Open **Window > Preferences > LPEX Editor > User Key Actions**
2. Enter a key, e.g. `c-a-f` for Ctrl+Alt+F, and the action `iRPGFormatter.Format`
3. Click **Set** and **Apply and Close**

iRPGFormatter adds its action and the **iFormat** menu item to each LPEX editor. It does not store them in the LPEX preferences.
Therefore `iRPGFormatter.Format` is not listed on the LPEX preference page **User Actions**.

Your key action is kept when you restart RDi. While RDi is closed, it is stored in the preferences of iRPGFormatter instead of
the LPEX preferences. That way it is removed together with iRPGFormatter, when you uninstall the plug-in.

## Supported Source Types

- **RPGLE** (ILE RPG free-format)
- **SQLRPGLE** (ILE RPG with embedded SQL, see [Embedded SQL](#embedded-sql))

The formatter requires **\*\*free** format. Fixed-format sources are not supported.

---

## Preferences

Access preferences via **Window > Preferences > iRpgFormatter**. This page at the root of the preferences leads to the actual page at
**Remote Systems > Remote Systems LPEX Editor > IBM i Parsers > ILE RPG > iRpgFormatter**, next to the IBM formatter settings.

Consider mapping a **User Key Action** to the action `iRPGFormatter.Format`, see [Keyboard Shortcut](#keyboard-shortcut).

### IBM Settings

The IBM settings are shown read-only. The links of the group lead to the RDi preference pages, where they are configured.

| Setting                                              | Preferences                                  | Description                                                                                         |
| ---------------------------------------------------- | -------------------------------------------- | --------------------------------------------------------------------------------------------------- |
| **Start column**                                     | IBM i Parsers > ILE RPG > Formatter          | The leftmost column for reformatted free-form source                                                |
| **End column**                                       | IBM i Parsers > ILE RPG > Formatter          | The rightmost column for reformatted free-form source                                               |
| **Indentation**                                      | IBM i Parsers > ILE RPG > Formatter          | The number of blanks to indent nested levels                                                        |
| **Indent WHEN/OTHER within SELECT statement**        | IBM i Parsers > ILE RPG > Formatter          | The extra blanks of WHEN and OTHER within a SELECT group                                            |
| **Preserve relative indentation of continued lines** | IBM i Parsers > ILE RPG > Formatter          | Keep the indentation of continued lines relative to the first line instead of lining them up        |
| **Formatting**                                       | IBM i Parsers > ILE RPG > Free-form SQL Formatting | Format embedded SQL statements, see [Embedded SQL](#embedded-sql)                             |
| **Automatic uppercasing**                            | IBM i Parsers > ILE RPG > Free-form SQL Formatting | Change the casing of embedded SQL statements                                                  |
| **Keyword casing style**                             | IBM i Parsers > ILE RPG > Key Behavior       | Specifies the style of keywords, special words, data types, operation codes and built-in functions |

### General Settings

| Setting                            | Default          | Description                                                                                  |
| ---------------------------------- | ---------------- | -------------------------------------------------------------------------------------------- |
| **Use const() keyword**            | Off              | Wrap constant values in `const()` for DCL-C statements                                       |
| **Put delimiter before parameter** | Off              | Place colon delimiter before parameter instead of after                                      |
| **Parameter spacing**              | Before parameter | Spacing around colon between keyword parameters                                              |
| **Align sub-fields/parameters**    | On               | Align keywords within blocks at a common column                                              |
| **Break name on case change**      | Off              | Break long names at camelCase boundaries                                                     |
| **Break before keyword**           | Off              | Break line before keyword when parameters don't fit                                          |
| **Sort const/value to end**        | Off              | Move `const()`/`value()` keyword to the end of the statement                                 |
| **Replace dcl-pi name with \*N**   | On               | Inside a DCL-PROC, rewrite the DCL-PI name to `*N`; when off, restore the procedure name     |
| **Remove end-proc name**           | On               | Remove the optional procedure name from END-PROC; when off, ensure it matches the DCL-PROC   |
| **Unindent compiler directives**   | On               | Force compiler directives to column 1; when off, keep their indentation                      |
| **Maximum name length**            | 60               | Max characters for a name or literal before it is split with `...`                           |
| **Minimum name length**            | 20               | Min characters that must remain on a line when splitting long names                          |
| **Format declarations**            | On               | Format the control options, the declarations and the compiler directives                     |
| **Format calculations**            | iRPGFormatter    | Select who formats the calculations, the comment lines in front of them and the embedded SQL |

**Format calculations** replaces the former setting **Execute IBM formatter**:

| Value             | Calculations, comment lines and embedded SQL                                                                     |
| ----------------- | ---------------------------------------------------------------------------------------------------------------- |
| **Off**           | Left unchanged                                                                                                   |
| **IBM formatter** | Formatted by the IBM formatter, before iRPGFormatter formats the declarations. iRPGFormatter leaves them unchanged |
| **iRPGFormatter** | Formatted by iRPGFormatter, see [Calculations](#calculations) (default)                                          |

- **Format declarations** is the former setting **Execute iRPG formatter**. If both settings are off, nothing is formatted.
- If **Execute IBM formatter** was switched off, **Format calculations** is **Off**, otherwise it is **iRPGFormatter**.
- A comment line is formatted like the statement that follows it.
- The IBM formatter needs an editor. It does not run, when you format members from a Remote Systems filter.
  With **IBM formatter**, the calculations stay unchanged there.
- RDi cannot format a source when it is saved. With **Format on save**, **Format declarations** off and
  **Format calculations** set to **IBM formatter**, iRPGFormatter runs only the IBM formatter when you save.

### Experimental Settings

These settings implement new formatting rules that are still being evaluated. They are disabled by default and may change in future releases.

| Setting                              | Default | Description                                                                                                          |
| ------------------------------------ | ------- | -------------------------------------------------------------------------------------------------------------------- |
| **Remove empty comment lines**       | Off     | Remove standalone `//` lines that serve no structural purpose (see [Comment Block Handling])                         |
| **Remove empty lines before dcl-pi** | Off     | Remove blank lines and empty `//` comments immediately before a `dcl-pi` statement (see [Empty Lines Before DCL-PI]) |
| **Outer parentheses of conditions**  | Off     | Add or remove the parentheses around the condition of IF, ELSEIF, WHEN, DOW and DOU (see [Outer Parentheses of Conditions](#outer-parentheses-of-conditions)) |
| **Apply keyword casing to operation codes and built-in functions** | Off | Format the operation codes and built-in functions of the calculations in the keyword casing style (see [Casing of Operation Codes and Built-in Functions](#casing-of-operation-codes-and-built-in-functions)) |

### Save Actions

| Setting                         | Default          | Description                                                              |
| ------------------------------- | ---------------- | ------------------------------------------------------------------------ |
| **Format on save**              | Off              | Automatically format when saving the source member                       |

### Settings

| Setting                         | Default          | Description                                                              |
| ------------------------------- | ---------------- | ------------------------------------------------------------------------ |
| **Data Types**                  | (built-in list)  | Custom data type casing mappings                                         |
| **Declaration Types**           | (built-in list)  | Custom declaration type casing mappings                                  |
| **Keywords**                    | (built-in list)  | Custom keyword casing mappings                                           |
| **Special Words**               | (built-in list)  | Custom keyword parameter casing mappings (e.g., \*NOPASS, \*OMIT)        |
| **Operation Codes**             | (built-in list)  | Custom operation code casing mappings (e.g., SETLL, EVAL-CORR)           |
| **Built-in Functions**          | (built-in list)  | Custom built-in function casing mappings (e.g., %SUBDT)                  |

The built-in lists correspond to the ILE RPG Reference for IBM i 7.6 as of 2026-10-28.

IBM writes operation codes and built-in functions in upper case only. Their UpperCamelCase spelling, such as `SetLL` or `%SubDt`,
is a choice of iRPGFormatter and can be changed in the lists.

### Source Preview

The preview pane shows a live preview of the formatted source based on the current settings. Changes in the settings are immediately reflected in the preview, allowing
you to see the impact of your adjustments before applying them to actual source files.

With **Format calculations** set to **IBM formatter**, the preview shows the calculations as iRPGFormatter would format them,
because the IBM formatter needs an editor and cannot run in the preview. iRPGFormatter indents them by the same IBM settings,
so the result is nearly always the same. It differs for inactive `/IF` branches, which the IBM formatter leaves unchanged,
and for sources with structural errors, such as a missing `ENDIF`, which the IBM formatter does not format at all.

The colors of the preview panel are derived from the current RDi theme, ensuring a consistent look and feel with the rest of the IDE:

| Color                        | Eclipse settings                                                        |
| ---------------------------- | ----------------------------------------------------------------------- |
| Error text color             | General -> Appearance -> Colors and Fonts -> Basics -> Error text color |
| Current line highlight color | General -> Editors -> Text Editors -> Current line highlight            |
| Vertical ruler color         | General -> Editors -> Text Editors -> Print margin                      |
| Line number ruler            | General -> Editors -> Text Editors -> Line number foreground            |

### Import / Export

Formatter settings can be exported to and imported from XML files. This allows sharing a common formatting profile across a team or backing up settings between workspaces.

- **Export...** -- Saves all current settings (general settings, experimental settings, save actions, data types, declaration types, keywords, special words, operation codes, built-in functions and custom preview source) to an XML file.
- **Import...** -- Loads settings from an XML file and applies them to the preference page. The imported values are shown in the UI but not persisted until you click **Apply** or **OK**.

The IBM settings are not part of the profile, because they belong to RDi.

Partial profiles are supported: if the XML file does not contain all settings, only the included settings are applied; the remaining settings keep their current values.
This is also the case for profiles of former versions, which do not contain the settings added later. A message tells that the
profile does not contain all settings. The former setting **Execute IBM formatter** of such a profile is converted to **Format calculations**.

---

## Formatting Rules

### What Gets Formatted

The formatter processes the following RPGLE statement types:

| Statement Type      | Examples                                             | Formatted?                               |
| ------------------- | ---------------------------------------------------- | ---------------------------------------- |
| Control options     | `ctl-opt`                                            | Yes                                      |
| Declarations        | `dcl-s`, `dcl-f`, `dcl-c`                            | Yes                                      |
| Block openers       | `dcl-ds`, `dcl-pr`, `dcl-pi`, `dcl-proc`, `dcl-enum` | Yes                                      |
| Subfields           | Fields/parameters within blocks                      | Yes                                      |
| Block closers       | `end-ds`, `end-pr`, `end-pi`, `end-proc`, `end-enum` | Yes                                      |
| Compiler directives | `/copy`, `/include`, `/if`, `/define`                | Yes (keyword casing and indentation)     |
| Free directive      | `**free`                                             | Yes (casing only)                        |
| Comments            | `// ...`                                             | Indented (see [Comments](#comments))     |
| Calculations        | `if`, `for`, `return`, etc.                          | Indented (see [Calculations](#calculations)) |
| Embedded SQL        | `exec sql`                                           | Yes (see [Embedded SQL](#embedded-sql))  |
| Blank lines         | (empty lines)                                        | No (preserved)                           |

**Format declarations** controls all statements except the calculations, **Format calculations** controls the calculations,
the comment lines in front of them and the embedded SQL, see [General Settings](#general-settings).

### Keyword Casing

All keywords, data types, declaration types and special words (e.g. `*NOPASS`) are formatted according to the configured casing style:

| Style            | Example `DCL-DS` | Example `*NOPASS` | Example `Char` |
| ---------------- | ---------------- | ----------------- | -------------- |
| All Uppercase    | `DCL-DS`         | `*NOPASS`         | `CHAR`         |
| UpperCamelCase   | `Dcl-Ds`         | `*NoPass`         | `Char`         |
| First Char Upper | `Dcl-ds`         | `*Nopass`         | `Char`         |
| lowerCamelCase   | `dcl-Ds`         | `*noPass`         | `char`         |
| All Lowercase    | `dcl-ds`         | `*nopass`         | `char`         |

The default casing style is **All Lowercase**.

Optionally the operation codes and built-in functions of the calculations are formatted in the same style, see
[Casing of Operation Codes and Built-in Functions](#casing-of-operation-codes-and-built-in-functions).

The keyword casing style is derived from the RDi setting at **Window > Preferences > IBM i > Parsers > ILE RPG > Content assist > Auto-close control structure** (the "endxx" setting). This ensures consistency between auto-completed code and formatted code.

### Indentation

- Indentation uses **spaces** (never tabs).
- The indent size is derived from the RDi setting at **Window > Preferences > IBM i > Parsers > ILE RPG > Content assist > Indentation**.
- Subfields and parameters within blocks (`dcl-ds`, `dcl-pr`, `dcl-pi`, `dcl-enum`) are indented one level deeper than their enclosing block.
- Calculations are indented by the nesting of their blocks, see [Calculations](#calculations).

**Example:**

```rpgle
dcl-ds customer qualified;
  custId char(10);
  name   char(50);
end-ds;
```

### Calculations

Calculations are indented by the nesting of their blocks: `IF`, `DOW`, `DOU`, `FOR`, `FOR-EACH`, `SELECT`, `MONITOR` and `BEGSR`.
Apart from the experimental rules, their content is not changed.

- Branches, such as `ELSE`, `ELSEIF`, `WHEN`, `OTHER`, `ON-ERROR`, `ON-EXCP` and `ON-EXIT`, are placed on the level of the
  statement that starts the block.
- `WHEN` and `OTHER` get the extra blanks of the IBM setting **Indent WHEN/OTHER within SELECT statement**.
- Continued lines keep their indentation relative to the first line or are lined up one level deeper, see the IBM setting
  **Preserve relative indentation of continued lines**. Lines, whose leading blanks belong to a literal continued with `-`, are never moved.
- If a line would exceed the end column, the statement is left unchanged and reported as an error.

**Example:**

```rpgle
dcl-proc p;
  if a = 1;
    x = 1;
  elseif a = 2;
    x = 2;
  else;
    select;
    when b = 1;
      x = 3;
    endsl;
  endif;
end-proc;
```

### Comments

A comment line gets the indentation of the statement that follows it. Blank lines and compiler directives in between are skipped.
A comment line at the end of the source or of the selection keeps the indentation of the block that encloses it.

Comments between the lines of a declaration are moved in front of the declaration and get its indentation.

If an indented comment line would exceed the end column, it is left unchanged and reported as an error.

### Embedded SQL

`EXEC SQL` statements are formatted with the SQL formatter of IBM i Access Client Solutions, which RDi uses, too. They follow the
RDi settings at **Remote Systems > Remote Systems LPEX Editor > IBM i Parsers > ILE RPG > Free-form SQL Formatting**:

| RDi setting                                         | Result                                                                                                 |
| --------------------------------------------------- | ------------------------------------------------------------------------------------------------------ |
| **Formatting** on                                   | `EXEC SQL` on a line of its own, the SQL statement formatted one SQL indent deeper with the line breaks of **New line: On comma** and **On AND/OR keywords** |
| **Formatting** off, **Automatic uppercasing** on    | Only the casing is changed, the lines stay as they are                                                 |
| Both off (default of RDi)                           | The statement is only indented                                                                         |

**Example (Formatting on):**

```rpgle
exec sql
  select name,
         city
    into :name,
         :city
    from customer
    where id = :id;
```

The statement is only indented, if the SQL formatter reports a syntax error, if a literal is continued on the next line, if a comment
follows the semicolon or if a line would exceed the end column. iRPGFormatter makes sure that the SQL formatter changed nothing but
blanks and the casing outside of literals and delimited identifiers.

Differences to RDi:

- The line length is the end column of iRPGFormatter. RDi uses the end column of the RPG formatter as well, but no limit for stream files with `*MAX`.
- A comment in front of the first word of the SQL statement is indented like the SQL statement.
- iRPGFormatter always uses the SQL formatter of IBM i Access Client Solutions, even if **Use the Enhanced SQL Formatter** is off.
- RDi 9.6 does not provide this SQL formatter to plug-ins (checked with RDi 9.6.0.12). There embedded SQL is only indented.

### Subfield Alignment

When **Align sub-fields** is enabled (default: on), the keywords following field names within a block are aligned at a common column. The alignment column is computed from the longest field name in the block.

**With alignment enabled:**

```rpgle
dcl-ds customer qualified;
  custId       char(10);
  customerName char(50);
end-ds;
```

**With alignment disabled:**

```rpgle
dcl-ds customer qualified;
  custId char(10);
  customerName char(50);
end-ds;
```

### Line Width and Wrapping

The formatter wraps statements that exceed the maximum line width. The maximum is determined by the RDi setting at **Window > Preferences > IBM i > Parsers > ILE RPG > Content assist > Right margin** (default: `*MAX`).

When a statement is too long, the formatter:

1. Breaks the line after the last token that fits
2. Continues on the next line with a sub-indent (one indent level)
3. Repeats until all tokens fit

**Example:**

```rpgle
// Before (one long line):
dcl-pr myPrototype extproc('MODULE_myPrototype') opdesc;

// After (wrapped at max width):
dcl-pr myPrototype
  extproc('MODULE_myPrototype') opdesc;
```

### Name and Literal Breaking

When a single name or literal is too long to fit on one line:

- **Names** are broken with an ellipsis (`...`) as continuation character.
  If **Break between case change** is enabled, the break is placed at camelCase boundaries.
- **Literals** are broken with `+` (skips leading whitespace on continuation) or `-` (preserves leading whitespace).

**Example:**

```rpgle
dcl-c LONG_MESSAGE 'This is a very long constant message that '+
  'continues to the next line';
```

### Parameter Spacing

The spacing around colons between keyword parameters is configurable:

| Style                | Example                    |
| -------------------- | -------------------------- |
| No space             | `:*omit:`                  |
| Before parameter     | `: *omit:` (default)       |
| After parameter      | `:*omit :`                 |
| Before and after     | `: *omit :`                |

### Constants (`dcl-c`)

- When **Use const() keyword** is enabled, the value is wrapped in `const()`: `dcl-c MY_CONST const(42);`
- When **Sort const value to end** is enabled, the `const()` keyword is moved after all other keywords.

### Procedure Interface (`dcl-pi`) Naming

When a DCL-PI sits inside a DCL-PROC, RPGLE allows its name to be either repeated from the procedure or replaced by the placeholder `*N`. The formatter rewrites the DCL-PI name based on **Replace dcl-pi name with \*N**:

| Setting | DCL-PI inside DCL-PROC contains... | Result                                |
| ------- | ---------------------------------- | ------------------------------------- |
| On      | a name (e.g. `myProc`)             | rewritten to `*N`                     |
| On      | `*N`                               | left as `*N`                          |
| Off     | `*N`                               | restored to the parent procedure name |
| Off     | a name                             | left unchanged                        |

A standalone DCL-PI without a DCL-PROC parent is never touched.

**Example (setting on, default):**

```rpgle
dcl-proc myProc;
  dcl-pi *n;
    parm1 char(10);
  end-pi;
end-proc;
```

### End-Procedure Name

The optional name on `end-proc` is governed by **Remove end-proc name**:

| Setting | END-PROC contains...         | Result                                                         |
| ------- | ---------------------------- | -------------------------------------------------------------- |
| On      | a name                       | name removed (`end-proc;`)                                     |
| On      | no name                      | left as `end-proc;`                                            |
| Off     | no name                      | the matching procedure name is inserted                        |
| Off     | a divergent name (e.g. typo) | overwritten with the procedure name from the matching DCL-PROC |
| Off     | the matching name            | left unchanged                                                 |

### Compiler Directives

Compiler directives (`/copy`, `/include`, `/if`, `/define`, `/eof`, etc.) are formatted with keyword casing applied to the directive keyword. The directive parameters are preserved as-is.

By default (**Unindent compiler directives** = on) directives are forced to column 1 regardless of the surrounding block indent. When the option is disabled, directives keep the indentation level of their enclosing block.

#### Conditional Compilation

- Each branch of an `/IF` group starts with the blocks that are open at `/IF`. After `/ENDIF` the formatter continues with the
  blocks that are open at the end of the first branch. If the branches end with different blocks, the formatter shows a warning,
  because the indentation of the following lines may be wrong.
- `/IF`, `/ELSEIF`, `/ELSE` and `/ENDIF` may appear between the lines of control options, file and data declarations and
  procedures. They split the declaration into sections, which are formatted on lines of their own. The directives are indented
  like continued lines, the sections within a branch one level deeper. If a directive is enclosed by the parameters of a
  keyword, or if the declaration contains comments as well, the declaration is left unchanged.
- The lines after an `/EOF` directive outside of an `/IF` group are ignored by the compiler and left unchanged.

### Formatter Directives

You can temporarily disable formatting for specific code sections using formatter directives:

```rpgle
// @formatter:off
dcl-s   myVar1   char(10);     // not formatted
dcl-s   myVar2   char(20);     // not formatted
// @formatter:on
dcl-s myVar3 char(30);         // formatted normally
```

- `// @formatter:off` disables formatting for all subsequent statements
- `// @formatter:on` re-enables formatting
- The directives are case-insensitive (`// @FORMATTER:OFF` works too)
- Extra whitespace is allowed: `//   @formatter:off`
- If `@formatter:off` is used without a corresponding `@formatter:on`, the rest of the file remains unformatted
- The directive lines themselves are always preserved as-is

### Experimental Formatting Rules

The rules in this section correspond to the [Experimental Settings] in the preferences.

#### Comment Block Handling

> **Note:** The following rules are implemented in `RemoveEmptyCommentLinesRule`
> (package `rules/statements`). When adding new comment-formatting behaviour,
> implement a corresponding `*Rule` class and integrate it via `IStatementListRule`.

When **Remove empty comment lines** is enabled, standalone `//` lines (empty
comments) are removed unless they serve a structural purpose.

**Structural markers** protect the comment block they belong to:

| Marker type       | Examples                                           |
| ----------------- | -------------------------------------------------- |
| Separator lines   | `//---`, `//===`, `// - - -`, `// = = =`           |
| ILEDoc delimiters | `///` (exactly three slashes, optional whitespace) |

A 3-step algorithm is applied to the full statement list before formatting:

1. Mark every empty `//` line for suppression.
2. When a structural marker is found, scan **backward**: restore each suppressed
   empty comment as long as the immediately preceding line is also a comment.
3. From the code line found in step 2, scan **forward**: re-suppress leading
   empty `//` lines until the first non-empty comment.

**Example — separator line protects the block:**

```rpgle
// before formatting:
dcl-c MY_CONST '123';
//
// Comment block
//---
dcl-pr cipher extproc('_CIPHER');

// after formatting (Remove empty comment lines = On):
dcl-c MY_CONST '123';

// Comment block
//---
dcl-pr cipher extproc('_CIPHER');
```

**Example — ILEDoc block:**

```rpgle
// before formatting:
dcl-c MY_CONST '123';
//
///
/// Procedure description.
/// @param parm1 The first parameter.
///
dcl-proc myProc;

// after formatting (Remove empty comment lines = On):
dcl-c MY_CONST '123';

///
/// Procedure description.
/// @param parm1 The first parameter.
///
dcl-proc myProc;
```

Lines inside a `///...///` ILEDoc block are never touched regardless of the
setting.

#### Empty Lines Before DCL-PI

When **Remove empty lines before dcl-pi** is enabled, blank lines and empty `//` comment lines that appear immediately before a `dcl-pi` statement are removed. This eliminates the visual gap that sometimes accumulates between `dcl-proc` and `dcl-pi`.

The rule runs after **Remove empty comment lines**, so `//` lines already converted to blanks by that rule are also removed here.

**Example:**

```rpgle
// before formatting:
dcl-proc f_isIfsDir;
  //
  dcl-pi *n ind;
    i_path like(ifs_path_t) const;
  end-pi;
end-proc;

// after formatting (Remove empty lines before dcl-pi = On):
dcl-proc f_isIfsDir;
  dcl-pi *n ind;
    i_path like(ifs_path_t) const;
  end-pi;
end-proc;
```

Lines inside a `@formatter:off` region are never removed.

#### Outer Parentheses of Conditions

When **Outer parentheses of conditions** is set, the condition of `IF`, `ELSEIF`, `WHEN`, `DOW` and `DOU` is enclosed in
parentheses or freed of them:

| Setting                 | Before            | After          |
| ----------------------- | ----------------- | -------------- |
| **With parentheses**    | `if a = 1;`       | `if (a = 1);`  |
| **Without parentheses** | `if ((a = 1));`   | `if a = 1;`    |

- Parentheses that enclose only a part of the condition, as in `if (a = 1) and (b = 2);`, are no outer parentheses.
- Continued lines, that are aligned with the condition, are shifted with it.
- The statement is left unchanged, if a parenthesis follows the operation code without a blank, as in `if(a = 1);`, because it
  could be an operation extender. It is left unchanged as well, if the condition starts on another line, if removing a closing
  parenthesis would leave a line without code or if a literal is continued.
- The rule applies only, if **Format calculations** is **iRPGFormatter**.

#### Casing of Operation Codes and Built-in Functions

When **Apply keyword casing to operation codes and built-in functions** is enabled, the operation code (the first word of a
calculation) and the built-in functions are formatted in the keyword casing style, using the lists **Operation Codes** and
**Built-in Functions**:

| Style            | Example `SETLL` | Example `%SUBDT` |
| ---------------- | --------------- | ---------------- |
| All Uppercase    | `SETLL`         | `%SUBDT`         |
| UpperCamelCase   | `SetLL`         | `%SubDt`         |
| First Char Upper | `Setll`         | `%Subdt`         |
| lowerCamelCase   | `setll`         | `%subDt`         |
| All Lowercase    | `setll`         | `%subdt`         |

- Literals, comments, operation extenders and embedded SQL are not changed. Words, that are not in the lists, stay as they are.
- RPG ignores the casing outside of literals. A line is changed only, if it differs from the original line only in the casing outside of literals.
- The rule applies only, if **Format calculations** is **iRPGFormatter**.

### Error Handling

If the formatter encounters an error while formatting a statement, the original unformatted lines are preserved and the formatter continues with the next statement.

---

## Supported Languages

The plug-in interface is available in:

- Deutsch
- English
- Dutch
- Italian

## See Also

- [iRpgFormatter Update Site](https://github.com/tools-400/irpgformatter)
