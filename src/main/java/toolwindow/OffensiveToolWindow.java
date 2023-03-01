package toolwindow;

import APIResponse.SingleScanResponse;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.ContentManagerListener;
import issuesTab.IssuesTabContent;

public class OffensiveToolWindow implements ContentManagerListener {
    Project project;
    public OffensiveToolWindow(Project project){this.project = project;}
    public void showIssues(SingleScanResponse singleScanResponse)
    {
        var contentManager = getToolWindow().getContentManager();
        var content = contentManager.findContent("Issues");
        contentManager.removeAllContents(false);

    }


    private ToolWindow getToolWindow() {
        var toolWindowManager = ToolWindowManager.getInstance(project);
        return toolWindowManager.getToolWindow(ToolWindowFactory.TOOL_WINDOW_ID);
    }

    private static void selectTab(ToolWindow toolWindow, String tabId) {
        var contentManager = toolWindow.getContentManager();
        var content = contentManager.findContent(tabId);
        if (content != null) {
            contentManager.setSelectedContent(content);
        }
    }

}
