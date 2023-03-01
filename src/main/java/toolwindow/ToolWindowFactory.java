package toolwindow;

import APIResponse.SingleScanResponse;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.content.ContentManager;
import icon.Offensive360Icons;
import issuesTab.SettingsTabContent;
import org.jetbrains.annotations.NotNull;
import javax.swing.*;

public class ToolWindowFactory implements com.intellij.openapi.wm.ToolWindowFactory {
    public static String TOOL_WINDOW_ID = "Offensive360";
    SingleScanResponse singleScanResponse;

    private SettingsTabContent settingsTabContent;
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull com.intellij.openapi.wm.ToolWindow toolWindow) {
        var contentManager = toolWindow.getContentManager();
        settingsTabContent = new SettingsTabContent();
        addissuesTab(project,contentManager);
        addSettingsTab(project,contentManager);
        }

        public void update(Project project,com.intellij.openapi.wm.ToolWindow toolWindow,SingleScanResponse singleScanResponse){
        this.singleScanResponse = singleScanResponse;
        createToolWindowContent(project,toolWindow);
        }



    private void addissuesTab(Project project, ContentManager contentManager) {
        var issuesPanel = new OffensiveIssuePannel(project);

        issuesPanel.setContent(getIssuesTabContent(project));
        var issuesContent = contentManager.getFactory()
                .createContent(
                        issuesPanel,
                        "Issues",
                        false);
        issuesContent.setCloseable(false);
        contentManager.addDataProvider(issuesPanel);
        contentManager.addContent(issuesContent);
    }

    private  void addSettingsTab(Project project,ContentManager contentManager){
        var settingsPanel = new OffensiveIssuePannel(project);

        settingsPanel.setContent(getSettingsTabContent(project));
        var settingsContent = contentManager.getFactory()
                .createContent(settingsPanel,
                        "Settings",
                        false);
        settingsContent.setCloseable(false);
        contentManager.addDataProvider(settingsPanel);
        contentManager.addContent(settingsContent);

    }

    private JComponent getSettingsTabContent(Project project) {
       try{
           System.out.println(settingsTabContent.getSettingsTabContent().toString());
           return (JComponent) settingsTabContent.getSettingsTabContent().getComponent(2);
       }
       catch (Exception e){
           System.out.println(e.toString());
           throw new ArrayIndexOutOfBoundsException();
       }
    }

    private JLabel getIssuesTabContent(Project project)  {
      JLabel intialLabel = new JLabel("No Analysis Done on Project, Perform Scan to get the Report");
        intialLabel.setIcon(Offensive360Icons.INFO_ICON);
        intialLabel.setBorder(BorderFactory.createEmptyBorder(0,20,0,0));
        return intialLabel;
    }

}
