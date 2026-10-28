# iRPGFormatter Plug-in

iRPGFormatter is a plug-in for IBM Rational Developer for i (RDi), that formats fully free-form ILE RPG sources: the control
options, the declarations, the calculations, the comments and the embedded SQL statements.

Further information is available on the [iRPGFormatter web site](https://tools-400.github.io/irpgformatter/).

## Installation

iRPGFormatter requires RDi 9.6.0.13 or later.

1. Select **Help > Install New Software...** in RDi.
2. Enter the update site `https://tools-400.github.io/irpgformatter/update-site/eclipse/rdi8.0/` and select **iRpgFormatter**.
3. Finish the wizard and restart RDi.

iRPGFormatter is available on the [Eclipse Marketplace](https://marketplace.eclipse.org/content/irpgformatter-rdi-96013/), too.

## Supported Sources

- Source types **RPGLE** and **SQLRPGLE**.
- Fully free-form sources, that start with `**FREE`. Other sources are not formatted.
- Source members, stream files in the IFS, local files and files of workspace projects.

## Usage

### Formatting in the Editor

Right-click in the LPEX editor and select **Source > iFormat**.

- Without a selection, the whole source is formatted.
- With selected lines, only these lines are formatted. If the selection starts or ends inside a block (DCL-DS, DCL-PR,
  DCL-PI, DCL-PROC, DCL-ENUM), it is expanded to the whole block. The selected lines are indented like in the whole source.

### Formatting Several Sources

Select the sources in the **Remote Systems** view or in the **Project Explorer** and select **Source > iFormat** in the
context menu:

- **Remote Systems**: object filters, libraries, source files and source members of type RPGLE or SQLRPGLE, IFS filters,
  folders and files with the extension RPGLE or SQLRPGLE, and the same for local files.
- **Project Explorer**: folders and files with the extension RPGLE or SQLRPGLE.

The sources are formatted in the background. Locked members are skipped. At the end a message tells how many sources were
formatted and lists the sources with errors or warnings and their messages.

### Keyboard Shortcut

iRPGFormatter does not assign a key. To format with a key, map a **User Key Action** to the action `iRPGFormatter.Format`:

1. Open **Window > Preferences > LPEX Editor > User Key Actions**.
2. Enter a key, e.g. `c-a-f` for Ctrl+Alt+F, and the action `iRPGFormatter.Format`.
3. Click **Set** and **Apply and Close**.

The action is not listed on the LPEX preference page **User Actions**, because iRPGFormatter adds it to each LPEX editor
itself. Your key is kept when you restart RDi.

### Format on Save

With the setting **Format on save**, a source is formatted, when you save it in the LPEX editor with **Save** or **Save All**.
It is not formatted, when RDi asks to save it while the editor is closed. Statements, that cannot be formatted, stay
unchanged. No message is shown.

## Preferences

Open **Window > Preferences > iRpgFormatter**. This page leads to the preference page of iRPGFormatter at
**Remote Systems > Remote Systems LPEX Editor > IBM i Parsers > ILE RPG > iRpgFormatter**, next to the IBM formatter settings.

The preview at the bottom of the page shows the effect of the settings immediately. Its colors follow the RDi theme.

### IBM Settings

iRPGFormatter uses some settings of RDi. They are shown read-only. The links of the group open the RDi preference pages at
**Remote Systems > Remote Systems LPEX Editor > IBM i Parsers > ILE RPG**, where the settings are changed.

| Setting                                              | RDi preference page      | Description                                                                                          |
| ---------------------------------------------------- | ------------------------ | ---------------------------------------------------------------------------------------------------- |
| **Start column**                                     | Formatter                | The leftmost column of the formatted source                                                          |
| **End column**                                       | Formatter                | The rightmost column of the formatted source                                                         |
| **Indentation**                                      | Formatter                | The number of blanks to indent nested levels                                                         |
| **Indent WHEN/OTHER within SELECT statement**        | Formatter                | The extra blanks of WHEN and OTHER within a SELECT group                                             |
| **Preserve relative indentation of continued lines** | Formatter                | Keep the indentation of continued lines relative to the first line instead of lining them up         |
| **Formatting**                                       | Free-form SQL Formatting | Format embedded SQL statements, see [Embedded SQL](#embedded-sql)                                    |
| **Automatic uppercasing**                            | Free-form SQL Formatting | Change the casing of embedded SQL statements                                                         |
| **Keyword casing style**                             | Key Behavior             | The style of **Automatic closure of control block**, used for keywords, special words and data types |

### General Settings

| Setting                                    | Default          | Description                                                                                  |
| ------------------------------------------ | ---------------- | -------------------------------------------------------------------------------------------- |
| **Use const() keyword in dcl-c statements** | Off              | Wrap constant values in `const()`                                                            |
| **Put delimiter before parameter**         | Off              | Place the colon before a keyword parameter instead of after it                               |
| **Parameter spacing style**                | Before parameter | Blanks around the colon between keyword parameters                                           |
| **Align sub-fields/parameters**            | On               | Align the keywords within blocks at a common column                                          |
| **Break name on case change**              | Off              | Break long names at camelCase boundaries                                                     |
| **Break before keyword**                   | Off              | Break the line before a keyword, when its parameters don't fit                               |
| **Sort const/value to end**                | Off              | Move the `const()`/`value()` keyword to the end of the statement                             |
| **Replace dcl-pi name with \*N**           | On               | Inside a DCL-PROC, rewrite the DCL-PI name to `*N`; when off, restore the procedure name     |
| **Remove end-proc name**                   | On               | Remove the optional procedure name from END-PROC; when off, ensure it matches the DCL-PROC   |
| **Unindent compiler directives**           | On               | Force compiler directives to column 1; when off, indent them like the enclosing block        |
| **Maximum name length**                    | 60               | Maximum characters of a name or literal before it is split with `...`                        |
| **Minimum name length**                    | 10               | Minimum characters, that must remain on a line when a long name is split                     |
| **Format declarations**                    | On               | Format the control options, the declarations and the compiler directives                     |
| **Format calculations**                    | iRPGFormatter    | Select who formats the calculations, the comment lines in front of them and the embedded SQL |

**Format calculations** has these values:

| Value             | Calculations, comment lines and embedded SQL                                                                      |
| ----------------- | ----------------------------------------------------------------------------------------------------------------- |
| **Off**           | Left unchanged                                                                                                    |
| **IBM formatter** | Formatted by the IBM formatter, before iRPGFormatter formats the declarations. iRPGFormatter leaves them unchanged |
| **iRPGFormatter** | Formatted by iRPGFormatter, see [Calculations](#calculations) (default)                                           |

- If both settings are off, nothing is formatted.
- The IBM formatter needs an editor. When you format sources in the Remote Systems view or in the Project Explorer, it does not
  run and the calculations stay unchanged.
- With **IBM formatter**, the preview shows the calculations as iRPGFormatter would format them, because the IBM formatter
  cannot run in the preview. The result is nearly always the same. It differs for inactive `/IF` branches, which the IBM
  formatter leaves unchanged, and for sources with structural errors, such as a missing `ENDIF`, which the IBM formatter does
  not format at all.
- RDi itself cannot format a source, when it is saved. With **Format on save**, **Format declarations** off and
  **Format calculations** set to **IBM formatter**, iRPGFormatter runs only the IBM formatter, when you save.

### Experimental Settings

These settings implement new formatting rules, that are still being evaluated. They are off by default and may change in
future releases.

| Setting                                                             | Default | Description                                                                                       |
| ------------------------------------------------------------------- | ------- | ------------------------------------------------------------------------------------------------- |
| **Remove empty comment lines**                                      | Off     | Replace empty `//` lines by empty lines, see [Empty Comment Lines](#empty-comment-lines)           |
| **Remove empty lines before dcl-pi**                                | Off     | Remove empty lines in front of `dcl-pi`, see [Empty Lines Before DCL-PI](#empty-lines-before-dcl-pi) |
| **Outer parentheses of conditions**                                 | Off     | Add or remove the parentheses around a condition, see [Outer Parentheses of Conditions](#outer-parentheses-of-conditions) |
| **Apply keyword casing to operation codes and built-in functions**  | Off     | See [Casing of Operation Codes and Built-in Functions](#casing-of-operation-codes-and-built-in-functions) |
| **Apply keyword casing to special words of the calculations**       | Off     | See [Casing of Special Words in Calculations](#casing-of-special-words-in-calculations)             |

### Save Actions

| Setting            | Default | Description                                         |
| ------------------ | ------- | --------------------------------------------------- |
| **Format on save** | Off     | Format a source, when it is saved in the LPEX editor |

### Word Lists

The tabs next to **Settings** contain the spelling of the words, that are formatted in the keyword casing style:

| Tab                    | Contents                                                                                      |
| ---------------------- | --------------------------------------------------------------------------------------------- |
| **Data Types**         | Data types, such as `CHAR` or `VARCHAR`                                                       |
| **Declaration Types**  | Declaration types, such as `DCL-DS`                                                           |
| **Keywords**           | Keywords of the declarations, such as `EXTPROC` or `QUALIFIED`                                |
| **Special Words**      | Special words, such as `*NOPASS`, `*OMIT` or `*ZEROS`, of the declarations and the calculations |
| **Operation Codes**    | Operation codes, such as `SETLL` or `EVAL-CORR`                                               |
| **Built-in Functions** | Built-in functions, such as `%SUBDT`                                                          |

- The lists correspond to the ILE RPG Reference for IBM i 7.6. Words, that are not in a list, keep their spelling.
- The list **Special Words** is used for the declarations and, if enabled, for the calculations. A special word, that is added
  to it, is formatted in the declarations, too.
- IBM writes operation codes and built-in functions in upper case only. Their UpperCamelCase spelling, such as `SetLL` or
  `%SubDt`, is a choice of iRPGFormatter and can be changed in the lists.
- Once you changed a list, new entries of a later version of iRPGFormatter are not added to it. **Restore Defaults** brings
  them in, but resets all settings and lists of the page.

### Import / Export

The settings can be exported to and imported from XML files, e.g. to share a profile in a team.

- **Export...** saves all settings of iRPGFormatter and the custom preview source to an XML file. The IBM settings are not
  part of the profile, because they belong to RDi.
- **Import...** loads a profile and shows its settings on the page. They are saved, when you click **Apply** or
  **Apply and Close**. Settings, that are missing in the profile, e.g. in a profile of a former version, keep their current
  values; a message tells so.

## Formatting Rules

### What Gets Formatted

| Statement type      | Examples                                             | Formatted                                    |
| ------------------- | ---------------------------------------------------- | -------------------------------------------- |
| Control options     | `ctl-opt`                                            | Yes                                          |
| Declarations        | `dcl-s`, `dcl-f`, `dcl-c`                            | Yes                                          |
| Block openers       | `dcl-ds`, `dcl-pr`, `dcl-pi`, `dcl-proc`, `dcl-enum` | Yes                                          |
| Subfields           | Fields and parameters within blocks                  | Yes                                          |
| Block closers       | `end-ds`, `end-pr`, `end-pi`, `end-proc`, `end-enum` | Yes                                          |
| Compiler directives | `/copy`, `/include`, `/if`, `/define`                | Casing and indentation                       |
| Free directive      | `**free`                                             | Casing                                       |
| Comments            | `// ...`                                             | Indentation, see [Comments](#comments)       |
| Calculations        | `if`, `for`, `return`, etc.                          | Indentation, see [Calculations](#calculations) |
| Embedded SQL        | `exec sql`                                           | Yes, see [Embedded SQL](#embedded-sql)       |
| Blank lines         | (empty lines)                                        | No                                           |

**Format declarations** controls all statements except the calculations, **Format calculations** controls the calculations,
the comment lines in front of them and the embedded SQL, see [General Settings](#general-settings).

### Keyword Casing

Keywords, data types, declaration types and special words are formatted in the keyword casing style of RDi, see
[IBM Settings](#ibm-settings):

| Style            | Example `DCL-DS` | Example `*NOPASS` | Example `Char` |
| ---------------- | ---------------- | ----------------- | -------------- |
| All Uppercase    | `DCL-DS`         | `*NOPASS`         | `CHAR`         |
| UpperCamelCase   | `Dcl-Ds`         | `*NoPass`         | `Char`         |
| First Char Upper | `Dcl-ds`         | `*Nopass`         | `Char`         |
| lowerCamelCase   | `dcl-Ds`         | `*noPass`         | `char`         |
| All Lowercase    | `dcl-ds`         | `*nopass`         | `char`         |

Taking the style from RDi keeps formatted code and code, that RDi completes, consistent. Optionally the operation codes,
built-in functions and special words of the calculations are formatted in the same style, see
[Casing of Operation Codes and Built-in Functions](#casing-of-operation-codes-and-built-in-functions) and
[Casing of Special Words in Calculations](#casing-of-special-words-in-calculations).

### Indentation

- Indentation uses blanks, never tabs. The number of blanks is the IBM setting **Indentation**.
- Subfields and parameters within blocks (`dcl-ds`, `dcl-pr`, `dcl-pi`, `dcl-enum`) are indented one level deeper than their
  block.
- Calculations are indented by the nesting of their blocks, see [Calculations](#calculations).

```rpgle
dcl-ds customer qualified;
  custId char(10);
  name   char(50);
end-ds;
```

### Calculations

Calculations are indented by the nesting of their blocks: `IF`, `DOW`, `DOU`, `FOR`, `FOR-EACH`, `SELECT`, `MONITOR` and
`BEGSR`. Apart from the experimental rules, their content is not changed.

- Branches, such as `ELSE`, `ELSEIF`, `WHEN`, `OTHER`, `ON-ERROR`, `ON-EXCP` and `ON-EXIT`, are placed on the level of the
  statement that starts the block.
- `WHEN` and `OTHER` get the extra blanks of the IBM setting **Indent WHEN/OTHER within SELECT statement**.
- Continued lines keep their indentation relative to the first line or are lined up one level deeper, see the IBM setting
  **Preserve relative indentation of continued lines**. Lines, whose leading blanks belong to a literal continued with `-`,
  are never moved.
- If a line would exceed the end column, the statement is left unchanged and reported as an error.

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

A comment line gets the indentation of the statement that follows it. Blank lines and compiler directives in between are
skipped. A comment line at the end of the source or of the selection keeps the indentation of the block that encloses it.

Comments between the lines of a declaration are moved in front of the declaration and get its indentation.

If an indented comment line would exceed the end column, it is left unchanged and reported as an error.

### Embedded SQL

`EXEC SQL` statements are formatted with the SQL formatter of IBM i Access Client Solutions, which RDi uses, too. They follow
the IBM settings of the page **Free-form SQL Formatting**:

| RDi setting                                      | Result                                                                                                  |
| ------------------------------------------------ | ------------------------------------------------------------------------------------------------------- |
| **Formatting** on                                | `EXEC SQL` on a line of its own, the SQL statement formatted one SQL indent deeper with the line breaks of **New line: On comma** and **On AND/OR keywords** |
| **Formatting** off, **Automatic uppercasing** on | Only the casing is changed, the lines stay as they are                                                  |
| Both off (default of RDi)                        | The statement is only indented                                                                          |

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

Only the blanks, the line breaks and the casing outside of literals and delimited identifiers are changed. If the SQL formatter
cannot format a statement safely, for example because of a syntax error, a literal continued on the next line or a comment
after the semicolon, the statement is only indented.

Differences to RDi:

- The line length is the end column of iRPGFormatter. RDi uses the end column of the RPG formatter as well, but no limit for
  stream files with `*MAX`.
- A comment in front of the first word of the SQL statement is indented like the SQL statement.
- iRPGFormatter always uses the SQL formatter of IBM i Access Client Solutions, even if **Use the Enhanced SQL Formatter** is off.
- RDi 9.6 does not provide this SQL formatter to plug-ins. There embedded SQL is only indented.

### Subfield Alignment

When **Align sub-fields/parameters** is on (default), the keywords after the names within a block are aligned at a common
column, computed from the longest name in the block.

```rpgle
// Align sub-fields/parameters on:
dcl-ds customer qualified;
  custId       char(10);
  customerName char(50);
end-ds;

// Align sub-fields/parameters off:
dcl-ds customer qualified;
  custId char(10);
  customerName char(50);
end-ds;
```

### Line Width and Wrapping

A statement, that exceeds the IBM setting **End column**, is wrapped: the line is broken after the last token that fits and
continues on the next line one indent level deeper.

```rpgle
// Before:
dcl-pr myPrototype extproc('MODULE_myPrototype') opdesc;

// After:
dcl-pr myPrototype
  extproc('MODULE_myPrototype') opdesc;
```

### Name and Literal Breaking

When a single name or literal does not fit on a line:

- **Names** are broken with the ellipsis `...`. With **Break name on case change**, the break is placed at a camelCase boundary.
- **Literals** are broken with `+` (leading blanks of the next line are skipped) or `-` (leading blanks are kept).

```rpgle
dcl-c LONG_MESSAGE 'This is a very long constant message that '+
  'continues to the next line';
```

### Parameter Spacing

The blanks around the colons between keyword parameters follow **Parameter spacing style**:

| Style            | Example              |
| ---------------- | -------------------- |
| No space         | `:*omit:`            |
| Before parameter | `: *omit:` (default) |
| After parameter  | `:*omit :`           |
| Before and after | `: *omit :`          |

### Constants

- With **Use const() keyword in dcl-c statements**, the value is wrapped in `const()`: `dcl-c MY_CONST const(42);`
- With **Sort const/value to end**, the `const()` keyword is moved after all other keywords.

### Procedure Interface Name

Within a DCL-PROC, the name of the DCL-PI may be the procedure name or the placeholder `*N`. **Replace dcl-pi name with \*N**
selects the form:

| Setting | DCL-PI within DCL-PROC contains | Result                         |
| ------- | ------------------------------- | ------------------------------ |
| On      | a name, e.g. `myProc`           | `*N`                           |
| On      | `*N`                            | `*N`                           |
| Off     | `*N`                            | the name of the procedure      |
| Off     | a name                          | unchanged                      |

A DCL-PI outside of a DCL-PROC is never changed.

```rpgle
dcl-proc myProc;
  dcl-pi *n;
    parm1 char(10);
  end-pi;
end-proc;
```

### End-Procedure Name

The optional name on `end-proc` follows **Remove end-proc name**:

| Setting | END-PROC contains            | Result                                  |
| ------- | ---------------------------- | --------------------------------------- |
| On      | a name                       | name removed: `end-proc;`               |
| On      | no name                      | unchanged                               |
| Off     | no name                      | the name of the procedure is inserted   |
| Off     | another name, e.g. a typo    | replaced by the name of the procedure   |
| Off     | the name of the procedure    | unchanged                               |

### Compiler Directives

Compiler directives (`/copy`, `/include`, `/if`, `/define`, `/eof` etc.) get the keyword casing. Their parameters are not
changed. With **Unindent compiler directives** (default), the directives start in column 1, otherwise they are indented like
the enclosing block.

#### Conditional Compilation

Each branch of an `/IF` group starts with the blocks that are open at `/IF`, like in the compiler. After `/ENDIF` the
formatter continues with the blocks that are open at the end of the first branch. Therefore alternative headers of a block
are formatted correctly:

```rpgle
/if defined(DEFINE_PROCPTR)
dcl-pr SrvPgmLayout_new ind extproc(g_pLayout_new);
/else
dcl-pr SrvPgmLayout_new ind extproc('SrvPgmLayout_new');
/endif
  i_pSuper   like(LOG4RPG_pLayout_t) const;
  io_pHandle pointer;
end-pr;
```

- If the branches end with different blocks, the formatter shows a warning, because the indentation of the following lines
  may be wrong.
- `/IF`, `/ELSEIF`, `/ELSE` and `/ENDIF` may appear between the lines of a control option, file or data declaration or
  procedure. They split the declaration into sections, which are formatted on lines of their own. The directives are indented
  like continued lines, the sections within a branch one level deeper. If a directive is enclosed by the parameters of a
  keyword, or if the declaration contains comments as well, the declaration is left unchanged.
- The lines after an `/EOF` directive outside of an `/IF` group are ignored by the compiler and left unchanged.

### Formatter Directives

Comment lines with `@formatter:off` and `@formatter:on` exclude parts of a source from formatting:

```rpgle
// @formatter:off
dcl-s   myVar1   char(10);     // not formatted
dcl-s   myVar2   char(20);     // not formatted
// @formatter:on
dcl-s myVar3 char(30);         // formatted
```

- The directives are not case-sensitive and may contain extra blanks, e.g. `//   @FORMATTER:OFF`.
- Without `@formatter:on`, the rest of the source stays unformatted.
- The directive lines themselves are never changed.

### Experimental Formatting Rules

These rules belong to the [Experimental Settings](#experimental-settings).

#### Empty Comment Lines

With **Remove empty comment lines**, each empty comment line `//` is replaced by an empty line, with these exceptions:

- In a comment block, that contains a separator line, such as `//---` or `//===`, or the ILEDoc delimiter `///`, the empty
  comment lines in front of it are kept, except those at the start of the block.
- Lines within an ILEDoc block (between two `///` lines) and within `@formatter:off` regions are never changed.

```rpgle
// Before:
dcl-c MY_CONST '123';
//
// Comment block
//
//---
dcl-pr cipher extproc('_CIPHER');

// After:
dcl-c MY_CONST '123';

// Comment block
//
//---
dcl-pr cipher extproc('_CIPHER');
```

#### Empty Lines Before DCL-PI

With **Remove empty lines before dcl-pi**, empty lines and empty comment lines `//` directly in front of a `dcl-pi` are
removed, such as the gap between `dcl-proc` and `dcl-pi`:

```rpgle
// Before:
dcl-proc f_isIfsDir;
  //
  dcl-pi *n ind;
    i_path like(ifs_path_t) const;
  end-pi;
end-proc;

// After:
dcl-proc f_isIfsDir;
  dcl-pi *n ind;
    i_path like(ifs_path_t) const;
  end-pi;
end-proc;
```

#### Outer Parentheses of Conditions

With **Outer parentheses of conditions**, the condition of `IF`, `ELSEIF`, `WHEN`, `DOW` and `DOU` is enclosed in
parentheses or freed of them:

| Setting                 | Before          | After         |
| ----------------------- | --------------- | ------------- |
| **With parentheses**    | `if a = 1;`     | `if (a = 1);` |
| **Without parentheses** | `if ((a = 1));` | `if a = 1;`   |

- Parentheses around a part of the condition, as in `if (a = 1) and (b = 2);`, are not changed.
- Continued lines, that are aligned with the condition, are shifted with it.
- In rare cases the statement is left unchanged, e.g. `if(a = 1);` without a blank, because the parenthesis could be an
  operation extender.

#### Casing of Operation Codes and Built-in Functions

With **Apply keyword casing to operation codes and built-in functions**, the operation code (the first word of a calculation)
and the built-in functions are formatted in the keyword casing style, using the lists **Operation Codes** and
**Built-in Functions**:

| Style            | Example `SETLL` | Example `%SUBDT` |
| ---------------- | --------------- | ---------------- |
| All Uppercase    | `SETLL`         | `%SUBDT`         |
| UpperCamelCase   | `SetLL`         | `%SubDt`         |
| First Char Upper | `Setll`         | `%Subdt`         |
| lowerCamelCase   | `setll`         | `%subDt`         |
| All Lowercase    | `setll`         | `%subdt`         |

Literals, comments, operation extenders and embedded SQL are not changed.

#### Casing of Special Words in Calculations

With **Apply keyword casing to special words of the calculations**, the special words of the calculations are formatted in the
keyword casing style, using the list **Special Words**. Besides the figurative constants, such as `*ON` or `*ZEROS`, the list
contains the special words of the built-in functions and operation codes, such as `*DAYS` in `%DIFF(d1 : d2 : *DAYS)`,
`*START` in `SETLL *START file` or `*ESCAPE` in `SND-MSG *ESCAPE`.

| Style            | Example `*INLR = *ON;` |
| ---------------- | ---------------------- |
| All Uppercase    | `*INLR = *ON;`         |
| UpperCamelCase   | `*InLr = *On;`         |
| First Char Upper | `*Inlr = *On;`         |
| lowerCamelCase   | `*inLr = *on;`         |
| All Lowercase    | `*inlr = *on;`         |

- An asterisk directly after an operand is a multiplication. In `price*DAYS` the name `DAYS` stays as it is.
- Numbered indicators, such as `*IN03`, and date formats with the separator `0`, such as `*ISO0`, stay as they are.
- Literals, comments and embedded SQL are not changed.

The rules for the calculations, the outer parentheses and the casing of operation codes, built-in functions and special
words, apply only, if **Format calculations** is **iRPGFormatter**. RPG ignores the casing outside of literals, therefore the
casing rules change a line only, if it differs from the original line in nothing but the casing outside of literals.

## Limitations and Messages

iRPGFormatter recognizes the statements and the blocks of a source, but it does not compile the source. Therefore:

- `/COPY` and `/INCLUDE` members are not read. If a block starts or ends in such a member, the indentation may be wrong or the
  source cannot be formatted.
- If the branches of an `/IF` group end with different blocks, the indentation of the following lines may be wrong. The
  formatter shows a warning.
- A source with structural errors, such as a missing `ENDIF`, is indented by the blocks as they are written. A block, that
  is not closed, ends with the block that encloses it, e.g. at `END-PROC`.

The formatter reports problems in a dialog:

- **Errors**: A statement, that cannot be formatted, e.g. because a line would exceed the end column, stays unchanged. The
  dialog lists these statements with their line numbers. All other statements are formatted.
- **Warnings**: The source is formatted, but the indentation after the reported line may be wrong.

If a source cannot be formatted at all, the message line of the editor shows the reason.

## Upgrading from Version 1.0.7 or Earlier

- **Format declarations** is the former setting **Execute iRPG formatter**.
- **Format calculations** replaces the former setting **Execute IBM formatter**. If that one was switched off, **Format
  calculations** is **Off**, otherwise **iRPGFormatter**. Profiles of former versions are converted the same way.
- The calculations are now formatted by iRPGFormatter by default. To let the IBM formatter format them as before, set
  **Format calculations** to **IBM formatter**.

## Supported Languages

The plug-in is available in English, German, Italian and Dutch.

## See Also

- [iRPGFormatter web site](https://tools-400.github.io/irpgformatter/)
- [Project page on GitHub](https://github.com/tools-400/irpgformatter-plugin)
- [Eclipse Marketplace](https://marketplace.eclipse.org/content/irpgformatter-rdi-96013/)
- [DEVELOPMENT.md](DEVELOPMENT.md) - notes for developers of iRPGFormatter
