// nodes created from the db's string chunk response
package com.rosamez.jdvdkat.nodes;

import java.util.HashMap;

/**
 * archived file node
 * @author Szabolcs Kurdi
 */
public class NodeArchivedFile extends NodeGeneric {
    public Boolean packed = true;

    // this may be omitted actually...
    @Override
    public void fromHashMap(HashMap hm) {
        super.fromHashMap(hm);
        packed = toBoolean(hm, "packed");
    }
}
