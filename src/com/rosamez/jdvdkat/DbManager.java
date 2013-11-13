/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.rosamez.jdvdkat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import org.apache.log4j.Logger;
import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.proc.Add;
import org.basex.core.proc.AlterDB;
import org.basex.core.proc.Close;
import org.basex.core.proc.CreateColl;
import org.basex.core.proc.Delete;
import org.basex.core.proc.DropDB;
import org.basex.core.proc.Export;
import org.basex.core.proc.List;
import org.basex.core.proc.Open;
import org.basex.core.proc.Optimize;
import org.basex.core.proc.XQuery;

/**
 * low level db operations
 * @author Szabolcs Kurdi
 */
public class DbManager {
    public static final String dbSuffix = "__jdk";
    static final Context ctx = new Context();
    private String activeDbName = null;
    private String activeDbNameShort = null;
    private static Logger log = Logger.getLogger(DbManager.class);

    public DbManager() {
    }

    /**
     * returns the shortened, active (opened) db name
     * @return
     */
    public String getActiveDbNameShort() {
        return activeDbNameShort;
    }

    /**
     * Return a list of available databases
     * @return
     */
    private String[] getDbNamesUnfiltered() {
        String[] dbs = List.list(ctx).finish();
        return dbs;
    }

    /**
     * adds a the db suffix to a string if it isn't there
     * TODO: is it on the end of string?
     * @param name
     * @return
     */
    private String addSuffix(String name) {
        if (name.indexOf(dbSuffix) == -1) {
            name += dbSuffix;
        }
        return name;
    }

    /**
     * get the filtered dbnames (ones that have the suffix)
     * @return dbNames names with suffix removed
     */
    public String[] getDbNames() {
        String[] dbs = getDbNamesUnfiltered();
        String[] filteredDbs;
        int count = 0;
        for (int i = 0; i < dbs.length; i++) {
            if (dbSuffix.equals(dbs[i])) {
                log.error("A db exists with an invalid (suffix only) name: " + dbSuffix);
            }
            if (dbs[i].indexOf(dbSuffix) > -1) {
                count++;
            }
        }
        filteredDbs = new String[count];
        count = 0;
        for (int i = 0; i < dbs.length; i++) {
            if (dbs[i].indexOf(dbSuffix) > -1) {
                filteredDbs[count] = dbs[i].replace(dbSuffix, "");
                count++;
            }
        }
        return filteredDbs;
    }

    /**
     * checks if a db name (no suffix) exists
     * @param name
     * @return
     */
    public boolean dbExists(String name) {
        String[] avail = getDbNames();
        return (Arrays.asList(avail).indexOf(name) > -1);
    }

    /**
     * Open a BaseX database
     * @param name
     * @return boolean success
     */
    public boolean openDb(String name) {
        String realName = addSuffix(name);
        if (!dbExists(name)) {
            return false;
        }
        try {
            new Open(realName).execute(ctx);
        } catch (BaseXException ex) {
            log.error("Could not open the db.\n" + ex.getMessage());
            return false;
        }
        activeDbName = realName;
        activeDbNameShort = name;
        return true;
    }

    /**
     * reopen the active db (in case we lost the connection)
     * @return
     */
    public boolean reOpenDb() {
        if (activeDbNameShort == null) {
            return false;
        }
        return openDb(activeDbNameShort);
    }

    /**
     * Close the current BaseX database
     */
    public void closeActiveDb() {
        if (activeDbName == null) {
            return;
        }
        try {
            new Close().execute(ctx);
            activeDbName = null;
            activeDbNameShort = null;
        } catch (BaseXException ex) {
            log.error(ex.getMessage());
        }
    }

