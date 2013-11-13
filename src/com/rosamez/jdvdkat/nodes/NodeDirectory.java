// nodes created from the db's string chunk response
package com.rosamez.jdvdkat.nodes;

import java.util.HashMap;

/**
 * directory type nodes
 * @author Szabolcs Kurdi
 */
public class NodeDirectory extends NodeGeneric {
    public String sort = null;
    public Boolean reverse = null;
    public Boolean volume = null;
    public String label = null;
    public String comment = null;

    @Override
    public void fromHashMap(HashMap hm) {
        super.fromHashMap(hm);
        sort = toString(hm, "sort");
        reverse = toBoolean(hm, "reverse");
        volume = toBoolean(hm, "volume");
        label = toString(hm, "label");
        comment = toString(hm, "comment");
    }
}
