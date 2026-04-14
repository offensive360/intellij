package com.offensive360.intellij.toolwindow;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.ui.table.JBTable;
import com.offensive360.intellij.kb.VulnerabilityKnowledgeBase;
import com.offensive360.intellij.models.DepVulnerability;
import com.offensive360.intellij.models.LangVulnerability;
import com.offensive360.intellij.models.LicenseIssue;
import com.offensive360.intellij.models.MalwareResult;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.*;

/**
 * Main panel for displaying Offensive360 scan results in the IDE tool window.
 * Features a split-pane layout with findings table on top and a tabbed detail
 * view (Details / How to Fix / References) on the bottom, matching the AS plugin.
 */
public class ScanResultsPanel extends JPanel implements ScanResultsService.ChangeListener {

    private final Project project;
    private final CardLayout cardLayout;
    private final JPanel cardPanel;

    private static final String CARD_EMPTY = "empty";
    private static final String CARD_RESULTS = "results";

    // Findings table
    private final JBTable langTable;
    private final LangTableModel langModel;

    // Status label
    private final JBLabel statusLabel;

    // Detail panel components
    private JLabel detailTitle;
    private JTabbedPane detailTabs;

    // Details tab
    private JLabel severityBadge;
    private JLabel detailFileLabel;
    private JTextArea descriptionArea;
    private JTextArea impactArea;
    private JTextArea detailCodeArea;

    // How to Fix tab
    private JTextArea recommendationArea;
    private JTextArea vulnerableCodeArea;
    private JTextArea secureCodeArea;

    // References tab
    private JPanel referencesPanel;

    // Mono font for code
    private static final Font MONO_FONT = new Font(Font.MONOSPACED, Font.PLAIN, 12);

    // Colors
    private static final Color CODE_BG = new Color(40, 42, 48);
    private static final Color CODE_FG = new Color(212, 212, 212);
    private static final Color FIX_BG = new Color(30, 50, 80);
    private static final Color FIX_FG = new Color(180, 210, 255);
    private static final Color VULN_CODE_BG = new Color(60, 30, 30);
    private static final Color VULN_CODE_FG = new Color(255, 180, 180);
    private static final Color SECURE_CODE_BG = new Color(30, 60, 35);
    private static final Color SECURE_CODE_FG = new Color(180, 255, 180);

    public ScanResultsPanel(Project project) {
        super(new BorderLayout());
        this.project = project;

        // Card layout: empty state vs results
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        // -- Empty state --
        JPanel emptyPanel = new JPanel(new GridBagLayout());
        JBLabel emptyLabel = new JBLabel("Run a scan to see results here");
        emptyLabel.setForeground(UIManager.getColor("Label.disabledForeground"));
        emptyPanel.add(emptyLabel);
        cardPanel.add(emptyPanel, CARD_EMPTY);

        // -- Results state --
        JPanel resultsPanel = new JPanel(new BorderLayout());

        // Status bar
        statusLabel = new JBLabel("No findings");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        resultsPanel.add(statusLabel, BorderLayout.NORTH);

        // Split pane: table on top, detail on bottom
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.45);

        // Findings table
        langModel = new LangTableModel();
        langTable = new JBTable(langModel);
        langTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        langTable.setRowHeight(24);
        langTable.setShowGrid(false);
        langTable.setIntercellSpacing(new Dimension(0, 0));
        langTable.getTableHeader().setReorderingAllowed(false);
        langTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Column widths
        langTable.getColumnModel().getColumn(0).setMaxWidth(80);
        langTable.getColumnModel().getColumn(0).setMinWidth(60);
        langTable.getColumnModel().getColumn(0).setCellRenderer(new SeverityRenderer());
        langTable.getColumnModel().getColumn(3).setMaxWidth(60);
        langTable.getColumnModel().getColumn(3).setMinWidth(40);