    /**
     * Execute an XQuery
     * TODO: throw exception!
     * @param query
     * @return String the stringified byteArray
     */
    public String executeQuery(String query) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try {
            new XQuery(query).execute(ctx, buffer);
        } catch (BaseXException ex) {
            log.error("Could not execute query.\n" + ex.getMessage());
            buffer = null;
        }
        try {
            return buffer.toString("UTF-8");
        } catch(OutOfMemoryError e) {
            log.fatal("Out of memory error; reached maximum heap size at " + buffer.size() + " bytes.");
            return "";
        } catch (UnsupportedEncodingException e) {
            log.fatal("Unsupported encoding (UTF-8).");
            return "";
        }
    }

    /**
     * create collection
     * @param dbName
     * @return
     */
    public boolean createDb(String dbName) {
        String realName = addSuffix(dbName);
        try {
            new CreateColl(realName).execute(ctx, System.out);
        } catch (BaseXException ex) {
            log.error("Could not create database.\n" + ex.getMessage());
            return false;
        }
        return true;
    }

    /**
     * add file and optimize, useful for single file import
     * @param file
     * @return
     */
    public boolean addFile(File file) {
        return addFile(file.getAbsolutePath(), true);
    }

    /**
     * add file wo optimization; useful for batch import
     * @param file
     * @return
     */
    public boolean addFileNoOpt(File file) {
        return addFile(file.getAbsolutePath(), false);
    }

    /**
     * add file to db, may or may not optimize
     * @param fileName
     * @param autoOptimize
     * @return
     */
    public boolean addFile(String fileName, boolean autoOptimize) {
        try {
            if (docExists(fileName)) {
                log.error("File already exists in db, please delete first!");
                return false;
            } else {
                new Add(fileName).execute(ctx, System.out);
                if (autoOptimize) {
                    optimize();
                }
            }
        } catch (BaseXException ex) {
            log.error("Could not add the file to the db!\n" + ex.getMessage());
            return false;
        }
        return true;
    }

    /**
     * basic optimize; use the BaseX gui for more...
     */
    public void optimize() {
        try {
            new Optimize().execute(ctx, System.out);
        } catch (BaseXException ex) {
            log.error("Could not optimize the db.\n" + ex.getMessage());
        }
    }

    /**
     * Removes an xml file from the db, for some reason
     * Delete doesn't throw a BaseXException
     * @param name short filename
     */
    public void removeFile(String name) {
        new Delete(name).exec(ctx, System.out);
    }

    /**
     * Exports all the xml files from the database
     * @param path target dir for the docs
     * @return
     */
    public boolean exportDb(String path) {
        try {
            new Export(path).execute(ctx, System.out);
        } catch (BaseXException ex) {
            log.error("Could not export the database!\n" + ex.getMessage());
            return false;
        }
        return true;
    }

    /**
     * drop database
     * @param name
     * @return
     */
    public boolean deleteDb(String name) {
        name = addSuffix(name);
        try {
            new DropDB(name).execute(ctx);
        } catch (BaseXException ex) {
            log.error("Could not drop the database \"" + name + "\"!\n" + ex.getMessage());
            return false;
        }
        // BaseX drops the connection, even if we deleted a db which was not active
        if (!name.equals(activeDbName)) {
            reOpenDb();
        } else {
            
        }
        return true;
    }

    /**
     * since BaseX 6.1.7 (?) same filenames will not throw an exception
     * which is fine, but nevertheless in my case a check needs to be done
     * @param name
     * @return
     */
    private boolean docExists(String name) {
        name = name.replaceAll("\\\\", "/");
        name = name.substring(name.lastIndexOf("/"));
        String query = "for $doc in collection()"
                + "return concat(base-uri($doc), '&#xA;')";
        String response = executeQuery(query);
        String[] baseUris = (response == null) ? null : response.split("\n ?");
        for (int i = 0; i < baseUris.length; i++) {
            String bUri = baseUris[i].replaceAll("\\\\", "/");
            if (name.equals(bUri.substring(bUri.lastIndexOf("/")))) {
                return true;
            }
        }
        return false;
    }

    public boolean renameDbTo(String dbName) {
        if (dbExists(dbName)) {
            return false;
        }
        dbName = addSuffix(dbName);
        try {
            new AlterDB(activeDbName, dbName).exec(ctx, System.out);
        } catch (Exception ex) { // no BaseXException?
            log.error("Could not rename the database \"" + activeDbName + "\" to \"" + dbName + "\"!\n" + ex.getMessage());
            return false;
        }
        return true;
    }

}
