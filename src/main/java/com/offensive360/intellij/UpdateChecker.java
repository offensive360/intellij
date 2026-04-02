package com.offensive360.intellij;

import com.intellij.ide.BrowserUtil;
import com.intellij.notification.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Checks for plugin updates on startup by querying a remote manifest.
 */
public class UpdateChecker implements StartupActivity.DumbAware {

    private static final String CURRENT_VERSION = "1.3.0";
    private static final String UPDATE_URL = "https://raw.githubusercontent.com/offensive360/intellij/main/update-manifest.json";

    @Override
    public void runActivity(@NotNull Project project) {
        new Thread(() -> checkForUpdate(), "O360-UpdateCheck").start();
    }

    private void checkForUpdate() {
        try {
            URL url = new URL(UPDATE_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(10000);
            conn.setRequestMethod("GET");

            if (conn.getResponseCode() != 200) return;

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            reader.close();

            parseAndNotify(sb.toString());
        } catch (Exception e) {
            // Silently ignore — update check is best-effort
        }
    }

    private void parseAndNotify(String json) {
        try {
            int asStart = json.indexOf("\"androidStudio\"");
            if (asStart < 0) return;
            String asSection = json.substring(asStart, json.indexOf("}", asStart) + 1);

            String latestVersion = extractValue(asSection, "\"version\"");
            String downloadUrl = extractValue(asSection, "\"downloadUrl\"");
            String releaseNotes = extractValue(asSection, "\"releaseNotes\"");

            if (latestVersion != null && !latestVersion.equals(CURRENT_VERSION) && isNewer(latestVersion, CURRENT_VERSION)) {
                Notification notification = new Notification(
                    "Offensive360",
                    "O360 SAST Update Available",
                    "Version " + latestVersion + " is available. " + (releaseNotes != null ? releaseNotes : ""),
                    NotificationType.INFORMATION
                );
                if (downloadUrl != null) {
                    final String dlUrl = downloadUrl;
                    notification.addAction(NotificationAction.createSimpleExpiring(
                        "Download Update",
                        () -> BrowserUtil.browse(dlUrl)
                    ));
                }
                Notifications.Bus.notify(notification);
            }
        } catch (Exception e) {
            // Silently ignore
        }
    }

    private String extractValue(String json, String key) {
        int keyIdx = json.indexOf(key);
        if (keyIdx < 0) return null;
        int colonIdx = json.indexOf(":", keyIdx);
        if (colonIdx < 0) return null;
        int quoteStart = json.indexOf("\"", colonIdx + 1);
        if (quoteStart < 0) return null;
        int quoteEnd = json.indexOf("\"", quoteStart + 1);
        if (quoteEnd < 0) return null;
        return json.substring(quoteStart + 1, quoteEnd);
    }

    private boolean isNewer(String latest, String current) {
        try {
            String[] l = latest.split("\\.");
            String[] c = current.split("\\.");
            for (int i = 0; i < Math.min(l.length, c.length); i++) {
                int lv = Integer.parseInt(l[i]);
                int cv = Integer.parseInt(c[i]);
                if (lv > cv) return true;
                if (lv < cv) return false;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }
}
