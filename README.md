# iRPGFormatter Plug-in

## Description

Plug-in for formatting RPGLE **\*\*free** source code in RDi (Rational Developer for i).

Further information about the iRpgFormatter are available on the [iRpgFormatter Web Site](https://tools-400.github.io/irpgformatter/).

## Limitations

The formatter does not have an Abstract Syntax Tree (AST), which usually is required for formatting source code. But it has basic
knowledge about functions, keywords, special words and data types.

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

The problem of the example above is that there are two `dcl-pr` statements in a row. This edge case works, because block statements
are automatically closed if a block statement is followed by a statement of the same type.

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

**Note:** If your selection starts or ends inside a block (DCL-DS, DCL-PR, DCL-PI), the selection is automatically expanded to include the complete block.

## Supported Source Types

- **RPGLE** (ILE RPG free-format)
- **SQLRPGLE** (SQL-based ILE RPG)

The formatter requires **\*\*free** format. Fixed-format sources are not supported.

---

## Preferences

Access preferences via **Window > Preferences > IBM i > Parsers > ILE RPG > iRPGFormatter**, next to the IBM formatter settings.

Consider mapping a **User Key Action** to the `iRPGFormatter.Format` **User Action**.

### IBM Settings

| Setting                   | Preferences                            | Description                                                   |
| ------------------------- | -------------------------------------- | ------------------------------------------------------------- |
| **Start column**          | IBM i Parsers > ILE RPG > Formatter    | The leftmost column for reformatted free-form source          |
| **End column**            | IBM i Parsers > ILE RPG > Formatter    | The rightmost column for reformatted free-form source         |
| **Indentation**           | IBM i Parsers > ILE RPG > Formatter    | The number of blanks to indent nested levels                  |
| **Keyword casing style**  | IBM i Parsers > ILE RPG > Key Behavior | Specifies the style of keywords, special words and data types |
| **Execute IBM formatter** | Default: On                            | Run the IBM formatter before iRPGFormatter                    |

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
| **Execute iRPG formatter**         | On               | Run the iRPGFormatter to apply iRPG formatting rules                                         |

### Experimental Settings

These settings implement new formatting rules that are still being evaluated. They are disabled by default and may change in future releases.

| Setting                              | Default | Description                                                                                                          |
| ------------------------------------ | ------- | -------------------------------------------------------------------------------------------------------------------- |
| **Remove empty comment lines**       | Off     | Remove standalone `//` lines that serve no structural purpose (see [Comment Block Handling])                         |
| **Remove empty lines before dcl-pi** | Off     | Remove blank lines and empty `//` comments immediately before a `dcl-pi` statement (see [Empty Lines Before DCL-PI]) |

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

### Source Preview

The preview pane shows a live preview of the formatted source based on the current settings. Changes in the settings are immediately reflected in the preview, allowing
you to see the impact of your adjustments before applying them to actual source files.

The colors of the preview panel are derived from the current RDi theme, ensuring a consistent look and feel with the rest of the IDE:

| Color                        | Eclipse settings                                                        |
| ---------------------------- | ----------------------------------------------------------------------- |
| Error text color             | General -> Appearance -> Colors and Fonts -> Basics -> Error text color |
| Current line highlight color | General -> Editors -> Text Editors -> Current line highlight            |
| Vertical ruler color         | General -> Editors -> Text Editors -> Print margin                      |
| Line number ruler            | General -> Editors -> Text Editors -> Line number foreground            |

### Import / Export

Formatter settings can be exported to and imported from XML files. This allows sharing a common formatting profile across a team or backing up settings between workspaces.

- **Export...** -- Saves all current settings (general settings, save actions, data types, declaration types, keywords, special words and custom preview source) to an XML file.
- **Import...** -- Loads settings from an XML file and applies them to the preference page. The imported values are shown in the UI but not persisted until you click **Apply** or **OK**.

Partial profiles are supported: if the XML file does not contain all settings, only the included settings are applied; the remaining settings keep their current values.

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
| Compiler directives | `/copy`, `/include`, `/if`, `/define`                | Yes (keyword casing only)                |
| Free directive      | `**free`                                             | Yes (casing only)                        |
| Comments            | `// ...`                                             | Partially (see [Comment Block Handling]) |
| Blank lines         | (empty lines)                                        | No (preserved)                           |
| Other statements    | `if`, `for`, `return`, etc.                          | No (preserved as-is)                     |

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

The keyword casing style is derived from the RDi setting at **Window > Preferences > IBM i > Parsers > ILE RPG > Content assist > Auto-close control structure** (the "endxx" setting). This ensures consistency between auto-completed code and formatted code.

### Indentation

- Indentation uses **spaces** (never tabs).
- The indent size is derived from the RDi setting at **Window > Preferences > IBM i > Parsers > ILE RPG > Content assist > Indentation**.
- Subfields and parameters within blocks (`dcl-ds`, `dcl-pr`, `dcl-pi`, `dcl-enum`) are indented one level deeper than their enclosing block.

**Example:**

```rpgle
dcl-ds customer qualified;
  custId char(10);
  name   char(50);
end-ds;
```

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
