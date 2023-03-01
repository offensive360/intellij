package utility;

import java.util.HashMap;
import java.util.Map;

public class FileExtension {
    final Map<String, Integer> fileExt = new HashMap<>();
    public FileExtension(){
        String DEFAULT_FILE_EXTENSIONS = ".DS_Store, .ipr, .iws, .bak, .tmp, .aac, .aif, .iff, .m3u, .mid, .mp3, .mpa, .ra, .wav, .wma, .3g2, .3gp, .asf, .asx, .avi, .flv, .mov, .mp4, .mpg, .rm, .swf, .vob, .wmv, .bmp, .gif, .jpg, .png, .psd, .tif, .swf, .jar, .zip, .rar, .exe, .dll, .pdb, .7z, .gz, .tar.gz, .tar, .gz, .ahtm, .ahtml, .fhtml, .hdm, .hdml, .hsql, .ht, .hta, .htc, .htd, .war, .ear, .htmls, .ihtml, .mht, .mhtm, .mhtml, .ssi, .stm, .stml, .ttml, .txn, .xhtm, .xhtml, .class, .iml, .gitignore";
        String[] traverse = DEFAULT_FILE_EXTENSIONS.split(", ");
        for(String files : traverse){
            fileExt.put(files, 1);
        }
    }
}
