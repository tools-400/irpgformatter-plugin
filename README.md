# iRPGFormatter

iRPGFormatter is a plug-in for IBM Rational Developer for i (RDi), that formats fully free-form ILE RPG sources: the control
options, the declarations, the calculations, the comments and the embedded SQL statements.

Further information is available on the [iRPGFormatter web site](https://tools-400.github.io/irpgformatter/).

## Installation

iRPGFormatter requires RDi 9.6.0.13 or later.

1. Select **Help > Install New Software...** in RDi.
2. Enter the update site `https://tools-400.github.io/irpgformatter/update-site/eclipse/rdi8.0/` and select **iRPGFormatter**.
3. Finish the wizard and restart RDi.

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
- [Project page on GitHub](https://github.com/tools-400/irpgformatter-plugin)
- [Eclipse Marketplace](https://marketplace.eclipse.org/content/irpgformatter-rdi-96013/)
