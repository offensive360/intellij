package issuesTab;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.components.JBLabel;
import icon.Offensive360Icons;
import toolwindow.OffensiveIssuePannel;

import javax.swing.*;
import java.awt.*;

public class APICallErrorContent {
    JBLabel errorMessageLabel;

    public  void showError(Project project,String errorMessage){
        EventQueue.invokeLater(() -> {
            ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow("Offensive360");
            var contentManager = toolWindow.getContentManager();
            contentManager.removeAllContents(true);
            var issuesPanel = new OffensiveIssuePannel(project);
            errorMessageLabel = new JBLabel(errorMessage);
            errorMessageLabel.setIcon(Offensive360Icons.ERROR_ICON);
            errorMessageLabel.setBorder(BorderFactory.createEmptyBorder(0,10,0,0));
            issuesPanel.setContent(errorMessageLabel);

            var issuesContent = contentManager.getFactory()
                    .createContent(
                            issuesPanel,
                            "Issues",
                            false);
            issuesContent.setCloseable(false);
            contentManager.addDataProvider(issuesPanel);
            contentManager.addContent(issuesContent);
        });

    }
}
