**free
DCL-PROC myProc VARCHAR(10);
  DCL-PI *n ExtProc('externalProcedureName');
    marker VARCHAR(10) Const OPTIONS(*VARSIZE: *TRIM : *Omit);
  END-PI;

  DCL-PR myPrototype EXTPROC('MODULE_myPrototype');
    mySubField1 VARCHAR(10);
    mySubField2 VARCHAR(10);
  END-PR;

  DCL-S myPointer POINTER;
  DCL-C MY_CONSTANT 1;
  DCL-DS myStructure LIKEDS(refStruct_t);

  dcl-enum myEnum Char(10) QUALIFIED;
    RED   'red';
    YELLOW 'yellow';
    GREEN 'green' Dft;
  end-enum;

  Return *Null;

END-PROC;
