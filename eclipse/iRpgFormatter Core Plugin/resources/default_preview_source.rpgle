**free
DCL-PROC myProc export;
  DCL-PI *n Pointer ExtProc('myProc');
    // Put delimiter before parameter.
    procedureName VARCHAR(256) OPTIONS(*VARSIZE: *Nopass : *Omit);

    // Sort const/value to end.
    message const Options(*nopass) like(message_t);
  END-PI;

  // Break before keyword.
  DCL-PR myPrototype EXTPROC('MODULE_myPrototype');
    mySubField1 VARCHAR(10);
    mySubField2 VARCHAR(10);
  END-PR;

  // Break name on case change.
  DCL-S longCamelCaseNameExceedingMaxLineLength VarCHAR(100) Template;

  // Casing style.
  DCL-S message_t VarCHAR(100) Template;

  // Use const() in dcl-c statements.
  DCL-C MY_CONSTANT 1;

  // Align sub-fields/parameters.
  dcl-enum myEnum QUALIFIED;
    RED   'red';
    YELLOW 'yellow';
    GREEN 'green' Dft;
  end-enum;

  Return *Null;

END-PROC;

