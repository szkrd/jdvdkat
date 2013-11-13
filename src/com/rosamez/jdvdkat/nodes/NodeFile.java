// nodes created from the db's string chunk response
package com.rosamez.jdvdkat.nodes;

import java.util.HashMap;
import org.apache.commons.lang.StringEscapeUtils;

/**
 * file type nodes
 * @author Szabolcs Kurdi
 */
public class NodeFile extends NodeGeneric {
    public String suffix = null;
    public String comment = null;
    
    // picture files may have some special properties
    public Long width = null;
    public Long height = null;

    // same goes for mp3 files
    public String title = null;
    public String artist = null;
    public Integer year = null;
    public String genre = null;

    @Override
    public void fromHashMap(HashMap hm) {
        super.fromHashMap(hm);
        suffix = toString(hm, "suffix");
        comment = StringEscapeUtils.unescapeHtml(toString(hm, "comment"));

        width = toLong(hm, "width");
        height = toLong(hm, "height");

        title = StringEscapeUtils.unescapeHtml(toString(hm, "title"));
        artist = StringEscapeUtils.unescapeHtml(toString(hm, "artist"));
        year = toInteger(hm, "year");
        genre = StringEscapeUtils.unescapeHtml(toString(hm, "genre"));
    }
}
