package com.rosamez.jdvdkat;

import com.rosamez.jdvdkat.nodes.NodeArchivedFile;
import com.rosamez.jdvdkat.nodes.NodeGeneric;
import com.rosamez.jdvdkat.nodes.NodeFile;
import com.rosamez.jdvdkat.nodes.NodeDirectory;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.log4j.Logger;

/**
 * Class for retrieving data from the db and return them as nodes
 * @author Szabolcs Kurdi
 */
public class DvdKatModel {

    protected DbManager dbManager = null;
    // the response from the db engine is kept in a string format
    // which is faster than parsing full blown xml nodes, so I define
    // field delimiters here, just to be on the safe side
    protected String delimAttrValueSeparator = "%ATTRVALSPLIT%";
    protected String delimAttrSeparator = "%ENDOFATTR%";
    protected String delimNodeSeparator = "%ENDOFNODE%";
    protected String currentDoc = null;

    private static Logger log = Logger.getLogger(DvdKatModel.class);

    public DvdKatModel(DbManager dbManager) {
        this.dbManager = dbManager;
    }

    /**
     * Get the XML files from the opened DB (collection)
     * @return String[] base uris or null if collection is empty
     */
    public String[] getFiles() {
        String query = "for $doc in collection()"
                + "return concat(base-uri($doc), '&#xA;')";
        String response = dbManager.executeQuery(query);
        String[] baseUris = (response == null) ? null : response.split("\n ?");
        return baseUris;
    }

    /**
     * returns name (node.name) of the given node's parent
     * @param ng
     * @return
     */
    public String getParentNodeName(NodeGeneric ng) {
        if (!ng.hasParent) {
            return null;
        }
        String path = getPathForNode(ng) + "/..";
        NodeGeneric node = getFirstNodeForFile(path);
        return (node == null) ? null : node.name;
    }

    /**
     * generates a unique xpath locator from the given node
     * using the absolutePath attribute
     * @param ng
     * @return
     */
    public String getPathForNode(NodeGeneric ng) {
        String type = "file";
        if (ng instanceof NodeDirectory) {
            type = "directory";
        }
        String absPath = ng.absolutePath;
        if (absPath == null) {
            log.error("Legacy xml! AbsolutePath missing for node!");
        }
        return "//" + type + "[@absolutePath=\"" + absPath + "\"]";
    }

    /**
     * get the path for a given node's children (/*)
     * @param ng
     * @return
     */
    public String getPathForNodeChilren(NodeGeneric ng) {
        return ng.hasChildren ? getPathForNode(ng) + "/*" : null;
    }

    /**
     * get the path for the children of the node's parent
     * @param ng
     * @return
     */
    public String getPathForParentNodeChildren(NodeGeneric ng) {
        return ng.hasParent ? getPathForNode(ng) + "/../../*" : null;
    }

    /**
     * get the first (root) node for a given xml file (a BaseX doc)
     * @param path
     * @return
     */
    public NodeGeneric getFirstNodeForFile(String path) {
        ArrayList<NodeGeneric> retNodes = getNodesForFile(path);
        return (retNodes.size() < 1) ? null : (NodeGeneric) retNodes.get(0);
    }

    public ArrayList<NodeGeneric> getNodesForFile(String path) {
        return getNodesForFile(currentDoc, path);
    }

    public ArrayList<NodeGeneric> getNodesForFile(String fileName, String path) {
        boolean inAllDocs = "".equals(fileName);
        String buffer = getStringNodesForFile(fileName, path, inAllDocs);
        return (buffer == null || "".equals(buffer)) ? null : convertRawResponse(buffer);
    }

