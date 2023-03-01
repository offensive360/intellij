package utility;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.project.Project;
import icon.Offensive360Icons;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

public class Misc {

    public static String[] riskTypes = {"Low","Medium","High","Critical"};

    public static final Color[] color = new Color[]{
            new Color(0xB30BE60F, true),
            new Color(0xB3111EF0, true),
            new Color(0xCCF78206, true),
            new Color(0xB3E50B0B, true)
    };

    protected static Project project;
    public Misc(@NotNull Project project){
        this.project = project;
    }

    //Checks Internet
    public static void checkInternet() throws Offensive360Exception {
        try{
            URL url = new URL("http://www.google.com");
            URLConnection connection = url.openConnection();
            connection.connect();
        }
        catch (IOException ex) {
            throw  new Offensive360Exception("Please Check Your Internet Connection");
        }
    }

    //send Notifications

    public static void sendNotification(String title,String message){
        Notification notification = new Notification("O360", title, message, NotificationType.INFORMATION);
        notification.setIcon(Offensive360Icons.ICON_OFFENSIVE360);
        Notifications.Bus.notify(notification, project);
    }




    //json to object

    //http config


}
