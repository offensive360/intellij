package actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.ui.Messages;
import offensive360api.StartScan;
import org.jetbrains.annotations.NotNull;
import utility.Misc;


public class MainMenu extends AnAction {
    int statusCode;
    private static boolean status_of_queue=false;
    public static  String TOOL_WINDOW_ID = "Offensive360";
    public boolean getStatusOfQueue(){
        return this.status_of_queue;
    }
    public  void setStatusOfQueue(){
        this.status_of_queue = !this.status_of_queue ;
    }
    public void actionPerformed(@NotNull AnActionEvent e) {
        try {
            if(!this.getStatusOfQueue()) {
                this.status_of_queue = true;
                new Misc(e.getProject());
                StartScan currScan = new StartScan(e.getProject(), "Offensive360 Scan ...", true);
                ProgressManager.getInstance().run(currScan);
            }
            else{
                Messages.showWarningDialog("A Scan is already going on please wait...","Offensive360 Scan");
            }
        } catch (Exception ex) {
            this.setStatusOfQueue();
            throw new RuntimeException(ex);
        }
    }
}