    /**
     * Get a list of nodes for a given xpath, inside a given xml file
     * in the collection
     * @param fileName name of the xml file in the collection
     * @param path xpath target for example "/directory/*"
     * @return String non-xml string that needs to be reprocessed
     */
    protected String getStringNodesForFile(String fileName, String path, boolean inAllDocs) {
        if (fileName.indexOf("file:") == -1) {
            fileName = "/" + fileName; // TODO: is "'" in filename okay?
        }
        String query =
                // search only in docs for doc that ends with fileName
                "for $doc in collection()"
                + "let $file-path := base-uri($doc)"
                // TODO: inAll === where ends-with($file-path, '.xml') ??
                + (inAllDocs ? "" : "where ends-with($file-path, '" + fileName + "')")
                + // main sequence return, returns native attributes
                "return ("
                + "   for $x in $doc" + path
                + "   let $nodes := ("
                + "       for $y in $x/@*"
                + "       return concat(node-name($y), '" + delimAttrValueSeparator + "', $y, '" + delimAttrSeparator + "')"
                + "   )"
                // extra "fake" attributes
                + "   let $ret := insert-before(concat('node', '" + delimAttrValueSeparator + "', node-name($x), '" + delimAttrSeparator + "'), 0, $nodes)"
                + "   let $ret := insert-before(concat('hasParent', '" + delimAttrValueSeparator + "', boolean(count($x/../../..)), '" + delimAttrSeparator + "'), 0, $ret)" // up 1: rootnode, up 1: doc-node, up 1: nothing
                + "   let $ret := insert-before(concat('elements', '" + delimAttrValueSeparator + "', count($x//*), '" + delimAttrSeparator + "'), 0, $ret)"
                + "   let $ret := insert-before(concat('children', '" + delimAttrValueSeparator + "', count($x/*), '" + delimAttrSeparator + "'), 0, $ret)"
                + "   let $ret := insert-before(concat('doc', '" + delimAttrValueSeparator + "', $file-path, '" + delimAttrSeparator + "'), 0, $ret)"
                // end of a node
                + "   let $ret := insert-before('" + delimNodeSeparator + "', 0, $ret)"
                + "   return $ret"
                + ")";
        String response = dbManager.executeQuery(query);
        return response;
    }

    /**
     * convert the large string response from getStringNodes into
     * an ArrayList of nodes.
     * @param stream xquery response
     * @return ArrayList list of nodes
     */
    protected ArrayList<NodeGeneric> convertRawResponse(String stream) {
        ArrayList<NodeGeneric> nodesList = null;
        String[] strNodes = stream.split(delimNodeSeparator);

        if (strNodes.length > 0) {
            nodesList = new ArrayList<NodeGeneric>();
        }

        // nodes
        for (int i = 0; i < strNodes.length; i++) {
            String node = strNodes[i];
            String[] attrs = node.split(delimAttrSeparator);
            HashMap<String, String> nodeObj = new HashMap<String, String>();

            // key-value pairs
            for (int j = 0; j < attrs.length; j++) {
                String attr = attrs[j];
                String[] keyVal = attr.split(delimAttrValueSeparator);

                if (keyVal.length == 2) {
                    String key = keyVal[0].trim();
                    String val = keyVal[1].trim();
                    nodeObj.put(key, val);
                }
            }

            // we have all key-value pairs in the nodeObj -> create a node obj
            String simpleType = nodeObj.get("node");
            NodeGeneric nodeParsed;
            if ("directory".equals(simpleType)) {
                nodeParsed = new NodeDirectory();
            } else if ("true".equals(nodeObj.get("packed"))) {
                nodeParsed = new NodeArchivedFile();
            } else {
                nodeParsed = new NodeFile();
            }
            nodeParsed.fromHashMap(nodeObj);

            // add to the arraylist
            nodesList.add(nodeParsed);
        }

        return nodesList;
    }

    /**
     * set active doc (0001.xml for example)
     * @param docName
     */
    public void setActiveDoc(String docName) {
        currentDoc = docName;
    }

    /**
     * retrieve xml from db as string
     * @param shortName
     * @return
     */
    public String fetchFileAsString(String shortName) {
        String query = "for $doc in collection()"
                + "let $file-path := base-uri($doc)"
                + "where ends-with($file-path, \"" + shortName + "\")"
                + "return $doc";
        String response = dbManager.executeQuery(query);
        return response;
    }

    /**
     * returns a saveable string in xml format with the proper xml header
     * @param shortName
     * @return
     */
    public String fetchFileAsSaveable(String shortName) {
        String response = fetchFileAsString(shortName);
        if (response != null) {
            response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + response;
        }
        return response;
    }
}
