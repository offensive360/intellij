package diaglogs;

import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import credentials.AuthCredentials;
import issuesTab.SettingsTabContent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import utility.Misc;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;


public class SettingsDialog extends DialogWrapper {

    JComponent contentPanel;
    final SettingsTabContent settingsTabContent;
    private String status;


    protected AuthCredentials authCredentials;

    public SettingsDialog(@Nullable Project project, boolean canBeParent, @NotNull IdeModalityType ideModalityType) {
        super(project, canBeParent, ideModalityType);
        setTitle("Offensive360 Settings");
        setOKButtonText("Save");
        settingsTabContent = new SettingsTabContent();
        status = null;
        init();
    }

    @Override
     protected @Nullable JComponent createCenterPanel() {
        contentPanel = new JPanel();
        contentPanel = settingsTabContent.getSettingsTabContent();
        settingsTabContent.label1.setForeground(Color.red);
        settingsTabContent.label1.setIcon(HighlightDisplayLevel.ERROR.getIcon());
        return contentPanel;
    }

    @Override
    protected void doOKAction() {
        try {
            String serverUrl = settingsTabContent.serverUrlField.getText();
            String authToken = settingsTabContent.authTokenField.getText();
            if(validate(serverUrl, authToken)){
                storeDetails(serverUrl,authToken);
                showStatus(status);
                Misc.sendNotification("O360:Settings","Server Details Saved Secure");
                super.doOKAction();
            }
            else{
                showStatus(status);
            }
        }catch (NullPointerException e){
            Messages.showWarningDialog(status,"Offensive360Settings");
        }
    }

    private void showStatus(String status) {
        try {
            settingsTabContent.label1.setText(status);
            settingsTabContent.label1.setEnabled(true);
        }catch (Exception e){
            throw new RuntimeException(e.toString());
        }
    }

    private void storeDetails(String serverUrl, String authToken) {
       try {
           authCredentials = new AuthCredentials();
           if(!authCredentials.storeDetails(serverUrl, authToken)){
               status = "Some error occurred while storing credentials";
           }
           else {
               status = "Saved Successfully";
           }
       }
       catch (Exception e){
          SwingUtilities.invokeLater(()-> Messages.showErrorDialog(e.getMessage(),"Server Exception"));
       }
    }

    private boolean validate(String serverUrl, String authToken) {
        if(serverUrl.isBlank() && authToken.isBlank()){
            status = "Details are Required";
        }
        else if(serverUrl.isBlank()){
            status = "Server Url is Required";
        }
        else if(authToken.isBlank()){
            status = "Auth Token is Required";
        }
        else{
            return checkConnection(authToken,serverUrl);
        }
        return false;
    }

    private boolean checkConnection(String authToken, String serverUrl) {
        try{
            if(!serverUrl.endsWith("/")){
                serverUrl+="/";
            }
            URL url = new URL(serverUrl+"app/api/ExternalScan/scanQueuePosition");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization","Bearer "+authToken);
            int responseCode = connection.getResponseCode();
            if(responseCode == 200){
                return true;
            }
            if(responseCode == 401){
                status = "Invalid Authentication Token";
            }
        } catch (IOException e) {
            status = "Please provide valid details";
        }
        return false;
    }
}