/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.rosamez.jdvdkat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import org.apache.log4j.Logger;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.FileUtils;

/**
 * Wrapper for Common's Configuration
 * @author Szabolcs Kurdi
 */
public class Config {
    private PropertiesConfiguration config;
    private static Logger log = Logger.getLogger(Config.class);
    private SearchViewProperties searchViewProps = null;
    private boolean loaded = false;
    private int loadedQueryNum = 0;

    /**
     * set up a config file in the given directory; tries
     * to create the dir and the file if they don't exist
     * @param directory
     * @param fileName
     */
    public Config(String directory, String fileName) {
        String fullName = directory + "/" + fileName;
        File dir = new File(directory);
        File f = new File(fullName);

        try {
            if (!dir.exists()) {
                FileUtils.forceMkdir(dir);
            }
            if (!f.exists()) {
                f.createNewFile();
            }
        } catch(IOException e) {
            log.error("Could not create a configuration file (" + fileName + ")\n" + e.getMessage());
        }

        log.info("Loading configration \"" + fileName + "\" from " + directory);
        config = new PropertiesConfiguration();
        config.setDelimiterParsingDisabled(true);
        config.setFileName(fullName);
        config.setEncoding("UTF-8");
        try {
            config.load();
            loaded = true;
        } catch (ConfigurationException ex) {
            log.error("Could not load the configuration.\n" + ex.getMessage());
        }

    }

    /**
     * write the config to the disk
     */
    public void save() {
        if (!loaded) {
            return;
        }
        try {
            config.save();
        } catch (ConfigurationException e) {
            log.error("Could not save the configuration file.\n" + e.getMessage());
        }
    }

    /**
     * get the queries, property examples:
     * query1.name = in name attributes
     * query1.query = //*[@name contains text \"${PARAM}$\"]
     * @return
     */
    public ArrayList<SearchQueryItem> getQueries() {
        config.setDelimiterParsingDisabled(true);
        ArrayList<SearchQueryItem> queries = new ArrayList<SearchQueryItem>();
        ArrayList test;
        SearchQueryItem sqItem;
        int i = 1;
        boolean hasNext = true;
        String name;
        String query;
        String iS;

        while (hasNext) {
            iS = Integer.toString(i);
            name = (String) config.getProperty("query" + iS + ".name");
            query = (String) config.getString("query" + iS + ".query");

            if (name == null || query == null) {
                hasNext = false;
            } else {
                sqItem = new SearchQueryItem();
                sqItem.name = name;
                sqItem.query = query;
                queries.add(sqItem);
                i++;
            }
        }

        // add a default query item
        if (queries.isEmpty()) {
            sqItem = new SearchQueryItem();
            sqItem.name = "in name attributes";
            sqItem.query = "//*[contains(lower-case(@name), lower-case('${PARAM}$'))]";
            queries.add(sqItem);
            saveQueries(queries);
        }

        loadedQueryNum = queries.size();
        return queries;
    }

    /**
     * Save a list of search queries, flush unused ones
     * @param queries
     */
    public void saveQueries(ArrayList<SearchQueryItem> queries) {
        SearchQueryItem queryItem;
        String iS;
        int max = loadedQueryNum;
        if (queries.size() > max) {
            max = queries.size();
        }
        for (int i = 0; i < max; i++) {
            iS = Integer.toString(i + 1);
            if (i < queries.size()) {
                queryItem = queries.get(i);
                config.setProperty("query" + iS + ".name", queryItem.name);
                config.setProperty("query" + iS + ".query", queryItem.query);
            } else { // flush old
                config.clearProperty("query" + iS + ".name");
                config.clearProperty("query" + iS + ".query");
            }
        }
        // save now, since queries are pretty important...
        save();
    }

    public SearchViewProperties getSearchViewProperties() {
        if (searchViewProps != null) {
            return searchViewProps;
        }
        searchViewProps = new SearchViewProperties();
        try {
            searchViewProps.top = config.getInt("searchview.top");
            searchViewProps.left = config.getInt("searchview.left");
        } catch (Exception ex) {
            searchViewProps.top = 0;
            searchViewProps.left = 0;
        }
        try {
            searchViewProps.width = config.getInt("searchview.width");
            searchViewProps.height = config.getInt("searchview.height");
        } catch (Exception ex) {
            searchViewProps.width = 0;
            searchViewProps.height = 0;
        }
        try {
            String[] intArrTmp = config.getStringArray("searchview.column");
            for (int i = 0; i < searchViewProps.colWidths.length; i++) {
                searchViewProps.colWidths[i] = Integer.parseInt(intArrTmp[i]);
            }
        } catch (Exception ex) {}
        return searchViewProps;
    }

    public void saveSearchViewProperties(SearchViewProperties props) {
        searchViewProps = props;
        config.setProperty("searchview.top", props.top);
        config.setProperty("searchview.left", props.left);
        config.setProperty("searchview.width", props.width);
        config.setProperty("searchview.height", props.height);
        config.setProperty("searchview.column", props.colWidths);
    }

}
