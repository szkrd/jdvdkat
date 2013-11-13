package com.rosamez.convert;

import java.util.ArrayList;

/**
 * a special array for storing the nodes, trying to find out
 * which needs to be closed or deleted
 * @author Szabolcs Kurdi
 */
public class DiskDirStack {
    private ArrayList<DiskDirSimpleNode> stack = new ArrayList<DiskDirSimpleNode>();
    private boolean verbose = false;

    public DiskDirStack() {
    }

    public void push(DiskDirSimpleNode node) {
        stack.add(node);
    }

    /**
     * marks previously closed nodes as deleted,
     * tests all nodes against fileName (if the fileName doesn't contain
     * the node's name then it must be closed)
     *
     * @param fileName
     * @return didClose did we close a node?
     */
    private boolean test(String fileName) {
        boolean didClose = false;
        for (int i = 0; i < stack.size(); i++) {
            DiskDirSimpleNode actNode = stack.get(i);
            // invalidate previously closed nodes
            if (actNode.closed) {
                actNode.deleted = true;
            }
            // close certain nodes
            if (!actNode.deleted) {
                if (fileName.indexOf(actNode.fileName) == -1) {
                    actNode.closed = true;
                    didClose = true;
                }
            }
        }
        return didClose;
    }

    boolean test(DiskDirSimpleNode node) {
        return test(node.fileName);
    }

    public boolean flushRemaining() {
        return test("");
    }

    /**
     * generates closer tag strings for closed nodes
     * like </directory> or </file>
     */
    public String generateCloserXmlStrings() {
        if (stack.isEmpty()) {
            return "";
        }
        String msg = "[closing node(s):";
        String ret = "";
        for (int i = stack.size() - 1; i >= 0; i--) {
            DiskDirSimpleNode actNode = stack.get(i);
            if (actNode.closed && !actNode.deleted) {
                ret += actNode.isArchive ? "</file>" : "</directory>";
                ret += "\n";
                msg += " " + actNode.fileName;
            }
        }
        msg += "]";
        if (verbose) {
            System.out.println(msg);
        }
        return ret;
    }

    /**
     * diskdir handles archives inside archives badly (TC
     * considers these nodes to be directories, but in fact they are
     * nested archives (containers))
     * @return
     */
    public DiskDirSimpleNode getFirstActiveArchive() {
        for (int i = 0; i < stack.size(); i++) {
            DiskDirSimpleNode actNode = stack.get(i);
            if (actNode.isArchive && !actNode.closed && !actNode.deleted) {
                return actNode;
            }
        }
        return null;
    }

}
