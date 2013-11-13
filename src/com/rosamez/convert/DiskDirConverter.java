package com.rosamez.convert;

import com.rosamez.dirlistex.SimpleFileUtils;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringEscapeUtils;

/**
 * convert DiskDir file format to xml; only a textual conversion is
 * done, no real xml is involved (which so far is not a big deal
 * since this is the only supported format)
 * @author Szabolcs Kurdi
 */
public class DiskDirConverter {

    private File inputFile;
    private boolean verbose = false;

    public DiskDirConverter(File inputFile) {
        this.inputFile = inputFile;
    }

    public String convert() {
        String s = "";
        String sIn;

        try {
            sIn = FileUtils.readFileToString(inputFile, "iso-8859-1");
        } catch (IOException ex) {
            System.err.println("Could not read the input file. " + ex.getMessage());
            return null;
        }

        // split to lines
        String[] lines = sIn.split("\n");

        if (lines.length < 2) {
            System.err.println("Not enough lines in input file.");
            return null;
        }

        String prefix = lines[0].trim(); // very first line
        DiskDirSimpleNode node;
        DiskDirSimpleNode lastParent = null;
        DiskDirStack buffer = new DiskDirStack();

        for (int i = 1; i < lines.length; i++) {
            String line = lines[i].trim();

            // skip empty lines
            if ("".equals(line)) {
                continue;
            }

            // explode line at tabs
            String[] parts = line.split("\t");
            if (parts.length < 4) {
                System.out.println("Not enough columns: [name + size + date(y.m.d)"
                        + " + time (h:m.s)] should've been set; sorry.");
                return null;
            }

            // create node {{{
            node = new DiskDirSimpleNode(
                    parts[0], // fileName
                    parts[1], // fileSize
                    parts[2] + " " + parts[3] // timeStamp
                    );
            if (lastParent != null && !node.isContainer && !node.isArchive) {
                node.lastParentPath = lastParent.fileName;
            }
            node.prefix = prefix; // TODO: clean up constructors
            /// }}} create node

            // filenames with slashes are not mere files, but either
            // directories or archives with subfiles
            if (node.isContainer) {
                lastParent = node;
                buffer.push(node);
                if (buffer.test(node)) { // find closed/closing nodes
                    s += buffer.generateCloserXmlStrings();
                }
            }

            // destroy archives inside archives, leave their files only
            // since jDvdKat doesn't store subarchives - btw diskdir
            // can not tell TC that these files are not directories,
            // so the .lst data format kinda sucks
            DiskDirSimpleNode firstArchive = buffer.getFirstActiveArchive();
            if (firstArchive != null && (!node.fileName.equals(firstArchive.fileName))) {
                node.packed = true;
                //node.archiveParentName = firstArchive.fileName;
                //if (node.isContainer && node.fileSize == 0) {
                //    node.invalidate();
                //}
            }

            // get the xml string of the node
            String xmlStr = "";
            if (!node.deleted) {
                xmlStr = node.getXmlString();
                s += xmlStr + "\n";
                if (verbose) {
                    System.out.println((node.packed ? "    " : "") + node.fileName + " --> " + xmlStr);
                }
            }
        }

        //flush remaining nodes from stack + get closer tags
        if (buffer.flushRemaining()) {
            s += buffer.generateCloserXmlStrings();
        }

        // build root node
        Date today = new Date();
        String rootStr = "<directory"
                + " format=\"DiskDir\""
                + " name=\"\""
                + " size=\"0\""
                + " lastModified=\"" + today.getTime() + "\""
                + " absolutePath=\"" + StringEscapeUtils.escapeXml(SimpleFileUtils.sanitizePath(prefix))
                + "\">\n";
        s = rootStr + s + "</directory>";

        // clean up empty nodes <a></a> --> <a />
        String esc = "%ESCAPED-SINGLE-TAG%";
        s = s.replaceAll("/>", esc);
        s = s.replaceAll("<(directory|file)([^<>]*)>\n</\\1>", "<$1$2 />"); // collapse empty nodes
        s = s.replaceAll(esc, "/>");

        return s;
    }
}
