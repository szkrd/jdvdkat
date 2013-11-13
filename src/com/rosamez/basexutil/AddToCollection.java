package com.rosamez.basexutil;

/**
 * this was a standalone utility; TODO: integrate properly into main app!
 *
 * DEPENDENCIES:
 * Commons CLI (1.1)
 * BaseX (6.1)
 *
 */
import com.rosamez.jdvdkat.DbManager;
import java.io.*;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.proc.Add;
import org.basex.core.proc.Close;
import org.basex.core.proc.CreateColl;
import org.basex.core.proc.Delete;
import org.basex.core.proc.Open;
import org.basex.core.proc.Optimize;

public class AddToCollection {

    private static Options opt = new Options();

    /**
     * Simple tool to add an XML file to a BaseX db
     * @param args	command line arguments
     */
    public static void main(String[] args) {

        opt.addOption("h", "help", false, "print this message");
        opt.addOption("o", "overwrite", false, "overwrite XML document if"
                + " it exists in the DB");

        // set up the cli parser
        BasicParser parser = new BasicParser();
        CommandLine cl;
        try {
            cl = parser.parse(opt, args);
        } catch (ParseException ex) {
            System.err.println("Could not initialize the commandline parser." + ex.getMessage());
            return;
        }

        // help message
        if (cl.hasOption("h") || args.length < 2) {
            System.out.println("Inject an XML file into a BaseX database.");
            HelpFormatter f = new HelpFormatter();
            f.printHelp("executable [options] XML_file BaseX_db_name", "\n options:", opt, "");
            return;
        }

        // get the arguments
        String xmlFileName = cl.getArgs()[0];
        File xmlFile = new File(xmlFileName);
        String appDb = cl.getArgs()[1] + DbManager.dbSuffix;
        Context context = new Context();

        // try to open the database, or create a new one if it doesn't exist
        try {
            new Open(appDb).execute(context, System.out);
        } catch (BaseXException e) {
            if (e.getMessage().indexOf("was not found.") > -1) { // yuck
                System.out.println("Creating new collection database.");
                try {
                    new CreateColl(appDb).execute(context, System.out);
                } catch (BaseXException ex) {
                    System.err.println("Could not create the db.");
                    return;
                }
            } else {
                System.err.println(e.getMessage());
                return;
            }
        }

        // try to add the XML file, or delete and add if it already exists
        // in the db (overwrite option)
        try {
            new Add(xmlFile.getAbsolutePath()).execute(context, System.out);
        } catch (BaseXException e) {
            if (e.getMessage().indexOf("exists already.") > -1) {
                if (cl.hasOption("o")) {
                    try {
                        new Delete(xmlFile.getAbsolutePath()).execute(context, System.out);
                        new Add(xmlFile.getAbsolutePath()).execute(context, System.out);
                        new Optimize().execute(context, System.out);
                    } catch (BaseXException ex) {
                        System.err.println(ex.getMessage()); // this is extraordinarily sloppy
                        return;
                    }
                } else {
                    System.err.println("The document \"" + xmlFileName + "\""
                            + " already exists in the collection, won't overwite.");
                }
            } else {
                System.err.println(e.getMessage());
            }
        }

        // close the db
        try {
            new Close().execute(context, System.out);
        } catch (BaseXException ex) {
            System.err.println(ex.getMessage());
        }
    }
}
