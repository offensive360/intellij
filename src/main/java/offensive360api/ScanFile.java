//package offensive360api;
//
//import APIResponse.SingleScanResponse;
//import actions.MainMenu;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.intellij.notification.Notification;
//import com.intellij.notification.NotificationType;
//import com.intellij.notification.Notifications;
//import com.intellij.openapi.progress.ProgressIndicator;
//import com.intellij.openapi.progress.Task;
//import com.intellij.openapi.project.Project;
//import com.intellij.openapi.ui.Messages;
//import com.intellij.openapi.util.NlsContexts;
//import com.intellij.openapi.wm.ToolWindow;
//import com.intellij.openapi.wm.ToolWindowManager;
//import credentials.AuthCredentials;
//import issuesTab.APICallErrorContent;
//import issuesTab.AnalyzingIssueTabContent;
//import issuesTab.IssuesList;
//import org.apache.http.HttpEntity;
//import org.apache.http.client.methods.CloseableHttpResponse;
//import org.apache.http.client.methods.HttpPost;
//import org.apache.http.entity.mime.HttpMultipartMode;
//import org.apache.http.entity.mime.MultipartEntityBuilder;
//import org.apache.http.impl.client.CloseableHttpClient;
//import org.apache.http.impl.client.HttpClients;
//import org.apache.http.util.EntityUtils;
//import org.jetbrains.annotations.NotNull;
//import org.jetbrains.annotations.Nullable;
//import toolwindow.OffensiveIssuePannel;
//import utility.ZipDirectory;
//
//import javax.swing.*;
//import java.awt.*;
//import java.io.*;
//import java.io.IOException;
//import java.net.MalformedURLException;
//import java.net.URL;
//import java.net.URLConnection;
//import java.util.List;
//
//
//public class ScanFile extends Task.Backgroundable {
//
//    Project project;
//    protected AuthCredentials authCredentials;
//    MainMenu mainMenu = new MainMenu();
//    JLabel scanStatus;
//    AnalyzingIssueTabContent analyzingIssueTabContent;
//    APICallErrorContent apiCallErrorContent;
//    private boolean labelFlag = false;
//
//    public ScanFile(@Nullable Project project, @NlsContexts.ProgressTitle @NotNull String title, boolean canBeCancelled) {
//        super(project, title, canBeCancelled);
//        apiCallErrorContent = new APICallErrorContent();
//        this.project = project;
//    }
//
//    @Override
//    public @Nullable NotificationInfo notifyFinished() {
//        return super.notifyFinished();
//    }
//
//    @Override
//    public void onCancel() {
//        super.onCancel();
//        mainMenu.setStatusOfQueue();
//        apiCallErrorContent.showError(project,"Scan Cancelled By The User");
//    }
//
//    @Override
//    public void run(@NotNull ProgressIndicator indicator) {
//        {
//            indicator.setIndeterminate(false);
//            indicator.setText("Checking Internet Connection");
//            indicator.setFraction(0.10);
//            String zipDirName = null;
//
//            analyzingIssueTabContent = new AnalyzingIssueTabContent();
//            analyzingIssueTabContent.showAnalysing(project);
//            scanStatus = analyzingIssueTabContent.jLabel;
//            try {
//
//                try {
//                    checkInternet();
//                } catch (RuntimeException ex) {
//                    JOptionPane.showMessageDialog(scanStatus, "Please check your interent connnection");
//                    throw new Exception("No Internent Connection");
//                }
//
//                indicator.setText("Zipping Files");
//                indicator.setFraction(0.20);
//                zipDirName = null;
//
//
//                String folderName = project.getBasePath();
//                ZipDirectory zf = new ZipDirectory(scanStatus, project.getBaseDir().getPath());
//                String fileName = project.getName();
//                File dir = new File(folderName);
//                zipDirName = folderName + "/" + fileName + ".zip";
//                try {
//                    zf.zipDirectory(dir, zipDirName, fileName);
//                } catch (FileNotFoundException e) {
//                    changeLabel(e.getMessage());
//                    throw new RuntimeException(e);
//                } catch (IOException e) {
//                    changeLabel(e.getMessage());
//                    throw new RuntimeException(e);
//                }
//
//
//                String authToken = "";
//                String serverUrl = "";
//                try {
//                    //System.out.println("Inside"+" "+"creds");
//                    changeLabel("Checking Configuration..........");
//                    indicator.setText("Checking Configuration");
//                    indicator.setFraction(0.30);
//                    authCredentials = new AuthCredentials();
//                    if (authCredentials.getCredentials() != null) {
//                        authToken = authCredentials.getCredentials().getPasswordAsString();
//                        serverUrl = authCredentials.getCredentials().getUserName();
//                    } else if (authCredentials.getCredentials() == null) {
//                        throw new Exception("Error Getting Credentials");
//                    }
//                    //System.out.println("swinutl" + authToken + "\n" + serverUrl);
//                    if (authToken == null || serverUrl == null || authToken.isBlank() || serverUrl.isBlank()) {
//                        //System.out.println("swinf" + authToken + " " + serverUrl);
//                        throw new Exception("Please Configure Offensive360 to scan");
//                    } else {
//
//                        //System.out.println("apistrt" + authToken + "\n" + serverUrl);
//                        changeLabel("Scanning..........");
//                        indicator.setText("Scanning");
//                        indicator.setFraction(0.40);
//                        CloseableHttpClient httpClient = HttpClients.createDefault();
//                        HttpPost httpPost = new HttpPost(serverUrl + "app/api/ExternalScan");
//                        httpPost.addHeader("accept", "*/*");
//                        httpPost.addHeader("Authorization", "Bearer " + authToken);
//                        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
//                        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
//                        builder.addTextBody("Name", "ide scan");
//                        builder.addTextBody("AllowMalwareScan", "false");
//                        builder.addTextBody("AllowLicenseScan", "false");
//                        builder.addTextBody("AllowDependencyScan", "false");
//                        builder.addTextBody("ExternalScanSourceType", "0");
//                        builder.addTextBody("KeepInvisibleAndDeletePostScan", "true");
//                        builder.addBinaryBody("FileSource", new File(zipDirName));
//                        HttpEntity multipart = builder.build();
//                        httpPost.setEntity(multipart);
//                        CloseableHttpResponse response = null;
//                        String responseString;
//                        try {
//                            try {
//                                //System.out.println("Inside"+" "+"reqstart");
//                                response = httpClient.execute(httpPost);
//                                indicator.setFraction(0.50);
//                            } catch (IOException e) {
//                                throw new RuntimeException("Check Your internet connection or credentials");
//                            } catch (Exception e) {
//                                response = null;
//                                throw new RuntimeException("Error in Sending request, Try Again");
//                            }
//                        } catch (Exception e) {
//                            throw e;
//                        } finally {
//                            indicator.setText("Finished");
//                            indicator.setFraction(1.0);
//                            assert response != null;
//                            System.out.println("Inside Entity" + response.toString());
//                            File delFile = new File(zipDirName);
//                            if (delFile.exists()) {
//                                System.out.println(delFile.isDirectory());
//                                delFiles(delFile);
//                            }
//                            if (response != null) {
//                                try {
//                                    indicator.setText("FInished");
//                                    indicator.setFraction(1.0);
//                                    if(response.getStatusLine().getStatusCode()==401){
//                                        throw  new RuntimeException("Please Provide Valid Token or it may expired");
//                                    }
//                                    responseString = EntityUtils.toString(response.getEntity());
//                                    response.close();
//                                } catch (IOException e) {
//                                    throw new RuntimeException(e.getMessage());
//                                } catch (Exception e) {
//                                    throw new RuntimeException("Something Bad Occured.. Please contact admin");
//                                }
//
//                                int statusCode = response.getStatusLine().getStatusCode();
//
//                                ObjectMapper objectMapper = new ObjectMapper();
//                                if (statusCode == 200) {
//                                    SingleScanResponse singleScanResponse = null;
//                                    try {
//                                        singleScanResponse = objectMapper.readValue(responseString, SingleScanResponse.class);
//                                    } catch (IOException e) {
//                                        throw new RuntimeException(e.getMessage().toString());
//                                    }
//                                    SingleScanResponse finalSingleScanResponse = singleScanResponse;
//                                    EventQueue.invokeLater(() -> {
//                                        ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow("Offensive360");
//                                        assert toolWindow != null;
//                                        var contentManager = toolWindow.getContentManager();
//                                        contentManager.removeAllContents(true);
//                                        var issuesPanel = new OffensiveIssuePannel(project);
//                                        IssuesList issuesList = new IssuesList(project,finalSingleScanResponse);
//                                        issuesPanel.setContent(issuesList.getIssuesList());
//                                        var issuesContent = contentManager.getFactory()
//                                                .createContent(
//                                                        issuesPanel,
//                                                        "Issues",
//                                                        false);
//                                        issuesContent.setCloseable(false);
//                                        contentManager.addDataProvider(issuesPanel);
//                                        contentManager.addContent(issuesContent);
//                                    });
//                                } else if (statusCode==401) {
//                                    Messages.showErrorDialog("Enter Valid Token","Authorization Error");
//                                }
//
//                            } else {
//                                APICallErrorContent apiCallErrorContent = new APICallErrorContent();
//                                apiCallErrorContent.showError(project, "Some Error occurred calling API, check the Offensive 360 settings or contact admin.");
//                            }
//                            //System.out.println("Task Completed");
//                        }
//                    }
//                } catch (NullPointerException exception) {
//                    throw new RuntimeException("Internal Issue while scannig please try again..");
//                } catch (Exception e) {
//                    throw new RuntimeException(e.getMessage());
//                }
//            } catch (Exception e) {
//
//                System.out.println(e);
//                changeLabel(e.getMessage());
//                com.intellij.notification.Notification notification = new Notification("Off", "My Title", "Error Occured", NotificationType.INFORMATION);
//                Notifications.Bus.notify(notification, project);
//                labelFlag = true;
//            } finally {
//                indicator.setFraction(1.0);
//                mainMenu.setStatusOfQueue();
//                if (labelFlag) {
//                    changeLabel("Problem while running scan..Please ty again");
//                    Notification notification = new Notification("Off", "My Title", "Some Error occurred calling API, check the Offensive 360 settings or contact admin.", NotificationType.INFORMATION);
//                    Notifications.Bus.notify(notification, project);
//                    APICallErrorContent apiCallErrorContent = new APICallErrorContent();
//                    apiCallErrorContent.showError(project, "Some Error occurred calling API, check the Offensive 360 settings or contact admin.");
//                }
//                if(zipDirName!=null) {
//                    File delFile = new File(zipDirName);
//                    if (delFile.exists()) {
//                        System.out.println(delFile.isDirectory());
//                        delFiles(delFile);
//                    }
//                }
//            }
//        }
//    }
//
//    private static void delFiles(File inputDirectory) {
//
//        File[] allFiles = inputDirectory.listFiles();
//        System.out.println(allFiles);
//        if(allFiles!=null) {
//            for (File file : allFiles) {
//                if (file.isDirectory()) {
//                    delFiles(file);
//                } else {
//                    System.out.println(file.getPath());
//                    file.delete();
//                }
//            }
//        }
//        else{
//            System.out.println(inputDirectory);
//            inputDirectory.delete();
//        }
//    }
//
//    private void checkInternet() {
//        try{
//            URL url = new URL("http://www.google.com");
//            URLConnection connection = url.openConnection();
//            connection.connect();
//        }
//        catch (MalformedURLException e) {
//            throw new RuntimeException(e);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    private void changeLabel(String toString) {
//        scanStatus.setText(toString);
//        System.out.println(toString);
//    }
//}