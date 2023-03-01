package toolwindow;

import com.intellij.ide.OccurenceNavigator;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.util.NlsActions;
import com.intellij.tools.SimpleActionGroup;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;

public class AbstractIssuesPanel extends SimpleToolWindowPanel  implements OccurenceNavigator {
    private final Project project;
    private ActionToolbar mainToolbar;

    protected AbstractIssuesPanel(Project project) {
        super(false, false);
        this.project = project;
        createTabs();
    }

    protected void setToolbar(Collection<AnAction> actions) {
        setToolbar(createActionGroup(actions));
    }

    protected void setToolbar(ActionGroup group) {
        if (mainToolbar != null) {
            mainToolbar.setTargetComponent(null);
            super.setToolbar(null);
            mainToolbar = null;
        }
        String ID = "Offensive360";
        mainToolbar = ActionManager.getInstance().createActionToolbar(ID, group, false);
        mainToolbar.setTargetComponent(this);
        var toolBarBox = Box.createHorizontalBox();
        toolBarBox.add(mainToolbar.getComponent());
        toolBarBox.setBackground(Color.GREEN);
        super.setToolbar(toolBarBox);
        mainToolbar.getComponent().setVisible(true);
    }

    private static ActionGroup createActionGroup(Collection<AnAction> actions) {
        var actionGroup = new SimpleActionGroup();
        actions.forEach(actionGroup::add);
        return actionGroup;
    }
    private void createTabs() {
    }

    @Override
    public boolean hasNextOccurence() {
        return false;
    }

    @Override
    public boolean hasPreviousOccurence() {
        return false;
    }

    @Override
    public OccurenceInfo goNextOccurence() {
        return null;
    }

    @Override
    public OccurenceInfo goPreviousOccurence() {
        return null;
    }

    @Override
    public @NlsActions.ActionText @NotNull String getNextOccurenceActionName() {

        return null;
    }

    @Override
    public @NlsActions.ActionText @NotNull String getPreviousOccurenceActionName() {
        return "";
    }
}
