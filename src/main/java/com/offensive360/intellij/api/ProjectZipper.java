package com.offensive360.intellij.api;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Utility for zipping project directories for scanning.
 * Uses a temporary file on disk instead of in-memory buffers to support
 * large codebases (1 GB+).
 *
 * Exclusion lists are kept in lockstep with the VS plugin's ScanCache.ExcludeExts
 * and the AS plugin's FileCollector to guarantee identical file sets across all
 * plugins for the same project.
 */
public class ProjectZipper {

    private static final long MAX_FILE_SIZE_BYTES = 50L * 1024 * 1024; // 50 MB per file

    // BLACKLIST approach (matches VS plugin's ScanCache.ExcludeExts exactly).
    // Any file whose extension is NOT in this set is considered scannable.
    private static final Set<String> EXCLUDED_EXTENSIONS = new HashSet<>(Arrays.asList(
        ".zip", ".dll", ".pdf", ".exe", ".ds_store", ".bak", ".tmp",
        ".mp3", ".mp4", ".wav", ".avi", ".mov", ".wmv", ".flv",
        ".bmp", ".gif", ".jpg", ".jpeg", ".png", ".psd", ".tif", ".tiff", ".ico", ".svg",
        ".jar", ".rar", ".7z", ".gz", ".tar", ".war", ".ear",
        ".pdb", ".class", ".iml", ".nupkg", ".vsix", ".aar",
        ".woff", ".woff2", ".ttf", ".otf", ".eot",
        ".db", ".sqlite", ".mdb", ".lock",
        ".sln", ".csproj", ".vbproj", ".vcxproj", ".fsproj", ".proj",
        ".suo", ".user", ".cache", ".snk", ".pfx", ".p12"
    ));

    // Folders to skip during file collection. Must match VS plugin's
    // ScanCache.ExcludeFolders and AS plugin's FileCollector.SKIP_DIRS exactly.
    private static final Set<String> EXCLUDED_FOLDERS = new HashSet<>(Arrays.asList(
        ".vs", "cvs", ".svn", ".hg", ".git", ".bzr", "bin", "obj",
        ".idea", ".vscode", "node_modules", "packages",
        "dist", "build", "out", "target", ".gradle", "__pycache__",
        ".sasto360", "testresults", "test-results", ".nuget",
        ".node_modules", ".pytest_cache", ".next", "coverage"
    ));

    /**
     * True if the given single-segment folder name should be skipped.
     * Combines a literal-set lookup with a pattern match for backup folders so
     * that backup1/backup2/Backup3 etc are automatically excluded.
     */
    private static boolean isExcludedFolder(String folderName) {
        if (folderName == null || folderName.isEmpty()) return false;
        String lower = folderName.toLowerCase();
        if (EXCLUDED_FOLDERS.contains(lower)) return true;
        if (lower.equals("backup") || lower.equals("backups")) return true;
        if (lower.startsWith("backup") && lower.length() > 6) {
            String suffix = lower.substring(6);
            for (char c : suffix.toCharArray()) {
                if (c < '0' || c > '9') return false;
            }
            return true;
        }
        return false;
    }

    /**
     * Zip a project directory to a temporary file on disk.
     * The caller is responsible for deleting the returned file when done.
     *
     * @param projectDir root directory to zip
     * @return a temporary {@link File} containing the zip archive
     * @throws IOException on I/O errors
     */
    public static File zipProjectToFile(File projectDir) throws IOException {
        File tempFile = File.createTempFile("o360_scan_", ".zip");
        tempFile.deleteOnExit(); // safety net

        try (FileOutputStream fos = new FileOutputStream(tempFile);
             BufferedOutputStream bos = new BufferedOutputStream(fos, 64 * 1024);
             ZipOutputStream zipOut = new ZipOutputStream(bos)) {

            Files.walkFileTree(projectDir.toPath(), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                    String folderName = dir.getFileName().toString();
                    if (isExcludedFolder(folderName)) {
                        return FileVisitResult.SKIP_SUBTREE;
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    String fileName = file.getFileName().toString();
                    String extension = getFileExtension(file);

                    // Skip excluded extensions
                    if (EXCLUDED_EXTENSIONS.contains(extension.toLowerCase())) {
                        return FileVisitResult.CONTINUE;
                    }

                    // Skip files larger than 50MB
                    try {
                        if (attrs.size() > MAX_FILE_SIZE_BYTES) {
                            return FileVisitResult.CONTINUE;
                        }
                    } catch (Exception e) {
                        // best-effort size check
                    }

                    Path relPath = projectDir.toPath().relativize(file);
                    ZipEntry entry = new ZipEntry(relPath.toString().replace("\\", "/"));
                    zipOut.putNextEntry(entry);
                    Files.copy(file, zipOut);
                    zipOut.closeEntry();

                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    // Skip files we cannot read
                    return FileVisitResult.CONTINUE;
                }
            });
        }

        return tempFile;
    }

    /**
     * Legacy in-memory method retained for backward compatibility with small projects.
     * Prefer {@link #zipProjectToFile(File)} for production use.
     */
    public static byte[] zipProject(File projectDir) throws IOException {
        File tempFile = zipProjectToFile(projectDir);
        try {
            return Files.readAllBytes(tempFile.toPath());
        } finally {
            tempFile.delete();
        }
    }

    private static String getFileExtension(Path path) {
        String fileName = path.getFileName().toString();
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(lastDot) : "";
    }
}
