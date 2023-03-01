package utility;

import com.intellij.openapi.ui.Messages;
import issuesTab.IssuesList;

import javax.swing.*;
import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Author:Shyam.Vegi
 * This class is to zip the file by filtering extensions and folders not needed.
 * Suppressed File and Configuration file also initialized here to provide support
 * to suppression Action
 */
public class ZipDirectory {

    private static JLabel scanStatus = new JLabel("Zipping Files");
    private final FileExtension fileExtension;
    private final FolderExtension folderExtension;
    String basePath;
    private ZipOutputStream zipOutputStream;

    public ZipDirectory(JLabel scanStatus,String filePath){
        this.scanStatus = scanStatus;
        fileExtension = new FileExtension();
        folderExtension = new FolderExtension();
        basePath = filePath;
    }

    //Skips Files With given Extensions to be excluded
    public boolean skipFileExtensions(File file){

        for (var entry : fileExtension.fileExt.entrySet()) {
            if(file.getName().endsWith(entry.getKey())){
                return true;
            }
        }
        return false;
    }

    //Skips folders with given exclusions
    public boolean skipFolderExtensions(File folderName){
        for (var entry : folderExtension.folderExt.entrySet()) {
            if(folderName.getName().endsWith(entry.getKey())){
                return true;
            }
        }
        return false;
    }

    // Suppressed Files are to be checked everytime to avoid displaying them to user
    private void createSuppressedFiles() {
        try{
            File folder = new File(basePath+File.separator+".360");
            File file = new File(folder.getPath()+File.separator+".360ignore");
            Map<String, Set<String>> suppressedFiles = new HashMap<>();
            if(folder.exists() || folder.mkdir()){
                if(file.exists() || file.createNewFile()){
                    BufferedReader reader = new BufferedReader(new FileReader(file.getPath()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        String[] temp = line.split("@",2);
                        if(temp.length==2) {
                            String fileName = temp[0];
                            String lineColumn = temp[1];
                            suppressedFiles.putIfAbsent(fileName, new HashSet<>());
                            suppressedFiles.get(fileName).add(lineColumn);
                        }
                    }
                }
            }
            else{
                SwingUtilities.invokeLater(()->Messages.showInfoMessage("Failed to create suppression",".360 Error"));
            }
            IssuesList.SUPPRESSED_FILES=suppressedFiles;
        }
        catch (IOException ioex){
            Messages.showErrorDialog("Zipping failed",ioex.getLocalizedMessage());
        }
        catch (SecurityException sx){
            Messages.showErrorDialog(sx.getMessage(),"Offensive360 Scan");
        }
        catch (Exception e){
            System.out.println(e.getLocalizedMessage());
        }
    }


    public void zipDirectory(File dir, String zipDirName, String fileName) throws IOException {
        try {
            File outputZip = new File(zipDirName);

            outputZip.getParentFile().mkdirs();
            List listFiles = new ArrayList();

            //Populate all files...iterate through directories/subdirectories...
            //recursively files:\n",
            listFiles(listFiles, dir);

            //Create zip output stream to zip files
             zipOutputStream = new ZipOutputStream(
                    new FileOutputStream(outputZip));

            //Create zip files by recursively iterating through directories
            //and sub directories....
            scanStatus.setText("\nZipped file at location: " + outputZip.getCanonicalPath());
            createZipFile(listFiles, dir, zipOutputStream, fileName);
        }
        catch (IOException ex){
            if(zipOutputStream!=null){
                zipOutputStream.flush();
                zipOutputStream.closeEntry();
                zipOutputStream.close();
            }
            throw new IOException(ex.getMessage());
        }
        createSuppressedFiles();
    }

    private void createZipFile(List<File> listFiles, File inputDirectory, ZipOutputStream zipOutputStream, String fileName) throws IOException {

        for (File file : listFiles) {
            String fileString = fileName + ".zip";
            if (file.getName().equals(fileString) || skipFolderExtensions(file) || skipFileExtensions(file)) {
                continue;
            }
            String filePath = file.getCanonicalPath();
            int lengthDirectoryPath = inputDirectory.getCanonicalPath().length();
            int lengthFilePath = file.getCanonicalPath().length();

            //Get path of files relative to input directory.
            String zipFilePath = filePath.substring(lengthDirectoryPath + 1, lengthFilePath);

            ZipEntry zipEntry = new ZipEntry(zipFilePath);
            zipOutputStream.putNextEntry(zipEntry);

            FileInputStream inputStream = new FileInputStream(file);
            byte[] bytes = new byte[1024];
            int length;
            if(!file.canRead())continue;
            System.out.println(file);
            while ((length = inputStream.read(bytes)) >= 0) {
                zipOutputStream.write(bytes, 0, length);
            }
            zipOutputStream.closeEntry();
            scanStatus.setText("Zipping file: "+filePath);
        }
        zipOutputStream.close();
    }

    //Get list of all files recursively by iterating through subdirectories
    private void listFiles(List listFiles, File inputDirectory) {

        File[] allFiles = inputDirectory.listFiles();
        assert allFiles != null;
        for (File file : allFiles) {
            if (file.isDirectory() && !skipFolderExtensions(file)) {
                listFiles(listFiles, file);
            } else if(!skipFileExtensions(file)){
                listFiles.add(file);
            }
        }
    }
}