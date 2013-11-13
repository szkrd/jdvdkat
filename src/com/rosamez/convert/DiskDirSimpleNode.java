package com.rosamez.convert;

import com.rosamez.dirlistex.SimpleFileUtils;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.commons.lang.StringEscapeUtils;

/**
 * a node representing one row from a diskdir file
 * @author Szabolcs Kurdi
 */
public class DiskDirSimpleNode {

    public String fileName;
    private String shortFileName = null;
    public Long fileSize = 0l;
    public Long timeStamp = 0l;
    public boolean isContainer = false;
    public boolean isArchive = false;
    public String prefix = "";
    public boolean packed = false;
    public String archiveParentName = "";
    public String lastParentPath = "";
    public boolean closed = false;
    public boolean deleted = false;

    public DiskDirSimpleNode(String s0, Long l1, Long l2) {
        construct(s0, l1, l2);
    }

    public DiskDirSimpleNode(String s0, String s1, String s2) {
        construct(s0, Long.valueOf(s1), strToDate(s2));
    }

    /**
     * get the xml string representation of this node
     * @return
     */
    public String getXmlString() {
        String ret = "";
        String attrs;

        attrs = " name=\"" + StringEscapeUtils.escapeXml(getShortFileName()) + "\""
                + " size=\"" + fileSize + "\""
                + " lastModified=\"" + timeStamp + "\""
                + " absolutePath=\"" + StringEscapeUtils.escapeXml(getAbsPath()) + "\"";

        if (packed) {
            attrs += " packed=\"true\"";
        }

        String suffix = getSuffix();
        if (suffix != null) {
            attrs += " suffix=\"" + suffix + "\"";
        }

        if (isContainer && !isArchive) {
            ret = "<directory" + attrs;
        } else {
            ret = "<file" + attrs;
        }
        if (!isContainer && !isArchive) {
            ret += " />"; // single file, we may close it right here
        } else {
            ret += ">"; // may have child nodes (in reality)
        }
        return ret;
    }

    private void construct(String fileName, Long fileSize, Long timeStamp) {
        this.fileName = normalizeSlashes(fileName);
        this.fileSize = fileSize;
        this.timeStamp = timeStamp;
        isContainer = isContainer(this.fileName);

        // an archive file, like zip, rar etc.
        if ((isContainer) && (this.fileSize > 0)) {
            isArchive = true;
            this.fileName = this.fileName.replaceAll("/$", "");
        }
    }

    /**
     * get unix timestamp for a date string
     * @param dateStr
     * @return
     */
    private Long strToDate(String dateStr) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm.ss");
        Long l = 0l;
        Date d = null;
        try {
            d = sdf.parse(dateStr);
        } catch (ParseException ex) {
            System.err.println("Could not convert the date '" + dateStr + "' to timestamp.");
        }
        l = d.getTime();
        return l;
    }

    /**
     * if the filename has slashes, then it's a container
     * @param s
     * @return
     */
    private boolean isContainer(String s) {
        return s.indexOf("/") > -1;
    }

    /**
     * try to build the full absolute path
     * @return
     */
    private String getAbsPath() {
        String fnChunk = fileName;
        String abp = "";
        abp = lastParentPath + "/" + fnChunk;
        abp = normalizeSlashes(prefix) + "/" + normalizeSlashes(abp);
        abp = abp.replaceAll("/+", "/");
        return abp;
    }

    /**
     * get the filename part of a path (last section); if it's a packed
     * file then make it relative to the topmost parent archive
     * @return
     */
    private String getShortFileName() {
        if (shortFileName != null) {
            return shortFileName;
        }
        String[] parts = fileName.trim().replaceAll("/$", "").split("/");
        shortFileName = parts[parts.length - 1];
        /*
        if (packed) {
        shortFileName = lastParentPath.replace(archiveParentName, "") + "/" + shortFileName;
        shortFileName = shortFileName.replaceAll("^/", "");

        if ("".equals(lastParentPath)) {
        shortFileName = fileName.replaceAll("^[^/]STARSYMBOL/", ""); // remove first element (topmost archive name)
        }
        }
        shortFileName = shortFileName.replaceAll("/+", "/");
         */
        return shortFileName;
    }

    /**
     * convert backslashes to slashes
     * @param s
     * @return
     */
    private String normalizeSlashes(String s) {
        return s.replaceAll("\\\\", "/");
    }

    /**
     * get file extension
     * @return
     */
    private String getSuffix() {
        return SimpleFileUtils.getSuffix(getShortFileName());
    }

    /**
     * mark the node as invalid, so that it will
     * never be closed or accounted for
     */
    public void invalidate() {
        closed = true;
        deleted = true;
    }
}
