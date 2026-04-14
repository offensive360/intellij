package com.offensive360.intellij.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.offensive360.intellij.UpdateChecker;
import org.jetbrains.annotations.NotNull;

/**
 * Action to manually check for plugin updates.
 * Available in the Tools > Offensive360 menu.
 */
public class CheckUpdateAction extends AnAction {

    public CheckUpdateAction() {
        super("Check for Updates", "Check for Offensive 360 plugin updates", null);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        if (e.getProject() == null) return;
        UpdateChecker.forceCheckAsync(e.getProject());
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabled(e.getProject() != null);
    }
}
