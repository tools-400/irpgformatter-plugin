# iRPGFormatter Plug-in

## Description

Plug-in for formatting RPGLE **\*\*free** source code in RDi (Rational Developer for i).

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

## Preferences

Access preferences via **Window > Preferences > iRPGFormatter**

| Setting                       | Description                                                                                                        |
| ----------------------------- | ------------------------------------------------------------------------------------------------------------------ |
| **Maximum source line width** | Maximum line width before wrapping (default: 90)                                                                   |
| **Keyword casing style**      | How keywords are formatted: All Uppercase, Upper Camel Case, First Char Uppercase, Lower Camel Case, All Lowercase |
| **Use const() keyword**       | Whether to use `const()` wrapper in DCL-C statements                                                               |
| **Execute IBM formatter**     | Run the IBM formatter before iRPGFormatter                                                                         |
| **Keywords**                  | Custom keyword mappings for special casing                                                                         |
| **Keyword Parameters**        | Custom keyword parameter mappings (e.g., *NOPASS, *OMIT)                                                           |

## Supported Languages

The plug-in interface is available in:

- English
- German (Deutsch)
- Dutch (Nederlands)
- Italian (Italiano)
