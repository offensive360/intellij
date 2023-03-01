package utility;

import java.util.HashMap;
import java.util.Map;

public class FolderExtension {
    final Map<String, Integer> folderExt = new HashMap<>();
    public FolderExtension(){
        String DEFAULT_FOLDER_EXCLUSIONS = "cvs, .svn, .hg, .git, .bzr, bin, obj, backup, .idea, .vscode, node_modules, .gitignore, README, .gradle, build";
        String[] traverse = DEFAULT_FOLDER_EXCLUSIONS.split(", ");
        for(String folders : traverse){
            folderExt.put(folders, 1);
        }

    }
}
