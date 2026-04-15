package com.offensive360.intellij;

import com.intellij.ide.BrowserUtil;
import com.intellij.notification.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Plugin update notifier -- fetches the latest release directly from the
 * GitHub Releases API of offensive360/intellij. Behaviour:
 *   * fire-and-forget on startup, never blocks
 *   * throttled (24h cache + once per session) so we never spam GitHub
 *   * silent on any failure (404, network, parse, rate-limit)
 *   * ALL errors swallowed -- update notifications must NEVER break scans
 */
public class UpdateChecker implements StartupActivity.DumbAware {

    private static final String CURRENT_VERSION = "1.1.17";
    private static final String RELEASES_API_URL = "https://api.github.com/repos/offensive360/intellij/releases/latest";
    private static final String USER_AGENT = "Offensive360-IntelliJ-Plugin/" + CURRENT_VERSION;
    private static final long CACHE_TTL_MS = 24L * 60 * 60 * 1000;

    private static final AtomicLong lastCheckMs = new AtomicLong(0);
    private static final AtomicBoolean notifiedThisSession = new AtomicBoolean(false);
    private static volatile boolean forceShowUpToDate = false;

    @Override
    public void runActivity(@NotNull Project project) {
        checkAsync(project);
    }

    /**
     * Manual check -- bypasses throttle. Shows "up to date" if no update.
     */
    public static void forceCheckAsync(Project project) {
        lastCheckMs.set(0);
        notifiedThisSession.set(false);
        forceShowUpToDate = true;
        checkAsync(project);
    }

    /**
     * Fire-and-forget. Never throws. Never blocks the scan.
     */
    public static void checkAsync(Project project) {
        if (notifiedThisSession.get() && !forceShowUpToDate) return;
        long now = System.currentTimeMillis();
        if (now - lastCheckMs.get() < CACHE_TTL_MS && !forceShowUpToDate) return;
        lastCheckMs.set(now);

        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(RELEASES_API_URL).openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(10000);
                connection.setRequestProperty("User-Agent", USER_AGENT);
                connection.setRequestProperty("Accept", "application/vnd.github+json");

                int code = connection.getResponseCode();
                if (code < 200 || code >= 300) return;

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
                reader.close();

                String body = sb.toString();
                if (body.isEmpty()) return;

                // Simple JSON extraction without a full parser
                if (extractBool(body, "draft") || extractBool(body, "prerelease")) return;

                String tag = extractValue(body, "tag_name");
                if (tag == null || tag.isEmpty()) return;

                String latestVersion = tag.startsWith("v") || tag.startsWith("V") ? tag.substring(1) : tag;

                if (!isNewer(latestVersion, CURRENT_VERSION)) {
                    if (forceShowUpToDate) {
                        forceShowUpToDate = false;
                        ApplicationManager.getApplication().invokeLater(() -> {
                            javax.swing.JOptionPane.showMessageDialog(
                                null,
                                "You're up to date! You have the latest version (v" + CURRENT_VERSION + ").",
                                "Offensive 360",
                                javax.swing.JOptionPane.INFORMATION_MESSAGE);
                        });
                    }
                    return;
                }
                forceShowUpToDate = false;

                // Find the .zip asset download URL
                String downloadUrl = extractValue(body, "html_url");
                int assetsIdx = body.indexOf("\"assets\"");
                if (assetsIdx >= 0) {
                    String assetsSection = body.substring(assetsIdx);
                    // Find first .zip asset
                    int zipIdx = assetsSection.indexOf(".zip");
                    if (zipIdx >= 0) {
                        // Look for browser_download_url near the .zip
                        String nearZip = assetsSection.substring(0, zipIdx + 4);
                        String dlUrl = extractValue(nearZip, "browser_download_url");
                        if (dlUrl != null && !dlUrl.isEmpty()) {
                            downloadUrl = dlUrl;
                        }
                    }
                }

                String releaseNotes = extractValue(body, "body");
                if (releaseNotes != null && releaseNotes.length() > 600) {
                    releaseNotes = releaseNotes.substring(0, 600) + "...";
                }

                notifiedThisSession.set(true);
                final String finalDownloadUrl = downloadUrl;
                final String finalNotes = releaseNotes;
                final String finalVersion = latestVersion;

                ApplicationManager.getApplication().invokeLater(() -> {
                    Notification notification = new Notification(
                        "Offensive360",
                        "O360 SAST Update Available",
                        "Version " + finalVersion + " is available (you have v" + CURRENT_VERSION + ")."
                            + (finalNotes != null && !finalNotes.isEmpty() ? "\n" + finalNotes : ""),
                        NotificationType.INFORMATION
                    );
                    if (finalDownloadUrl != null && !finalDownloadUrl.isEmpty()) {
                        notification.addAction(NotificationAction.createSimpleExpiring(
                            "Download Update",
                            () -> BrowserUtil.browse(finalDownloadUrl)
                        ));
                    }
                    Notifications.Bus.notify(notification);
                });
            } catch (Exception e) {
                // Silent: update notifications must NEVER block scans
            }
        });
    }

    private static String extractValue(String json, String key) {
        String searchKey = "\"" + key + "\"";
        int keyIdx = json.indexOf(searchKey);
        if (keyIdx < 0) return null;
        int colonIdx = json.indexOf(":", keyIdx + searchKey.length());
        if (colonIdx < 0) return null;
        // Skip whitespace
        int pos = colonIdx + 1;
        while (pos < json.length() && json.charAt(pos) == ' ') pos++;
        if (pos >= json.length()) return null;
        if (json.charAt(pos) == '"') {
            int quoteEnd = json.indexOf("\"", pos + 1);
            if (quoteEnd < 0) return null;
            return json.substring(pos + 1, quoteEnd);
        }
        return null;
    }

    private static boolean extractBool(String json, String key) {
        String searchKey = "\"" + key + "\"";
        int keyIdx = json.indexOf(searchKey);
        if (keyIdx < 0) return false;
        int colonIdx = json.indexOf(":", keyIdx + searchKey.length());
        if (colonIdx < 0) return false;
        String after = json.substring(colonIdx + 1).trim();
        return after.startsWith("true");
    }

    private static boolean isNewer(String latest, String current) {
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