        // Selection listener for detail view
        langTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = langTable.getSelectedRow();
                if (row >= 0) {
                    showDetail(langModel.getRow(row));
                }
            }
        });

        // Double-click navigation
        langTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && langTable.getSelectedRow() >= 0) {
                    navigateToLangVuln();
                }
            }
        });

        // Context menu
        langTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) { handlePopup(e); }
            @Override
            public void mouseReleased(MouseEvent e) { handlePopup(e); }

            private void handlePopup(MouseEvent e) {
                if (!e.isPopupTrigger()) return;
                int row = langTable.rowAtPoint(e.getPoint());
                if (row < 0) return;
                langTable.setRowSelectionInterval(row, row);

                JPopupMenu menu = new JPopupMenu();
                JMenuItem copyItem = new JMenuItem("Copy Row");
                copyItem.addActionListener(ev -> copyRowToClipboard(langTable, row));
                menu.add(copyItem);
                menu.show(langTable, e.getX(), e.getY());
            }
        });

        splitPane.setTopComponent(new JScrollPane(langTable));

        // Detail panel
        JPanel detailContainer = buildDetailPanel();
        splitPane.setBottomComponent(detailContainer);

        resultsPanel.add(splitPane, BorderLayout.CENTER);

        cardPanel.add(resultsPanel, CARD_RESULTS);
        add(cardPanel, BorderLayout.CENTER);

        // Start with empty state
        cardLayout.show(cardPanel, CARD_EMPTY);

        // Listen for result changes
        ScanResultsService service = ScanResultsService.getInstance(project);
        service.addChangeListener(this);
        refreshFromService(service);
    }

    // -- Detail panel builder --

    private JPanel buildDetailPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        detailTitle = new JLabel("Select a finding for details");
        detailTitle.setFont(detailTitle.getFont().deriveFont(Font.BOLD, 14f));
        detailTitle.setBorder(BorderFactory.createEmptyBorder(4, 4, 8, 4));
        panel.add(detailTitle, BorderLayout.NORTH);

        detailTabs = new JTabbedPane(JTabbedPane.TOP);
        detailTabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

        detailTabs.addTab("Details", buildDetailsTab());
        detailTabs.addTab("How to Fix", buildFixTab());
        detailTabs.addTab("References", buildReferencesTab());

        panel.add(detailTabs, BorderLayout.CENTER);
        return panel;
    }

    private JComponent buildDetailsTab() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        // Severity badge + file info row
        JPanel headerRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        headerRow.setOpaque(false);
        headerRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        headerRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        severityBadge = new JLabel();
        severityBadge.setOpaque(true);
        severityBadge.setFont(severityBadge.getFont().deriveFont(Font.BOLD, 11f));
        severityBadge.setForeground(Color.WHITE);
        severityBadge.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
        severityBadge.setVisible(false);
        headerRow.add(severityBadge);

        detailFileLabel = new JLabel();
        detailFileLabel.setForeground(Color.GRAY);
        detailFileLabel.setFont(detailFileLabel.getFont().deriveFont(Font.PLAIN, 11f));
        headerRow.add(detailFileLabel);

        panel.add(headerRow);
        panel.add(Box.createVerticalStrut(10));

        // Description
        panel.add(createSectionLabel("Description"));
        panel.add(Box.createVerticalStrut(4));
        descriptionArea = createReadOnlyTextArea();
        descriptionArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(descriptionArea);
        panel.add(Box.createVerticalStrut(12));

        // Impact
        panel.add(createSectionLabel("Impact"));
        panel.add(Box.createVerticalStrut(4));
        impactArea = createReadOnlyTextArea();
        impactArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(impactArea);
        panel.add(Box.createVerticalStrut(12));

        // Affected Code
        panel.add(createSectionLabel("Affected Code"));
        panel.add(Box.createVerticalStrut(4));
        detailCodeArea = createCodeArea(CODE_BG, CODE_FG);
        detailCodeArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(wrapCodeArea(detailCodeArea, CODE_BG));
        panel.add(Box.createVerticalStrut(8));

        panel.add(Box.createVerticalGlue());

        JScrollPane scroll = new JScrollPane(panel);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        return scroll;
    }

    private JComponent buildFixTab() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        // Recommendation
        JLabel recLabel = createSectionLabel("Recommendation");
        recLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(recLabel);
        panel.add(Box.createVerticalStrut(4));

        recommendationArea = createReadOnlyTextArea();
        recommendationArea.setBackground(FIX_BG);
        recommendationArea.setForeground(FIX_FG);
        recommendationArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 3, 0, 0, new Color(80, 140, 220)),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        recommendationArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(recommendationArea);
        panel.add(Box.createVerticalStrut(16));

        // Vulnerable code
        JLabel vulnLabel = createSectionLabel("Vulnerable Code");
        vulnLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(vulnLabel);
        panel.add(Box.createVerticalStrut(4));

        vulnerableCodeArea = createCodeArea(VULN_CODE_BG, VULN_CODE_FG);
        vulnerableCodeArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(wrapCodeArea(vulnerableCodeArea, VULN_CODE_BG));
        panel.add(Box.createVerticalStrut(16));

        // Secure code pattern
        JLabel secureLabel = createSectionLabel("Secure Code Pattern");
        secureLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(secureLabel);
        panel.add(Box.createVerticalStrut(4));

        secureCodeArea = createCodeArea(SECURE_CODE_BG, SECURE_CODE_FG);
        secureCodeArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(wrapCodeArea(secureCodeArea, SECURE_CODE_BG));
        panel.add(Box.createVerticalStrut(8));

        panel.add(Box.createVerticalGlue());

        JScrollPane scroll = new JScrollPane(panel);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        return scroll;
    }

    private JComponent buildReferencesTab() {
        referencesPanel = new JPanel();
        referencesPanel.setLayout(new BoxLayout(referencesPanel, BoxLayout.Y_AXIS));
        referencesPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JScrollPane scroll = new JScrollPane(referencesPanel);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        return scroll;
    }

    // -- UI helper methods --

    private JLabel createSectionLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 12f));
        label.setForeground(new Color(180, 180, 180));
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 2, 0));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JTextArea createReadOnlyTextArea() {
        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(area.getFont().deriveFont(Font.PLAIN, 12f));
        area.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        area.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        return area;
    }

    private JTextArea createCodeArea(Color bg, Color fg) {
        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setLineWrap(false);
        area.setFont(MONO_FONT);
        area.setBackground(bg);
        area.setForeground(fg);
        area.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        area.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        return area;
    }

    private JComponent wrapCodeArea(JTextArea codeArea, Color bg) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(bg);
        wrapper.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 63, 70), 1),
            BorderFactory.createEmptyBorder()
        ));
        JScrollPane scroll = new JScrollPane(codeArea);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setPreferredSize(new Dimension(0, 120));
        scroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
        wrapper.add(scroll, BorderLayout.CENTER);
        wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
        return wrapper;
    }

    private JLabel createClickableLink(String text, String url) {
        JLabel link = new JLabel("<html><a href='" + url + "' style='color:#589df6;'>" + text + "</a></html>");
        link.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        link.setAlignmentX(Component.LEFT_ALIGNMENT);
        link.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
        link.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(new URI(url));
                } catch (Exception ex) {
                    // Silently fail
                }
            }
        });
        return link;
    }

    // -- Show detail for a finding --

    private void showDetail(LangVulnerability vuln) {
        if (vuln == null) return;

        String title = vuln.getTitle() != null ? vuln.getTitle() : (vuln.getType() != null ? vuln.getType() : "");
        String type = vuln.getType() != null ? vuln.getType() : "";
        detailTitle.setText(title);

        // Details tab
        String riskLevel = vuln.getRiskLevel() != null ? vuln.getRiskLevel() : "Medium";
        severityBadge.setText(" " + riskLevel + " ");
        severityBadge.setBackground(getSeverityColor(riskLevel));
        severityBadge.setVisible(true);

        String fileName = vuln.getFileName() != null ? vuln.getFileName() : (vuln.getFilePath() != null ? vuln.getFilePath() : "");
        detailFileLabel.setText(fileName + " : " + vuln.getLineNo());

        // KB lookup
        VulnerabilityKnowledgeBase.KBEntry kbEntry = VulnerabilityKnowledgeBase.lookupVuln(type);
        if (kbEntry == null && title != null) {
            kbEntry = VulnerabilityKnowledgeBase.lookupVuln(title);
        }

        // Description
        String description = vuln.getVulnerability();
        if ((description == null || description.isEmpty()) && kbEntry != null) {
            description = kbEntry.getDescription();
        }
        descriptionArea.setText(description != null && !description.isEmpty() ? description : "No description available.");
        descriptionArea.setCaretPosition(0);

        // Impact
        String impact = vuln.getEffect();
        if ((impact == null || impact.isEmpty()) && kbEntry != null) {
            impact = kbEntry.getImpact();
        }
        impactArea.setText(impact != null && !impact.isEmpty() ? impact : "No impact information available.");
        impactArea.setCaretPosition(0);

        // Code snippet
        String code = vuln.getCodeSnippet();
        detailCodeArea.setText(code != null && !code.isEmpty() ? code : "No code snippet available.");
        detailCodeArea.setCaretPosition(0);

        // How to Fix tab
        String recommendation = vuln.getRecommendation();
        if ((recommendation == null || recommendation.isEmpty()) && kbEntry != null) {
            recommendation = kbEntry.getHowToFix();
        }
        recommendationArea.setText(recommendation != null && !recommendation.isEmpty() ? recommendation : "No recommendation available.");
        recommendationArea.setCaretPosition(0);

        vulnerableCodeArea.setText(code != null && !code.isEmpty() ? code : "No vulnerable code snippet available.");
        vulnerableCodeArea.setCaretPosition(0);

        // Derive secure code hint from recommendation
        if (recommendation != null && !recommendation.isEmpty()) {
            secureCodeArea.setText("// Secure pattern based on recommendation:\n// " + recommendation.replace("\n", "\n// "));
        } else {
            secureCodeArea.setText("// No secure code pattern available.");
        }
        secureCodeArea.setCaretPosition(0);

        // References tab
        referencesPanel.removeAll();

        JLabel refTitle = createSectionLabel("Related References");
        refTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        referencesPanel.add(refTitle);
        referencesPanel.add(Box.createVerticalStrut(8));

        // Extract CWE references
        Set<String> cweMatches = new LinkedHashSet<>();
        Pattern cwePattern = Pattern.compile("CWE-\\d+", Pattern.CASE_INSENSITIVE);
        extractCWEs(cwePattern, vuln.getVulnerability(), cweMatches);
        extractCWEs(cwePattern, title, cweMatches);
        extractCWEs(cwePattern, type, cweMatches);
        extractCWEs(cwePattern, vuln.getEffect(), cweMatches);
        extractCWEs(cwePattern, vuln.getRecommendation(), cweMatches);

        // Map common vulnerability types to CWE if none found
        if (cweMatches.isEmpty()) {
            String combined = (type + " " + title + " " + (vuln.getVulnerability() != null ? vuln.getVulnerability() : "")).toLowerCase();
            Map<String, String> cweMap = new LinkedHashMap<>();
            cweMap.put("sql injection", "CWE-89");
            cweMap.put("sqli", "CWE-89");
            cweMap.put("xss", "CWE-79");
            cweMap.put("cross-site scripting", "CWE-79");
            cweMap.put("command injection", "CWE-78");
            cweMap.put("os command", "CWE-78");
            cweMap.put("path traversal", "CWE-22");
            cweMap.put("directory traversal", "CWE-22");
            cweMap.put("hardcoded", "CWE-798");
            cweMap.put("hard-coded", "CWE-798");
            cweMap.put("insecure deserialization", "CWE-502");
            cweMap.put("deseriali", "CWE-502");
            cweMap.put("xxe", "CWE-611");
            cweMap.put("xml external", "CWE-611");
            cweMap.put("ssrf", "CWE-918");
            cweMap.put("server-side request", "CWE-918");
            cweMap.put("open redirect", "CWE-601");
            cweMap.put("csrf", "CWE-352");
            cweMap.put("cross-site request", "CWE-352");
            cweMap.put("buffer overflow", "CWE-120");
            cweMap.put("weak crypto", "CWE-327");
            cweMap.put("weak hash", "CWE-328");
            cweMap.put("information disclosure", "CWE-200");
            cweMap.put("info leak", "CWE-200");
            cweMap.put("broken auth", "CWE-287");
            cweMap.put("authentication", "CWE-287");
            cweMap.put("insecure random", "CWE-330");
            cweMap.put("race condition", "CWE-362");
            cweMap.put("null pointer", "CWE-476");
            cweMap.put("log injection", "CWE-117");
            cweMap.put("ldap injection", "CWE-90");
            cweMap.put("xpath injection", "CWE-643");
            cweMap.put("missing encryption", "CWE-311");
            cweMap.put("cleartext", "CWE-319");
            cweMap.put("insecure storage", "CWE-922");

            for (Map.Entry<String, String> entry : cweMap.entrySet()) {
                if (combined.contains(entry.getKey())) {
                    cweMatches.add(entry.getValue());
                    break;
                }
            }
        }

        // Add CWE links
        List<String> sortedCWEs = new ArrayList<>(cweMatches);
        Collections.sort(sortedCWEs);
        for (String cwe : sortedCWEs) {
            String cweId = cwe.replace("CWE-", "");
            referencesPanel.add(createClickableLink(cwe + " - MITRE",
                "https://cwe.mitre.org/data/definitions/" + cweId + ".html"));
            referencesPanel.add(Box.createVerticalStrut(4));
        }
        if (!sortedCWEs.isEmpty()) {
            referencesPanel.add(Box.createVerticalStrut(8));
        }

        // KB references
        if (kbEntry != null && kbEntry.getReferences() != null && !kbEntry.getReferences().isEmpty()) {
            String[] refs = kbEntry.getReferences().split("\n");
            for (String ref : refs) {
                ref = ref.trim();
                if (ref.startsWith("http")) {
                    String label;
                    if (ref.contains("owasp.org")) label = "OWASP Reference";
                    else if (ref.contains("cwe.mitre.org")) label = "MITRE CWE Reference";
                    else if (ref.contains("knowledge-base.offensive360.com")) label = "O360 Knowledge Base";
                    else {
                        try { label = new java.net.URL(ref).getHost(); } catch (Exception e) { label = ref; }
                    }
                    referencesPanel.add(createClickableLink(label, ref));
                    referencesPanel.add(Box.createVerticalStrut(4));
                }
            }
        }

        // Server-side references
        if (vuln.getReferences() != null) {
            for (String ref : vuln.getReferences()) {
                if (ref != null && ref.startsWith("http")) {
                    String label;
                    try { label = new java.net.URL(ref).getHost(); } catch (Exception e) { label = ref; }
                    referencesPanel.add(createClickableLink(label, ref));
                    referencesPanel.add(Box.createVerticalStrut(4));
                }
            }
        }

        // General search link
        try {
            String searchQuery = java.net.URLEncoder.encode(title + " vulnerability fix", "UTF-8");
            referencesPanel.add(createClickableLink("Search: " + title + " vulnerability fix",
                "https://www.google.com/search?q=" + searchQuery));
        } catch (Exception e) {
            // ignore encoding error
        }

        referencesPanel.add(Box.createVerticalGlue());
        referencesPanel.revalidate();
        referencesPanel.repaint();

        // Switch to Details tab
        detailTabs.setSelectedIndex(0);
    }

    private void extractCWEs(Pattern pattern, String text, Set<String> out) {
        if (text == null || text.isEmpty()) return;
        Matcher m = pattern.matcher(text);
        while (m.find()) {
            out.add(m.group().toUpperCase());
        }
    }

    private Color getSeverityColor(String level) {
        if (level == null) return new Color(0, 120, 200);
        switch (level.toLowerCase()) {
            case "critical": return new Color(255, 59, 59);
            case "high": return new Color(255, 140, 0);
            case "medium": return new Color(255, 215, 0);
            case "low": return new Color(79, 195, 247);
            default: return new Color(144, 164, 174);
        }
    }

    // -- Service listener --

    @Override
    public void onResultsChanged() {
        ApplicationManager.getApplication().invokeLater(() -> {
            ScanResultsService service = ScanResultsService.getInstance(project);
            refreshFromService(service);
        });
    }

    private void refreshFromService(ScanResultsService service) {
        List<LangVulnerability> vulns = service.getLangVulnerabilities();
        langModel.setData(vulns);

        if (vulns != null && !vulns.isEmpty()) {
            cardLayout.show(cardPanel, CARD_RESULTS);

            int critical = 0, high = 0, medium = 0, low = 0;
            for (LangVulnerability v : vulns) {
                String rl = v.getRiskLevel() != null ? v.getRiskLevel().toLowerCase() : "";
                switch (rl) {
                    case "critical": critical++; break;
                    case "high": high++; break;
                    case "medium": medium++; break;
                    case "low": low++; break;
                }
            }
            statusLabel.setText(vulns.size() + " findings - Critical: " + critical
                + "  High: " + high + "  Medium: " + medium + "  Low: " + low);

            // Auto-select first row
            if (langTable.getRowCount() > 0) {
                langTable.setRowSelectionInterval(0, 0);
            }
        } else if (service.getTotalCount() > 0) {
            cardLayout.show(cardPanel, CARD_RESULTS);
            statusLabel.setText(service.getTotalCount() + " findings");
        } else {
            cardLayout.show(cardPanel, CARD_EMPTY);
        }
    }

    // -- Clipboard --

    private void copyRowToClipboard(JBTable table, int row) {
        StringBuilder sb = new StringBuilder();
        for (int col = 0; col < table.getColumnCount(); col++) {
            if (col > 0) sb.append("\t");
            Object val = table.getValueAt(row, col);
            sb.append(val != null ? val.toString() : "");
        }
        StringSelection sel = new StringSelection(sb.toString());
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(sel, sel);
    }

    // -- Navigation --

    private void navigateToLangVuln() {
        int row = langTable.getSelectedRow();
        if (row < 0) return;

        LangVulnerability vuln = langModel.getRow(row);
        if (vuln == null || vuln.getFilePath() == null) return;

        String basePath = project.getBasePath();
        String filePath = vuln.getFilePath();

        VirtualFile vf = LocalFileSystem.getInstance().findFileByPath(filePath);
        if (vf == null && basePath != null) {
            String fullPath = basePath + File.separator + filePath;
            vf = LocalFileSystem.getInstance().findFileByPath(fullPath.replace('\\', '/'));
        }
        if (vf == null && basePath != null) {
            String fileName = vuln.getFileName();
            if (fileName != null) {
                String fullPath = basePath + File.separator + fileName;
                vf = LocalFileSystem.getInstance().findFileByPath(fullPath.replace('\\', '/'));
            }
        }

        if (vf != null) {
            int line = Math.max(0, vuln.getLineNo() - 1);
            int col = Math.max(0, vuln.getColumnNo());
            new OpenFileDescriptor(project, vf, line, col).navigate(true);
        } else {
            Messages.showWarningDialog(project,
                "Could not locate file: " + filePath,
                "Offensive 360 SAST");
        }
    }

    // -- Severity renderer --

    private static class SeverityRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(
                table, value, isSelected, hasFocus, row, column);
            label.setHorizontalAlignment(SwingConstants.CENTER);

            if (!isSelected && value != null) {
                String sev = value.toString().toLowerCase();
                switch (sev) {
                    case "critical":
                        label.setForeground(new Color(180, 0, 0));
                        label.setFont(label.getFont().deriveFont(Font.BOLD));
                        break;
                    case "high":
                        label.setForeground(new Color(220, 80, 0));
                        label.setFont(label.getFont().deriveFont(Font.BOLD));
                        break;
                    case "medium":
                        label.setForeground(new Color(0, 120, 200));
                        label.setFont(label.getFont().deriveFont(Font.PLAIN));
                        break;
                    case "low":
                        label.setForeground(new Color(0, 140, 0));
                        label.setFont(label.getFont().deriveFont(Font.PLAIN));
                        break;
                    default:
                        label.setForeground(table.getForeground());
                        label.setFont(label.getFont().deriveFont(Font.PLAIN));
                        break;
                }
            }
            return label;
        }
    }

    // -- Table Model --

    private static class LangTableModel extends AbstractTableModel {
        private static final String[] COLUMNS = {"Severity", "Title", "File", "Line"};
        private List<LangVulnerability> data = Collections.emptyList();

        void setData(List<LangVulnerability> data) {
            this.data = data != null ? data : Collections.emptyList();
            fireTableDataChanged();
        }

        LangVulnerability getRow(int row) {
            return row >= 0 && row < data.size() ? data.get(row) : null;
        }

        @Override public int getRowCount() { return data.size(); }
        @Override public int getColumnCount() { return COLUMNS.length; }
        @Override public String getColumnName(int column) { return COLUMNS[column]; }

        @Override
        public Object getValueAt(int row, int col) {
            LangVulnerability v = data.get(row);
            switch (col) {
                case 0: return v.getRiskLevel() != null ? v.getRiskLevel() : "Medium";
                case 1: return v.getTitle() != null ? v.getTitle() : (v.getType() != null ? v.getType() : v.getVulnerability());
                case 2: return v.getFileName() != null ? v.getFileName() : v.getFilePath();
                case 3: return v.getLineNo();
                default: return "";
            }
        }
    }
}
