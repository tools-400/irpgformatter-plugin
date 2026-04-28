/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "de.tools400.lpex.irpgformatter.messages"; //$NON-NLS-1$

    public static String E_R_R_O_R;

    public static String Label_Enter_the_keyword_key_and_its_canonical_value;
    public static String Label_The_key_will_be_converted_to_uppercase_automatically;
    public static String Label_IBM_Settings;
    public static String Label_IBM_Formatter_Settings_Help;
    public static String Label_IBM_Key_Behavior_Settings_Help;
    public static String Label_IBM_Settings_Indent;
    public static String Label_IBM_Settings_Start_column;
    public static String Label_IBM_Settings_End_column;
    public static String Label_General_Settings;
    public static String Label_Keyword_casing_style;
    public static String Label_Use_const_keyword;
    public static String Label_Put_delimiter_before_parameter;
    public static String Label_Parameter_spacing_style;
    public static String Label_Align_Sub_Fields;
    public static String Label_Break_on_case_change;
    public static String Label_Break_before_keyword;
    public static String Label_Sort_const_value_to_end;
    public static String Label_Max_name_length;
    public static String Label_Min_name_length;
    public static String Label_Execute_IBM_formatter;
    public static String Label_Execute_iRPG_formatter;
    public static String Label_Save_Actions;
    public static String Label_Format_on_save;
    public static String Label_Settings;
    public static String Label_Data_Types;
    public static String Label_Declaration_Types;
    public static String Label_Keywords;
    public static String Label_Special_Words;
    public static String Label_Add;
    public static String Label_Change;
    public static String Label_Remove;
    public static String Label_Key;
    public static String Label_Value;

    public static String Label_All_Uppercase;
    public static String Label_UpperCamelCase;
    public static String Label_First_Char_Uppercase;
    public static String Label_lowerCamelCase;
    public static String Label_All_lowercase;

    public static String Label_No_space_parameter;
    public static String Label_Before_parameter;
    public static String Label_After_parameter;
    public static String Label_Before_after_parameter;

    public static String Title_Add_Data_Type;
    public static String Title_Change_Data_Type;
    public static String Title_Duplicate_Data_Type;
    public static String Title_Add_Declaration_Type;
    public static String Title_Change_Declaration_Type;
    public static String Title_Duplicate_Declaration_Type;
    public static String Title_Add_Keyword;
    public static String Title_Change_Keyword;
    public static String Title_Duplicate_Keyword;
    public static String Title_Add_Special_Word;
    public static String Title_Change_Special_Word;
    public static String Title_Duplicate_Special_Word;

    public static String ColumnLabel_Key_read_only;
    public static String ColumnLabel_Value_editable;

    public static String Label_Preview;
    public static String Label_Preview_Edit;
    public static String Label_View_Edit_raw_code;
    public static String Label_Custom_preview_contents;
    public static String Label_Show_whitespaces;
    public static String Label_Preview_line_width;

    public static String Tooltip_IBM_Settings_Start_column;
    public static String Tooltip_IBM_Settings_End_column;
    public static String Tooltip_IBM_Settings_Indent;
    public static String Tooltip_Keyword_casing_style;

    public static String Tooltip_Use_const_keyword;
    public static String Tooltip_Put_delimiter_before_parameter;
    public static String Tooltip_Parameter_spacing_style;
    public static String Tooltip_Align_Sub_Fields;
    public static String Tooltip_Break_on_case_change;
    public static String Tooltip_Break_before_keyword;
    public static String Tooltip_Sort_const_value_to_end;
    public static String Tooltip_Max_name_length;
    public static String Tooltip_Min_name_length;
    public static String Tooltip_Execute_IBM_formatter;
    public static String Tooltip_Execute_iRPG_formatter;
    public static String Tooltip_Format_on_save;
    public static String Tooltip_View_Edit_raw_code;
    public static String Tooltip_Custom_preview_contents;
    public static String Tooltip_Show_whitespaces;
    public static String Tooltip_Preview_line_width;
    public static String Tooltip_Reset_preview_line_width;

    public static String MenuItem_SubMenu_iRPGFormatter;
    public static String MenuItem_Format;

    public static String Message_Source_formatted_successfully;
    public static String Message_Source_Lines_A_B_formatted_successfully;
    public static String Message_No_formatting_applied;

    public static String Error_Format_failed_A;
    public static String Error_Failed_reading_file_A;
    public static String Error_Failed_writing_file_A;
    public static String Error_Unexpected_error_A;
    public static String Error_Unsupported_source_type_A;
    public static String Error_Not_free_format;
    public static String Error_Could_not_format_member;
    public static String Error_Key_is_required;
    public static String Error_Value_is_required;
    public static String Error_Keyword_parameters_must_start_with_A;
    public static String Error_A_data_type_with_key_A_already_exists;
    public static String Error_A_declaration_type_with_key_A_already_exists;
    public static String Error_A_keyword_with_key_A_already_exists;
    public static String Error_A_special_word_with_key_A_already_exists;

    public static String Label_Export;
    public static String Label_Import;
    public static String Label_XML_Files;
    public static String Title_Export_Profile;
    public static String Title_Import_Profile;
    public static String Tooltip_Export;
    public static String Tooltip_Import;
    public static String Error_Export_failed_A;
    public static String Error_Import_failed_A;
    public static String Error_Invalid_profile_format_A;

    public static String Job_Formatting_stream_files;
    public static String Job_Formatting_remote_stream_files;
    public static String Job_Formatting_remote_source_members;
    public static String Job_Formatting;
    public static String SubTask_Reading;
    public static String SubTask_Formatting;
    public static String SubTask_Writing;

    public static String I_N_F_O_R_M_A_T_I_O_N;

    public static String Info_Finished_formatting_stream_files_A;
    public static String Info_Finished_formatting_source_members_A;
    public static String Error_A_formatted_B_errors;
    public static String Error_Not_all_files_formatted_A;
    public static String Error_A_members_formatted_B_errors;
    public static String Error_File_is_locked_A;
    public static String Error_Not_all_members_formatted_A;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
