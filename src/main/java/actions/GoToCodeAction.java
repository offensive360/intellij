package actions;

import APIResponse.Vulnerability;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.editor.markup.EffectType;
import com.intellij.openapi.editor.markup.HighlighterLayer;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import java.awt.*;

//Author : shyam_vegi

/**
 * Go To Code
 * This is an action which listens to events on issue tree
 * It performs action when user performs double click on vulnerability.
 * Navigates user to source code and offset.
 * Highlights risk with color indication
**/

public class GoToCodeAction extends AnAction {
    private String filePath;
    int line;
    int column;

    /**
     * Risk color Accordingly
     * 1-Green - low
     * 2-purple - medium
     * 3-orange - high
     * 4-red - Critical
     */
    static final Color[] color = new Color[]{
            new Color(0xFC0D770F, true),
            new Color(0xFF0E45F0, true),
            new Color(0xFEEF7F04, true),
            new Color(0xFEE50B0B, true)
    };

    private Color riskColor;
    //Default Constructor
    @SuppressWarnings({})
    public GoToCodeAction(){
    }

    //Constructor initializes data
    public GoToCodeAction(Vulnerability data){
        super();
        filePath = data.getFilePath().replace("\\","/");
        String[] s = data.getLineNumber().split(",",2);
        line = Integer.parseInt(s[0]);
        column = Integer.parseInt(s[1]);
        riskColor = color[Integer.parseInt(data.getRiskLevel())-1];
    }

    //Logic to navigate to code and offset
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        try {
            Project project = e.getData(CommonDataKeys.PROJECT);
            assert project != null;
            VirtualFile vf = project.getBaseDir().findFileByRelativePath(filePath);
            if (vf != null) {
                FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
                fileEditorManager.openFile(vf, true);
                Editor editor = fileEditorManager.getSelectedTextEditor();
                assert editor!=null;
                editor.getCaretModel().moveToLogicalPosition(new LogicalPosition(line-1, column));
                editor.getScrollingModel().scrollToCaret(ScrollType.CENTER);
                TextAttributes textAttributes = new TextAttributes(riskColor, Color.WHITE, Color.BLACK, EffectType.SLIGHTLY_WIDER_BOX, Font.BOLD);
                editor.getMarkupModel().addLineHighlighter(line-1, HighlighterLayer.WARNING, textAttributes);
            } else {
                Messages.showErrorDialog("Invalid file", "Offensive360 Error");
            }
        }catch (Exception ex){
            Messages.showErrorDialog("Unable to navigate","Offensive360 Scan");
        }
    }
}
