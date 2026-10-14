/*******************************************************************************
 * Copyright (c) 2012-2026 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.menuextension.test;

/**
 * Values of the LPEX settings of workspace <code>rdp_098</code>, saved on
 * 2026-10-13 (<code>com.ibm.lpex.prefs</code>), in the form returned by
 * <code>LpexView.globalQuery()</code>. <code>INSTALL_POPUP</code> is
 * <code>install.popup</code> of LPEX 4.23.5.
 * <p>
 * The settings contain orphaned entries of iSphere Comments (5 user actions,
 * <code>c-tab</code>, <code>c-s-tab</code>) and the key action
 * <code>c-a-f iRPGFormatter.Format</code> of the user.
 */
public final class TestData {

    public static final String POPUP = "beginSubmenu \"CMOne\" \"Compile\" CMOneCompile \"Recompile dependent objects\" CMOneRecompile endSubmenu "
        + "separator beginSubmenu \"RPG Toolbox\" \"Convert to Free Form\" ConvertRPGFree beginSubmenu \"Indent\" "
        + "\"Indent 0\" Indent0 \"Indent 1\" Indent1 \"Indent 2\" Indent2 \"Indent 3\" Indent3 \"Indent 4\" Indent4 "
        + "\"Indent 5\" Indent5 \"Indent 6\" Indent6 \"Indent 7\" Indent7 \"Indent 8\" Indent8 \"Indent 9\" Indent9 "
        + "endSubmenu separator \"Insert Snippet\" InsertSnippet endSubmenu separator popup.cut cut popup.copy "
        + "copy popup.paste paste separator MARK-Compare beginSubmenu popup.selectMenu popup.blockMarkElement "
        + "blockMarkElement popup.blockMarkCharacter blockMarkCharacter popup.blockMarkRectangle "
        + "blockMarkRectangle endSubmenu beginSubmenu popup.selectedMenu popup.blockCopy blockCopy "
        + "popup.blockOverlay blockOverlay popup.blockMove blockMove popup.blockDelete blockDelete "
        + "popup.blockUpperCase blockUpperCase popup.blockLowerCase blockLowerCase popup.blockShiftLeft "
        + "blockShiftLeft popup.blockShiftRight blockShiftRight separator popup.filterSelection filterSelection "
        + "popup.excludeSelection excludeSelection popup.findSelection findSelection popup.blockFill blockFill "
        + "endSubmenu popup.blockUnmark blockUnmark separator popup.showAll showAll separator MARK-Source "
        + "beginSubmenu popup.sourceMenu separator popup.hexEditLine hexEditLine endSubmenu \"Remove Color "
        + "Codes\" RemoveColorCodesAction";

    public static final String USER_ACTIONS = "Indent8 com.linoma.rpgtoolbox.Indent8 ConvertRPGFree com.linoma.rpgtoolbox.ConvertRPGFree Indent9 "
        + "com.linoma.rpgtoolbox.Indent9 InsertSnippet com.linoma.rpgtoolbox.snippets.InsertSnippet "
        + "iSphere.Lpex.UnComment biz.isphere.lpex.comments.lpex.action.UnCommentAction Indent4 "
        + "com.linoma.rpgtoolbox.Indent4 Indent5 com.linoma.rpgtoolbox.Indent5 Indent6 "
        + "com.linoma.rpgtoolbox.Indent6 Indent7 com.linoma.rpgtoolbox.Indent7 CMOneCompile "
        + "de.taskforce.cmoneng.rse.lpexactions.CompileLPEXAction Indent0 com.linoma.rpgtoolbox.Indent0 Indent1 "
        + "com.linoma.rpgtoolbox.Indent1 CMOneRecompile "
        + "de.taskforce.cmoneng.rse.lpexactions.RecompileLPEXAction Indent2 com.linoma.rpgtoolbox.Indent2 "
        + "Indent3 com.linoma.rpgtoolbox.Indent3 iSphere.Lpex.ToggleComment "
        + "biz.isphere.lpex.comments.lpex.action.ToggleCommentAction iSphere.Lpex.Comment "
        + "biz.isphere.lpex.comments.lpex.action.CommentAction RemoveColorCodesAction "
        + "de.tools400.rdp.removecolorcodes.action.RemoveColorCodesAction iSphere.Lpex.UnIndent "
        + "biz.isphere.lpex.comments.lpex.action.UnIndentAction iSphere.Lpex.Indent "
        + "biz.isphere.lpex.comments.lpex.action.IndentAction";

    public static final String USER_COMMANDS = "editISeriesMember com.ibm.etools.iseries.edit.commands.EditISeriesMemberCommand browseISeriesMember "
        + "com.ibm.etools.iseries.edit.commands.BrowseISeriesMemberCommand runISeriesCommand "
        + "com.ibm.etools.iseries.edit.commands.RunISeriesCommandCommand";

    public static final String USER_KEY_ACTIONS = "c-tab iSphere.Lpex.Indent c-a-1 CMOneCompile c-a-2 CMOneRecompile c-a-f iRPGFormatter.Format c-s-tab "
        + "iSphere.Lpex.UnIndent";

    public static final String INSTALL_POPUP = "popup.cut cut popup.copy copy popup.paste paste separator MARK-Compare beginSubmenu popup.selectMenu "
        + "popup.blockMarkElement blockMarkElement popup.blockMarkCharacter blockMarkCharacter "
        + "popup.blockMarkRectangle blockMarkRectangle endSubmenu beginSubmenu popup.selectedMenu "
        + "popup.blockCopy blockCopy popup.blockOverlay blockOverlay popup.blockMove blockMove "
        + "popup.blockDelete blockDelete popup.blockUpperCase blockUpperCase popup.blockLowerCase "
        + "blockLowerCase popup.blockShiftLeft blockShiftLeft popup.blockShiftRight blockShiftRight separator "
        + "popup.filterSelection filterSelection popup.excludeSelection excludeSelection popup.findSelection "
        + "findSelection popup.blockFill blockFill endSubmenu popup.blockUnmark blockUnmark separator "
        + "popup.showAll showAll separator MARK-Source beginSubmenu popup.sourceMenu separator "
        + "popup.hexEditLine hexEditLine endSubmenu";

    private TestData() {
    }
}
