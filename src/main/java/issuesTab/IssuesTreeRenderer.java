package issuesTab;

import APIResponse.Vulnerability;
import icon.Offensive360Icons;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;

import java.util.HashMap;
import java.util.Map;


/** Author : Shyam_Vegi
 * Issue Tree Renderer is an modified tree cell renderer
 *
 */

public class IssuesTreeRenderer extends DefaultTreeCellRenderer{
    DefaultMutableTreeNode nodeSelected;

    Vulnerability nodeData;

    Map<String,Icon> riskColor;

    public IssuesTreeRenderer() {
        riskColor = new HashMap<>();
        riskColor.put("1",Offensive360Icons.LOW_ICON);
        riskColor.put("2",Offensive360Icons.MEDIUM_ICON);
        riskColor.put("3",Offensive360Icons.HIGH_ICON);
        riskColor.put("4",Offensive360Icons.CRITICAL_ICON);
    }

    public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                  boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {

        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row,
                hasFocus);

        nodeSelected = (DefaultMutableTreeNode) value;
        //assert nodeSelected != null;
        if (nodeSelected == null) return this;

        if (leaf && nodeSelected.getUserObject() instanceof Vulnerability) {
            nodeData = (Vulnerability) nodeSelected.getUserObject();
            setIcon(getIconFor());
            setToolTipText(nodeData.getVulnerability());
            return this;
        }

        if(leaf && nodeSelected.getParent()==null){
            setIcon(Offensive360Icons.INFO_ICON);
            setText("No Vulnerabilities Found");
            return this;
        }

        if(leaf && nodeSelected.getUserObject() instanceof String) {
            ((DefaultTreeModel) tree.getModel()).removeNodeFromParent(nodeSelected);
            return this;
        }


        setIcon(Offensive360Icons.ICON_OFFENSIVE360);
        setToolTipText(null);
        return this;

    }

    private Icon getIconFor() {
        if(nodeData!=null){
            return riskColor.getOrDefault(nodeData.getRiskLevel(),null);
        }
        return null;
    }
}