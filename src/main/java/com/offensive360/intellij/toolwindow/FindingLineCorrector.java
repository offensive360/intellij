package com.offensive360.intellij.toolwindow;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.offensive360.intellij.models.LangVulnerability;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Repairs line numbers on findings whose lineNumber drifts from the actual
 * snippet location in the local source. For each finding we try to locate
 * the codeSnippet in the file content; if found at a different line, the
 * finding's lineNumber is rewritten. If the snippet cannot be located in
 * the file at all, the finding is dropped (we can't navigate accurately).
 */
public final class FindingLineCorrector {

    public static final class Result {
        public final List<LangVulnerability> findings;
        public final int corrected;
        public final int dropped;
        Result(List<LangVulnerability> findings, int corrected, int dropped) {
            this.findings = findings;
            this.corrected = corrected;
            this.dropped = dropped;
        }
    }

    private FindingLineCorrector() {}

    public static Result apply(List<LangVulnerability> findings, Project project,
                               Function<LangVulnerability, VirtualFile> resolver) {
        if (findings == null || findings.isEmpty()) return new Result(findings == null ? List.of() : findings, 0, 0);
        return ApplicationManager.getApplication().runReadAction((com.intellij.openapi.util.Computable<Result>) () -> {
            int corrected = 0, dropped = 0;
            List<LangVulnerability> out = new ArrayList<>(findings.size());
            for (LangVulnerability f : findings) {
                int v = process(f, resolver);
                if (v == 0) out.add(f);                           // keep
                else if (v == 1) { corrected++; out.add(f); }     // corrected (in-place)
                else dropped++;                                    // drop
            }
            return new Result(out, corrected, dropped);
        });
    }

    /** 0 = keep as-is, 1 = corrected (mutated in place), -1 = drop. */
    private static int process(LangVulnerability f, Function<LangVulnerability, VirtualFile> resolver) {
        String snippet = f.getCodeSnippet();
        if (snippet == null) return 0;
        snippet = snippet.trim();
        if (snippet.length() < 8) return 0;

        VirtualFile vf = resolver.apply(f);
        if (vf == null) return 0;
        String text;
        try { text = new String(vf.contentsToByteArray(), StandardCharsets.UTF_8); }
        catch (Exception e) { return 0; }
        String[] lines = text.split("\n", -1);
        if (lines.length == 0) return 0;

        String needle = normalize(snippet);
        if (needle.length() < 8) return 0;

        int reportedIdx = Math.max(0, f.getLineNo() - 1);
        if (reportedIdx < lines.length && contains(lines[reportedIdx], needle)) return 0;

        int foundIdx = locate(lines, needle);
        if (foundIdx < 0) return -1;
        int newLine = foundIdx + 1;
        if (newLine == f.getLineNo()) return 0;
        f.setLineNo(newLine);
        f.setLineNumber(String.valueOf(newLine));
        return 1;
    }

    private static String normalize(String s) {
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (!Character.isWhitespace(c)) sb.append(Character.toLowerCase(c));
        }
        return sb.toString();
    }

    private static boolean contains(String line, String normNeedle) {
        String hay = normalize(line);
        if (hay.isEmpty()) return false;
        String shortNeedle = normNeedle.length() > 24 ? normNeedle.substring(0, 24) : normNeedle;
        return hay.contains(shortNeedle) || (hay.length() >= 8 && normNeedle.contains(hay));
    }

    private static int locate(String[] lines, String normNeedle) {
        String shortNeedle = normNeedle.length() > 24 ? normNeedle.substring(0, 24) : normNeedle;
        for (int i = 0; i < lines.length; i++) {
            if (normalize(lines[i]).contains(shortNeedle)) return i;
        }
        return -1;
    }
}
