package issuesTab;

import APIResponse.SingleScanResponse;
import APIResponse.Vulnerability;

import actions.GoToCodeAction;
import actions.SupressAction;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.JBMenuItem;
import com.intellij.openapi.ui.JBPopupMenu;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.ActionCallback;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.ui.JBUI;
import icon.Offensive360Icons;
import org.jetbrains.annotations.NotNull;
import utility.Misc;


import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;


/**
 * Author: Shyam.Vegi
 * This class makes a tree with list of vulnerabilities
 */

public class IssuesList {
    JBScrollPane scrollPane;
    JPanel panel;
    Tree jTree;
    Project project;
    public static  Map<String, Set<String>> SUPPRESSED_FILES;

    public IssuesList(Project project,SingleScanResponse singleScanResponse){
        panel = new JPanel(new BorderLayout());
        this.project = project;
        List<Vulnerability> listVulnerability = singleScanResponse.getVulnerabilities();
        DefaultMutableTreeNode defaultMutableTreeNode = new DefaultMutableTreeNode("Last Scanned at "+ LocalDateTime.now());

        Map<String,Map<String,List<Vulnerability>>> vulnerabilities = new HashMap<>();
        AtomicReference<Map<String, List<Vulnerability>>> v = new AtomicReference<>();

        try{
            listVulnerability.forEach(vulnerability -> {
                if(!isSuppressedFile(vulnerability)){
                    vulnerabilities.putIfAbsent(vulnerability.getType(),new HashMap<>());
                    v.set(vulnerabilities.get(vulnerability.getType()));
                    v.get().putIfAbsent(vulnerability.getFileName(),new ArrayList<>());
                    v.get().get(vulnerability.getFileName()).add(vulnerability);
                }
            });
           // System.out.println(vulnerabilities);
        }
        catch (Exception e){
            Messages.showErrorDialog("Internal error","Offensive360 Error");
        }

        final DefaultMutableTreeNode[] vulnerabilityType = {null};


        vulnerabilities.forEach((s, objects) -> {
            vulnerabilityType[0] = new DefaultMutableTreeNode(s);
            defaultMutableTreeNode.add(vulnerabilityType[0]);

            objects.forEach((file,lc)->{
                DefaultMutableTreeNode fileName = new DefaultMutableTreeNode(lc.get(0)){
                    @Override
                    public String toString() {
                        if(lc.size()==1) {
                            return "["
                            + Misc.riskTypes[Integer.parseInt(lc.get(0).getRiskLevel())-1]
                                    +"] "
                                    + file
                                    +", ("+lc.get(0).getLineNumber()+" )";
                        }
                        return  file;
                    }
                };
                if(lc.size()>1){
                    lc.forEach(obj-> fileName.add(new DefaultMutableTreeNode(obj){
                        @Override
                        public String toString() {

                            return "["
                                    + Misc.riskTypes[Integer.parseInt(lc.get(0).getRiskLevel())-1]
                                    +"] "
                                    +obj.getCodeSnippet()+" ("
                                    + obj.getLineNumber() +") ";
                        }
                    }));
                }
                vulnerabilityType[0].add(fileName);
            });
        });

        jTree  = new Tree(defaultMutableTreeNode);
        jTree.setCellRenderer(new IssuesTreeRenderer());
        jTree.addMouseListener(new MouseAdapter() {
            DefaultMutableTreeNode nodeSelected;

            Object userObject;

            TreePath path;

            /**
             * @param e the event to be processed
             */
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    try {
                        nodeSelected = (DefaultMutableTreeNode)jTree.getLastSelectedPathComponent();
                        if (nodeSelected!=null && nodeSelected.isLeaf()) {
                            userObject =  nodeSelected.getUserObject();
                            if(userObject instanceof Vulnerability){
                                Vulnerability nodeData = (Vulnerability) userObject;
                                ActionManager.getInstance().tryToExecute(new GoToCodeAction(nodeData),e,null,null,true);
                            }
                        }
                    }
                    catch (Exception ex){
                        //System.out.println(ex);
                        throw new RuntimeException();
                    }
                }
            }
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    path = jTree.getPathForLocation(e.getX(),e.getY());
                    if(path==null){
                        return;
                    }
                    nodeSelected = (DefaultMutableTreeNode)  path.getLastPathComponent();
                    if(nodeSelected!=null && nodeSelected.isLeaf()){
                        userObject = nodeSelected.getUserObject();
                        if(userObject!=null && userObject instanceof Vulnerability){
                            this.showPopUp(e,true);
                        }
                    }
                    else if(nodeSelected!=null && nodeSelected.getParent()!=null){
                        this.showPopUp(e,false);
                    }
                }
            }

            private void showPopUp(MouseEvent e, boolean b) {
                JBPopupMenu popup = new JBPopupMenu();
                JBMenuItem getHelp = new JBMenuItem("Get Help");
                JBMenuItem suppress = new JBMenuItem("Suppress");
                JBMenuItem clearIssues = new JBMenuItem("ClearAll");
                getHelp.setHorizontalTextPosition(SwingConstants.CENTER);
                suppress.setHorizontalTextPosition(SwingConstants.CENTER);
                clearIssues.setHorizontalTextPosition(SwingConstants.CENTER);
                popup.setBorderPainted(true);
                popup.setMaximumSize(new Dimension(200,200));
                popup.setPopupSize(new Dimension(180,110));
                popup.add(getHelp);
                popup.add(suppress);
                popup.add(clearIssues);
                suppress.setEnabled(b);
                popup.show(e.getComponent(), e.getX()+2, e.getY()+3);

                Vulnerability data = (Vulnerability) userObject;
                if(data!=null && data.getAdditionalProperties().get("references")==null){
                    getHelp.setEnabled(false);
                }

                //Suppressing Vulnerability Feature
                suppress.addActionListener((ActionEvent e1)->{
                    ActionCallback actionCallback =  ActionManager.getInstance().tryToExecute(new SupressAction(data),e,null,null,true);
                    actionCallback.doWhenDone(()-> removeSuppressedNodeFromTree(nodeSelected));
                });

                //Clear All Issues Feature
                clearIssues.addActionListener((ActionEvent e12) -> removeAllIssues());

                //Get Help Feature
                getHelp.addActionListener((ActionEvent e1) -> {
                    String url = data != null ? data.getAdditionalProperties().getOrDefault("references", null).toString() : null;
                    openBrowserForHelp(url);
                });
            }
        });
        panel.add(jTree);
        panel.setBorder(BorderFactory.createEmptyBorder(0,10,0,0));
        scrollPane = new JBScrollPane(panel);
        scrollPane.setBorder(JBUI.Borders.empty());
    }

    private void openBrowserForHelp(String url) {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(new URI(url));
            } catch (IOException | URISyntaxException ex) {
                Messages.showErrorDialog(jTree, "Error navigating to URL:" + url, "Error");
            }
        } else {
            Messages.showErrorDialog(jTree, "Desktop feature not supported on this platform", "Error");
        }
    }

    //Method for Clear Issues Action
    private void removeAllIssues() {
        int res = JOptionPane.showConfirmDialog(panel,"Are you sure to clear all","O360: ClearAll",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
        if(res == JOptionPane.YES_OPTION) {
            panel.removeAll();
            JLabel label = new JLabel("Run A Scan To Find Vulnerabilities");
            label.setIcon(Offensive360Icons.INFO_ICON);
            label.setHorizontalAlignment(SwingUtilities.CENTER);
            label.setIconTextGap(5);
            label.setForeground(new Color(0x0F59DA));
            panel.add(label);
            panel.updateUI();
            Misc.sendNotification("O360: ClearAll", "All Vulnerabiilities Cleared Successfully");
        }
    }

    //Method For Suppression Action
    private void removeSuppressedNodeFromTree(@NotNull DefaultMutableTreeNode nodeSelected) {
        DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) nodeSelected.getParent();
        if(parentNode.getParent()==null){
            parentNode.removeAllChildren();
            parentNode.add(new DefaultMutableTreeNode("No Issues Found"));
            jTree.updateUI();
        }
        else if(parentNode.getChildCount()>1) {
            ((DefaultTreeModel) jTree.getModel()).removeNodeFromParent(nodeSelected);
        }
        else{
            ((DefaultTreeModel) jTree.getModel()).removeNodeFromParent(parentNode);
        }
        Misc.sendNotification("O360: Suppress","Suppressed Successfully");
    }

    //Checks For Suppressed Files
    private boolean isSuppressedFile(Vulnerability vulnerability) {
        return SUPPRESSED_FILES != null &&
                SUPPRESSED_FILES.containsKey(vulnerability.getFilePath()) &&
                SUPPRESSED_FILES.get(vulnerability.getFilePath()).contains(vulnerability.getLineNumber());
    }

    public JScrollPane getIssuesList(){
        return scrollPane;
    }
}
