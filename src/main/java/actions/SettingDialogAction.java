package actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.DialogWrapper;

import diaglogs.SettingsDialog;
import org.jetbrains.annotations.NotNull;
import utility.Misc;

public class SettingDialogAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        new Misc(e.getProject());
        SettingsDialog settingsDialog = new SettingsDialog(e.getProject(),true, DialogWrapper.IdeModalityType.IDE);
        settingsDialog.show();
    }
}
