package toolwindow;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBTextArea;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import java.util.List;

public class OffensiveIssuePannel extends AbstractIssuesPanel implements Disposable {
    public OffensiveIssuePannel(Project project) {
        super(project);
        setToolbar(actions());
    }

    private static Collection<AnAction> actions() {
        return List.of(
                ActionManager.getInstance().getAction("offensive360.ScanAll"),
                ActionManager.getInstance().getAction("Offensive360.Settings")
        );
    }

    @Override
    public void dispose() {

    }
}
