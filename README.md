# iRPGFormatter Plug-in

## Description

Plug-in for formatting RPGLE **\*\*free** source code in RDi (Rational Developer for i).

Further information about the iRpgFormatter are available on the [iRpgFormatter Web Site](https://tools-400.github.io/irpgformatter/).

## Usage

### Formatting Source Code

1. Open an RPGLE source member in the LPEX editor
2. Right-click to open the context menu
3. Select **iRPGFormatter > iFormat**

### Formatting Selected Lines

You can format only a portion of the source:

1. Select the lines you want to format
2. Right-click and select **iRPGFormatter > iFormat**
3. Only the selected lines will be formatted

**Note:** If your selection starts or ends inside a block (DCL-DS, DCL-PR, DCL-PI), the selection is automatically expanded to include the complete block.

## Supported Source Types

- **RPGLE** (ILE RPG free-format)
- **SQLRPGLE** (SQL-based ILE RPG)

The formatter requires **\*\*free** format. Fixed-format sources are not supported.

---

## Preferences

Access preferences via **Window > Preferences > IBM i > Parsers > ILE RPG > iRPGFormatter**

### IBM Settings

| Setting                   | Preferences                            | Description                                                   |
| ------------------------- | -------------------------------------- | ------------------------------------------------------------- |
| **Start column**          | IBM i Parsers > ILE RPG > Formatter    | The leftmost column for reformatted free-form source          |
| **End column**            | IBM i Parsers > ILE RPG > Formatter    | The rightmost column for reformatted free-form source         |
| **Indentation**           | IBM i Parsers > ILE RPG > Formatter    | The number of blanks to indent nested levels                  |
| **Keyword casing style**  | IBM i Parsers > ILE RPG > Key Behavior | Specifies the style of keywords, special words and data types |
| **Execute IBM formatter** | Default: On                            | Run the IBM formatter before iRPGFormatter                    |

### General Settings

| Setting                        | Default          | Description                                                         |
| ------------------------------ | ---------------- | ------------------------------------------------------------------- |
| **Use const() keyword**        | Off              | Wrap constant values in `const()` for DCL-C statements              |
| **Delimiter before parameter** | Off              | Place colon delimiter before parameter instead of after             |
| **Parameter spacing**          | Before parameter | Spacing around colon between keyword parameters                     |
| **Align sub-fields**           | On               | Align keywords within blocks at a common column                     |
| **Break between case change**  | Off              | Break long names at camelCase boundaries                            |
| **Break before keyword**       | Off              | Break line before keyword when parameters don't fit                 |
| **Sort const value to end**    | Off              | Move `const()`/`value()` keyword to the end of the statement        |
| **Maximum name length**        | 60               | Max characters for a name or literal before it is split with `...`  |
| **Minimum name length**        | 20               | Min characters that must remain on a line when splitting long names |
| **Execute iRPG formatter**     | On               | Run the iRPGFormatter to apply iRPG formatting rules                |

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

### Import / Export

Formatter settings can be exported to and imported from XML files. This allows sharing a common formatting profile across a team or backing up settings between workspaces.

- **Export...** -- Saves all current settings (general settings, save actions, data types, declaration types, keywords, special words and custom preview source) to an XML file.
- **Import...** -- Loads settings from an XML file and applies them to the preference page. The imported values are shown in the UI but not persisted until you click **Apply** or **OK**.

Partial profiles are supported: if the XML file does not contain all settings, only the included settings are applied; the remaining settings keep their current values.

---

## Formatting Rules

### What Gets Formatted

The formatter processes the following RPGLE statement types:

| Statement Type      | Examples                                             | Formatted?                |
| ------------------- | ---------------------------------------------------- | ------------------------- |
| Control options     | `ctl-opt`                                            | Yes                       |
| Declarations        | `dcl-s`, `dcl-f`, `dcl-c`                            | Yes                       |
| Block openers       | `dcl-ds`, `dcl-pr`, `dcl-pi`, `dcl-proc`, `dcl-enum` | Yes                       |
| Subfields           | Fields/parameters within blocks                      | Yes                       |
| Block closers       | `end-ds`, `end-pr`, `end-pi`, `end-proc`, `end-enum` | Yes                       |
| Compiler directives | `/copy`, `/include`, `/if`, `/define`                | Yes (keyword casing only) |
| Free directive      | `**free`                                             | Yes (casing only)         |
| Comments            | `// ...`                                             | No (preserved as-is)      |
| Blank lines         | (empty lines)                                        | No (preserved)            |
| Other statements    | `if`, `for`, `return`, etc.                          | No (preserved as-is)      |

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

### Compiler Directives

Compiler directives (`/copy`, `/include`, `/if`, `/define`, `/eof`, etc.) are formatted with keyword casing applied to the directive keyword. The directive parameters are preserved as-is. Compiler directives are always placed at column 1 (no indentation).

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

### Error Handling

If the formatter encounters an error while formatting a statement, the original unformatted lines are preserved and the formatter continues with the next statement.

---

## Supported Languages

The plug-in interface is available in:

- Deutsch
- English
- Dutch
- Italian

# See Also

* [iRpgFormatter Update Site](https://github.com/tools-400/irpgformatter)
