/*
 * JDvdKatApp.java
 */

package com.rosamez.jdvdkat;

import com.rosamez.basexutil.AddToCollection;
import com.rosamez.convert.Convert;
import com.rosamez.dirlistex.DirectoryListing;
import java.io.File;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.basex.BaseXWin;
import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;

/**
 * The main class of the application.
 */
public class JDvdKatApp extends SingleFrameApplication {
    public DbManager dbManager = new DbManager();
    public DvdKatModel dvdKatModel = new DvdKatModel(dbManager);
    public SearchQueryManager searchQueryManager;
    public Config config = null;
    private static Logger log = Logger.getLogger(JDvdKatApp.class);

    JDvdKatView mainWindow;

    protected String defaultDbName = "jdvdkat";

    @Override protected void initialize(String[] args) {
        String dbName = null;
        Boolean useDefault = false;

        if (args.length > 0) {
            dbName = args[0];
        } else {
            dbName = defaultDbName;
            useDefault = true;
        }

        if ((!dbManager.dbExists(dbName)) && (useDefault)) {
            if (!dbManager.createDb(dbName)) {
                exit();
                return;
            } else {
                log.info("Created new database.");
            }
        }

        if (!dbManager.openDb(dbName)) {
            log.error("Could not open the specified database.");
            exit();
            return;
        }
    }

    /**
     * At startup create and show the main frame of the application.
     */
    @Override protected void startup() {
        File f = getContext().getLocalStorage().getDirectory();
        String loc = f.getAbsolutePath();
        config = new Config(loc, "jdvdkat.properties");
        searchQueryManager = new SearchQueryManager(config);

        mainWindow = new JDvdKatView(this);
        mainWindow.updateTitle();
        mainWindow.populateListFiles();

        show(mainWindow);
    }

    /**
     * This method is to initialize the specified window by injecting resources.
     * Windows shown in our application come fully initialized from the GUI
     * builder, so this additional configuration is not needed.
     */
    @Override protected void configureWindow(java.awt.Window root) {
    }

    /**
     * A convenient static getter for the application instance.
     * @return the instance of JDvdKatApp
     */
    public static JDvdKatApp getApplication() {
        return Application.getInstance(JDvdKatApp.class);
    }

    /**
     * Main method launching the application.
     */
    public static void main(String[] args) {
        String[] newArgs = args;

        // quick and dirty logging to stdout
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.ERROR);

        // default character set may not be UTF-8!
        // probably not needed with the latest string conversion fix added,
        // but that one I'd like to test a bit more thoroughly...
        if (!"UTF-8".equals(System.getProperty("file.encoding"))) {
            System.out.println("Your system's default character set " +
                    "is not UTF-8, please specify at commandline with the " +
                    "'-Dfile.encoding=UTF8' java/jvm option!");
        }
            
        
        if (args.length > 0) {
            newArgs = popFirstParam(args);
            // crawler mode
            if ("basex".equals(args[0])) {
                BaseXWin.main(newArgs);
                return;
            } else if ("crawl".equals(args[0])) {
                DirectoryListing.main(newArgs);
                return;
            } else if ("convert".equals(args[0])) {
                Convert.main(newArgs);
                return;
            } else if ("addtodb".equals(args[0])) {
                AddToCollection.main(newArgs);
                return;
            } else if("db".equals(args[0])) { // gui mode with db
                if (args.length < 2) {
                    System.err.println("Please specify a database!");
                    return;
                }
            } else if (("--help".equals(args[0])) || ("-h".equals(args[0]))) { // help
                System.out.println("To use the crawler: JDvdKat crawl -h\n" +
                        "To add an xml file to a db: JDvdKat addtodb -h\n" +
                        "To start the converter: JDvdKat convert -h\n" +
                        "To start the gui with a db: JDvdKat db [dbname]\n" +
                        "To start the gui (default database is \"jdvdkat\"): JDvdKat\n" +
                        "To start the BaseX gui: JDvdKat basex\n" +
                        "(BaseX is developed by Christian Grün and Alexander" +
                        "Holupirek in the DBIS Research Group, led by Marc H. Scholl)");
                return;
            }
        }

        launch(JDvdKatApp.class, newArgs);
    }
    
    @Override protected void shutdown() {
        dbManager.closeActiveDb(); // is it okay or shall I use a listener?
        if (config != null) {
            config.save();
        }
        super.shutdown();
    }

    protected static String[] popFirstParam(String[] args) {
        String[] newArgs = new String[args.length - 1];
        if (args.length > 1) {
            for (int i = 1; i < args.length; i++) {
                newArgs[i - 1] = args[i];
            }
        }
        return newArgs;
    }

}
