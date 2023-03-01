package offensive360api;

import APIResponse.SingleScanResponse;
import actions.MainMenu;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import credentials.AuthCredentials;
import issuesTab.APICallErrorContent;
import issuesTab.AnalyzingIssueTabContent;
import issuesTab.IssuesList;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import toolwindow.OffensiveIssuePannel;
import utility.Offensive360Exception;
import utility.Misc;
import utility.ZipDirectory;

import javax.swing.*;
import java.awt.*;
import java.io.*;

import static utility.Misc.checkInternet;


/**
 * Author : Shyam_Vegi
 * StartScan  is an Action which scan the project and runs as a background Task.
 */

public class StartScan  extends Task.Backgroundable{

    Project project;
    MainMenu mainMenu = new MainMenu();
    protected String serverUrl;
    protected String authToken;
    protected String zipDirName;
    JLabel scanStatus;

    ProgressIndicator indicator;
    AuthCredentials authCredentials;
    AnalyzingIssueTabContent analyzingIssueTabContent;
    APICallErrorContent apiCallErrorContent;

    CloseableHttpResponse response;
    public StartScan (@Nullable Project project, @NlsContexts.ProgressTitle @NotNull String title, boolean canBeCancelled) {
        super(project, title, canBeCancelled);
        apiCallErrorContent = new APICallErrorContent();
        this.project = project;
    }

    @Override
    public @Nullable NotificationInfo notifyFinished() {
        return super.notifyFinished();
    }

    @Override
    public void onCancel() {
        if(response!=null){
            try {
                response.close();
            } catch (IOException e) {

            }
            finally {
                Misc.sendNotification("Scan Cancelled","Scan aborted by user");
            }
        }
        if(mainMenu.getStatusOfQueue()){
            mainMenu.setStatusOfQueue();
        }
        apiCallErrorContent.showError(project,"Scan Cancelled By The User");
        super.onCancel();
    }

