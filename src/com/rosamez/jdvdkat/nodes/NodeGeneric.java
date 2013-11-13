// nodes created from the db's string chunk response
package com.rosamez.jdvdkat.nodes;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import org.apache.commons.lang.StringEscapeUtils;

/**
 * generic node with the common properties for all node types
 * (directory, file, archivedFile)
 * @author Szabolcs Kurdi
 */
public class NodeGeneric {
    public String name = null;
    public String doc = null;
    public Long size = null;
    public Long lastModified = null;
    public String date = null; // the original date from Matt's xml-dir-listing
    public Date dateNative = null; // java native date format
    public String absolutePath = null;
    public String unescapedAbsolutePath = null;
    
    // the "fake" attributes (created with the xquery return)
    public String node = null;
    public Boolean hasParent = null;
    public Long elements = null;
    public Long children = null;

    // computed locally
    public Boolean hasChildren = null;

    public void fromHashMap(HashMap hm) {
        name = StringEscapeUtils.unescapeXml(toString(hm, "name"));
        doc = toString(hm, "doc");
        size = toLong(hm, "size");
        lastModified = toLong(hm, "lastModified");
        date = toString(hm, "date");
        dateNative = new Date(lastModified); // added later... TODO: rename simple date to dateStr?
        absolutePath = toString(hm, "absolutePath");
        unescapedAbsolutePath = StringEscapeUtils.unescapeXml(absolutePath); // ugly hack; TODO: force abspath to be unescaped for all the time

        node = toString(hm, "node");
        hasParent = toBoolean(hm, "hasParent");
        elements = toLong(hm, "elements");
        children = toLong(hm, "children");

        hasChildren = children > 0;
    }

    public String getPath() {
        return "//" + node + "[@absolutePath='" + absolutePath + "']";
    }

    public String getParentPath() {
        return hasParent ? getPath() + "/.." : null;
    }

    protected Long toLong(HashMap hm, String key) {
        String buffer = new String();
        buffer = (String)hm.get(key);
        if (buffer != null) {
            return Long.parseLong(buffer);
        }
        return null;
    }

    protected Integer toInteger(HashMap hm, String key) {
        String buffer = new String();
        buffer = (String)hm.get(key);
        if (buffer != null) {
            return Integer.parseInt(buffer);
        }
        return null;
    }

    protected String toString(HashMap hm, String key) {
        String buffer = new String();
        buffer = (String)hm.get(key);
        if (buffer != null && !"".equals(buffer)) {
            return buffer;
        }
        return null;
    }
    
    protected Boolean toBoolean(HashMap hm, String key) {
        String buffer = new String();
        buffer = (String)hm.get(key);
        if (buffer != null) {
            return Boolean.parseBoolean(buffer);
        }
        return null;
    }

    // formatted output

    public String getShortDoc() {
        String shDoc = doc;
        int lastSlash = shDoc.lastIndexOf("/");
        if (lastSlash > -1) {
            shDoc = shDoc.substring(lastSlash + 1);
        }
        return shDoc;
    }

    public String getPrettyType() {
        return ("file".equals(node) && hasChildren) ? "archive" : node;
    }

    public String getFormattedSize() {
        return NumberFormat.getInstance().format(size);
    }

    public String getLastModDate(SimpleDateFormat dateFormatter) {
        long msecs = lastModified;
        Date fileDate = new Date(msecs);
        return dateFormatter.format(fileDate);
    }

    /**
     * formatted (truncated + escaped) abspath for the search view
     * @return
     */
    public String getTruncAbsPath() {
        String aP = absolutePath;
        if (aP.indexOf(":/") == 1) {
            aP = aP.substring(2);
        }
        aP = StringEscapeUtils.unescapeXml(aP);
        return aP;
    }
}
