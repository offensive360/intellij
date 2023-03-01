package actions;

import APIResponse.Vulnerability;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class SupressAction extends AnAction {

    Vulnerability data;
    private static final String FOLDER_NAME = ".360";
    private static final String FILE_NAME = ".360ignore";
    public SupressAction(){

    }
    public  SupressAction(Object data){
        if(data instanceof Vulnerability) this.data = (Vulnerability) data;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        if(data!=null) {
            Project project = e.getProject();
            assert project != null;
            String baseDir = project.getBasePath();
            String folderPath = baseDir + File.separator + FOLDER_NAME;
            String filePath = folderPath + File.separator + FILE_NAME;
            File folder = new File(folderPath);
            File file = new File(filePath);
            if (!folder.exists()) {
                if (folder.mkdir()) {
                    System.out.println("Folder created: " + folderPath);
                } else {
                    System.out.println("Folder could not be created: " + folderPath);
                    return;
                }
            }

            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                boolean pathExists = false;
                while ((line = reader.readLine()) != null) {
                    if (line.equals(data.getFilePath())) {
                        pathExists = true;
                        break;
                    }
                }

                if (!pathExists) {
                    try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
                        writer.write(data.getFilePath()+"@"+data.getLineNumber());
                        writer.newLine();
                        System.out.println("File path added to " + filePath);
//                        Messages.showInfoMessage("Suppressed Successfully","Offensive360 Suppression");
                    }
                }
            } catch (IOException ex) {
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                    writer.write(data.getFilePath()+"@"+data.getLineNumber());
                    writer.newLine();
                    System.out.println("File created and file path added to " + filePath);
//                    Messages.showInfoMessage("Suppre,ssed Successfully", "Offensive360 Supression");
                } catch (IOException e2) {
                    System.out.println("File could not be created: " + filePath);
                    e2.printStackTrace();
                }
            }
        }
    }
}
