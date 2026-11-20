# iRPGFormatter

iRPGFormatter is a plug-in for IBM Rational Developer for i (RDi), that formats fully free-form ILE RPG sources: the control
options, the declarations, the calculations, the comments and the embedded SQL statements.

Further information is available on the [iRPGFormatter web site](https://tools-400.github.io/irpgformatter/).

## Features

- Indents the calculations by the nesting of their blocks and the comment lines like the following statement.
- Formats embedded SQL statements according to the RDi preferences page *Free-form SQL Formatting*.
- Formats the branches of conditional compiling (`/IF`, `/ELSE`, `/ENDIF`) like the compiler.
- Various formatting options, e.g. the casing style, adding or removing the keyword `const()`, replacing the name of
  `dcl-pi` with `*N` and removing the name of `end-proc`.
- Optionally formats the calculations with the IBM formatter.
- Formats the whole source or the selected lines in the LPEX editor. A selection is expanded to the whole block.
- Formats many sources at once from the *Remote Systems* view or the *Project Explorer*.
- Optionally formats a source on save.
- Project specific settings, e.g. for the sources of a Git repository, that the team formats the same way.
- Lists the problems in the view *iRPGFormatter Problems* and in the ruler of the editor.
- Lets AI agents like Claude Code format sources with the settings of RDi: the optional feature *iRPGFormatter
  iAgentBridge* adds the tool `format_rpg` to the MCP server of the plug-in iAgentBridge.

## Installation

iRPGFormatter requires RDi 9.6.0.13 or later.

1. Select **Help > Install New Software...** in RDi.
2. Enter the update site `https://tools-400.github.io/irpgformatter/update-site/eclipse/rdi8.0/` and select **iRPGFormatter**.
3. Finish the wizard and restart RDi.

The optional feature **iRPGFormatter iAgentBridge** requires the plug-in iAgentBridge. Install iAgentBridge first or
together with it.

iRPGFormatter is available on the [Eclipse Marketplace](https://marketplace.eclipse.org/content/irpgformatter-rdi-96013/), too.

## Supported Sources

- Source types **RPGLE** and **SQLRPGLE**.
- Fully free-form sources, that start with `**FREE`. Other sources are not formatted.
- Source members, stream files in the IFS, local files and files of workspace projects.

## Usage

How to format sources, the preferences, the formatting rules and the messages of iRPGFormatter are described in
[USAGE.md](USAGE.md).

## Supported Languages

The plug-in is available in English, German, Italian and Dutch.

## For developers

How the plug-in works internally and how it is built: [DEVELOPMENT.md](DEVELOPMENT.md).

## Liability

This software is free and provided without warranty. You use it at your own risk: test it with non-critical data first, and back up your data before you use it. The [license](LICENSE) applies in addition.

## See Also

- [iRPGFormatter web site](https://tools-400.github.io/irpgformatter/)
- [iRPGFormatter Update Site](https://github.com/tools-400/irpgformatter)
- [Eclipse Marketplace](https://marketplace.eclipse.org/content/irpgformatter-rdi-96013/)