    @Override
    public void run(@NotNull ProgressIndicator indicator) {

        zipDirName = null;

        this.indicator = indicator;
        analyzingIssueTabContent = new AnalyzingIssueTabContent();

        analyzingIssueTabContent.showAnalysing(project);

        scanStatus = analyzingIssueTabContent.jLabel;
        try {
            indicator.setIndeterminate(false);
            indicatorStatus(0.5,"Verifying Internet");
            //Check Internet Connection
            checkInternet();
            indicatorStatus(0.10,"Internet Verified");

            //Check Configuration
            indicatorStatus(0.20,"Checking Configuration");
            getServerDetails();
            indicatorStatus(0.30,"Configured O360");

            //Zip the project/file to run scan on
            indicatorStatus(0.40,"Zipping Project");
            zipScannableFile();
            indicatorStatus(0.50,"Zipping Done");

            //Send it to External api endpoint and receive response
            indicatorStatus(0.60,"Scanning Vulnerabilities");
            String responseString = sendToApi();
            indicatorStatus(0.80,"Scanning Completed");

            //if response is success make an object from it.
            responseToObject(responseString);
            Misc.sendNotification("O360:Scan","Scan Completed.Check Report");
            indicatorStatus(0.1,"Scan Completed");
            SwingUtilities.invokeLater(()->{
                        ToolWindow offToolWindow = ToolWindowManager.getInstance(project).getToolWindow(MainMenu.TOOL_WINDOW_ID);
                        if(offToolWindow!=null){
                            offToolWindow.show();
                        }
            });
        } catch (Offensive360Exception ex) {
            SwingUtilities.invokeLater(()-> Messages.showErrorDialog(project, ex.getMessage(), "Offensive36o Error"));
            Misc.sendNotification("O360 Scan",ex.getMessage());
            apiCallErrorContent.showError(project,ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            Misc.sendNotification("O360:Scan","Scan Failed");
            APICallErrorContent apiCallErrorContent = new APICallErrorContent();
            apiCallErrorContent.showError(project, "Some Error occurred calling API, check the Offensive 360 settings or contact admin.");
        } finally {
            if (zipDirName != null) {
                File delFile = new File(zipDirName);
                if (delFile.exists()) {
                    delFiles(delFile);
                }
            }
            if(mainMenu.getStatusOfQueue()) mainMenu.setStatusOfQueue();
        }
    }



    private void indicatorStatus(double fraction,String text) {
        indicator.setText(text);
        indicator.setFraction(fraction);
        changeLabel(text);
    }


    private void responseToObject(String responseString) throws Offensive360Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        SingleScanResponse singleScanResponse;
            try {
                singleScanResponse = objectMapper.readValue(responseString, SingleScanResponse.class);
            } catch (Exception ex) {
                throw new Offensive360Exception(ex.getMessage());
            }
            SingleScanResponse finalSingleScanResponse = singleScanResponse;
            EventQueue.invokeLater(() -> {
                ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow("Offensive360");
                assert toolWindow != null;
                var contentManager = toolWindow.getContentManager();
                contentManager.removeAllContents(true);
                var issuesPanel = new OffensiveIssuePannel(project);
                IssuesList issuesList = new IssuesList(project,finalSingleScanResponse);
                issuesPanel.setContent(issuesList.getIssuesList());
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


    private String sendToApi() throws Offensive360Exception {
        System.out.println(serverUrl+" "+authToken);
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(serverUrl + "app/api/ExternalScan");
        httpPost.addHeader("accept", "*/*");
        httpPost.addHeader("Authorization", "Bearer " + authToken);
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        builder.addTextBody("Name", "ide scan");
        builder.addTextBody("AllowMalwareScan", "false");
        builder.addTextBody("AllowLicenseScan", "false");
        builder.addTextBody("AllowDependencyScan", "false");
        builder.addTextBody("ExternalScanSourceType", "0");
        builder.addTextBody("KeepInvisibleAndDeletePostScan", "true");
        builder.addBinaryBody("FileSource", new File(zipDirName));
        System.out.println(zipDirName);
        HttpEntity multipart = builder.build();
        httpPost.setEntity(multipart);
        String responseString = null;
        try {
            response = httpClient.execute(httpPost);
            if (response == null) {
                throw new Offensive360Exception("Internal Error Please Contact Admin / Report Issue");
            }
            int statusCode = response.getStatusLine().getStatusCode();
            System.out.println(statusCode);
            System.out.println(response);
            if (statusCode == 401) {
                throw new Offensive360Exception("Please Provide Valid Token For Authentication or It May Expire");
            }

            if (statusCode == 200) {
                responseString = EntityUtils.toString(response.getEntity());
            }
            if(statusCode == 403){
                throw new Offensive360Exception("Access Forbidden to perform this action.Please contact admin");
            }
            if(statusCode==500){
                throw new Offensive360Exception("Internal Error.Please Contact admin");
            }
        }catch (Offensive360Exception ex){
            throw ex;
        }
        catch (IOException e) {
            throw new Offensive360Exception("Please Check Your Internet or Invalid Host");
        } catch (Exception e) {
            throw new Offensive360Exception("Error in Sending request, Try Again");
        }
        return responseString;
    }



    private void zipScannableFile() throws Offensive360Exception {
        String folderName = project.getBasePath();
        ZipDirectory zf = new ZipDirectory(scanStatus, project.getBasePath());
        String fileName = project.getName();
        File dir = null;
        if (folderName != null) {
            dir = new File(folderName);
        }
        zipDirName = folderName + "/" + fileName + ".zip";
        try {
            zf.zipDirectory(dir, zipDirName, fileName);
        } catch (FileNotFoundException e) {
            throw new Offensive360Exception("File Not Found While Zipping");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println(e.getLocalizedMessage());
            throw new Offensive360Exception("Zipping Files Failed Contact Admin/Report Issue");
        }
    }

    private void getServerDetails() throws Offensive360Exception {
        authCredentials = new AuthCredentials();
        String[] temp = authCredentials.checkCredentials().split("@");
        serverUrl = temp[1];
        authToken = temp[0];
    }


    private static void delFiles(File inputDirectory) {

        File[] allFiles = inputDirectory.listFiles();
        if(allFiles!=null) {
            for (File file : allFiles) {
                if (file.isDirectory()) {
                    delFiles(file);
                } else {
                    file.delete();
                }
            }
        }
        else{
            inputDirectory.delete();
        }
    }

    public void sendNotification(String title,String message){
        Notification notification = new Notification("O360", title, message, NotificationType.INFORMATION);
        Notifications.Bus.notify(notification, project);
    }

    private void changeLabel(String toString) {
        scanStatus.setText(toString);
        System.out.println(toString);
    }
}