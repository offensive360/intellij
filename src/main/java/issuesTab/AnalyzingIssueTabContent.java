package issuesTab;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import icon.Offensive360Icons;
import toolwindow.OffensiveIssuePannel;

import javax.swing.*;
import java.awt.*;

public class AnalyzingIssueTabContent {

    public JLabel jLabel;

    public AnalyzingIssueTabContent(){
        jLabel = new JLabel("Analysing.....");

    }
    public void showAnalysing(Project project){
        {
            EventQueue.invokeLater(() -> {
                ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow("Offensive360");
                var contentManager = toolWindow.getContentManager();
                contentManager.removeAllContents(true);
                var issuesPanel = new OffensiveIssuePannel(project);

                jLabel.setIcon(Offensive360Icons.ICON_OFFENSIVE360);
                jLabel.setBorder(BorderFactory.createEmptyBorder(0,10,0,0));
                issuesPanel.setContent(jLabel);
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
}
