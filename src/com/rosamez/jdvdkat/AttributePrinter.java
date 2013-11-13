package com.rosamez.jdvdkat;

import com.rosamez.jdvdkat.nodes.NodeArchivedFile;
import com.rosamez.jdvdkat.nodes.NodeGeneric;
import com.rosamez.jdvdkat.nodes.NodeFile;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

/**
 * prints the attributes of a node using a StyledDocument
 * @author Szabolcs Kurdi
 */
public class AttributePrinter {
    private StyledDocument document = new DefaultStyledDocument();
    private SimpleAttributeSet boldAttrs = new SimpleAttributeSet();
    private SimpleAttributeSet simpleAttrs = new SimpleAttributeSet();
    private SimpleDateFormat dateFormatter = new SimpleDateFormat("kk:mm");
    private static Logger log = Logger.getLogger(AttributePrinter.class);

    public AttributePrinter(NodeGeneric node) {
        boldAttrs.addAttribute(StyleConstants.CharacterConstants.Bold, Boolean.TRUE);
        try {
            addAttributeText("Name:");
            addValueText(node.name);

            if (node.size > 0) {
                addAttributeText("Size:");
                addValueText(FileUtils.byteCountToDisplaySize(node.size));
            }

            addAttributeText("Last modified:");
            Date lmDate = new Date(node.lastModified);
            DateFormat df = DateFormat.getDateInstance(DateFormat.LONG);
            addValueText(df.format(lmDate) + " - " + dateFormatter.format(lmDate));

            // since it's pretty important for xpath, do not escape this one in NodeGeneric!
            addAttributeText("Absolute path:");
            addValueText(StringEscapeUtils.unescapeHtml(node.absolutePath));

            addAttributeText("Node:");
            String nodeName = node.node;
            if (node instanceof NodeFile && node.hasChildren) {
                nodeName += " (archive)";
            } else if (node instanceof NodeArchivedFile) {
                nodeName += " (archived)";
            }
            addValueText(nodeName);

            if (node.children > 0) {
                addAttributeText("Childnodes:");
                addValueText(Long.toString(node.children));
            }

            if (node.elements > node.children) {
                addAttributeText("Childnodes (recursive):");
                addValueText(Long.toString(node.elements));
            }

            if (node instanceof NodeFile) {
                String comment = ((NodeFile)node).comment;
                if (comment != null && !"".equals(comment)) {
                    addAttributeText("Comment:");
                    addValueText(comment);
                }

                String dim = "";
                if ((((NodeFile)node).width != null) && (((NodeFile)node).height != null)) {
                    addAttributeText("Dimension:");
                    dim += Long.toString(((NodeFile)node).width);
                    dim += " x ";
                    dim += Long.toString(((NodeFile)node).height);
                    addValueText(dim);
                }

                if (((NodeFile)node).title != null) {
                    addAttributeText("Title:");
                    addValueText(((NodeFile)node).title);
                }

                if (((NodeFile)node).artist != null) {
                    addAttributeText("Artist:");
                    addValueText(((NodeFile)node).artist);
                }

                if (((NodeFile)node).year != null) {
                    addAttributeText("Year:");
                    addValueText(Integer.toString(((NodeFile)node).year));
                }

                if (((NodeFile)node).genre != null) {
                    addAttributeText("Genre:");
                    addValueText(((NodeFile)node).genre);
                }
            }

        } catch (BadLocationException ex) {
            log.error(ex.getMessage());
        }
    }

    public StyledDocument getStyledDocument() {
        return document;
    }

    private void addAttributeText(String text) throws BadLocationException {
        document.insertString(document.getLength(), text + "\n", boldAttrs);
    }

    private void addValueText(String text) throws BadLocationException {
        document.insertString(document.getLength(), "  " + text + "\n", simpleAttrs);
    }

}
