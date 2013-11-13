package com.rosamez.dirlistex;

import java.io.*;
import java.util.Date;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.ParseException;

/**
 * (I have pulled this one here from Eclipse, where it was a standalone project)
 *
 * DirectoryListing.java from xml-dir-listing
 * Command line interface for XML Directory Listing class.
 */
public final class DirectoryListing {

    /** CLI options object */
    private static Options opt = new Options();
    /** XmlDirectoryListing object */
    private static XmlDirectoryListingEx lister = new XmlDirectoryListingEx();
    /** Outputstream for XmlDirectoryListing object */
    private static FileOutputStream out;
    /** The directory to list */
    private static File dir;

    // Main function
    public static void main(String aArguments[]) {

        try {
            // Define options
            opt.addOption("h", "help", false, "print this message");
            opt.addOption("s", "sort", true, "sort method");
            opt.addOption("r", "reverse", false, "sort reverse");
            opt.addOption("o", "output", true, "output file");
            opt.addOption("v", "verbose", false, "verbose logging");
            opt.addOption("vv", "moreverbose", false, "more verbose logging");
            opt.addOption("f", "dateformat", true, "date format for listings");
            opt.addOption("d", "depth", true, "depth of directory listings");
            opt.addOption("i", "includes", true, "includes regEx for directory listings");
            opt.addOption("e", "excludes", true, "excludes regEx for directory listings");
            opt.addOption("c", "encoding", true, "sets character encoding definition for XML file (default is UTF-8)");

            // extras
            opt.addOption("a", "archive", false, "allow parsing of rar, zip and 7z files");
            opt.addOption("m", "mp3", false, "fetch idv1/idv2 info from mp3 files");
            opt.addOption("p", "picture", false, "parse pictures for width and height");
            opt.addOption("dion", "parsedion", false, "get comments from DOS ISO-8859-1 descript.ion files");

            // Parse arguments
            BasicParser parser = new BasicParser();
            CommandLine cl = parser.parse(opt, aArguments);

            // If help or no args,
            if (cl.hasOption("h") || aArguments.length == 0) {

                // Display help and return (exit).
                HelpFormatter f = new HelpFormatter();
                //f.printHelp("xml-dir-list [options] source", "\n options:", opt, "");
                f.printHelp("JDvdKat crawl [options] source", "\n options:", opt, "");
                return;

            }

            // Check for output option and and apply it to XmlDirectoryListing class, otherwise fail.
            if (cl.hasOption("o")) {
                File tempFile = new File(cl.getOptionValue("o"));
                if (tempFile.isDirectory()) {
                    System.out.println("No file set.");
                    return;
                } else {
                    tempFile = tempFile.getParentFile();
                }                
                if (!tempFile.exists()) {
                    System.out.println("Destination doesn't exist.");
                    return;
                }
                try {
                    // Set output stream for generated file
                    out = new FileOutputStream(cl.getOptionValue("o"));
                } catch (FileNotFoundException ex) {}
            } else {
                System.out.println("Please specify an output file.");
                return;
            }

            // Allow archive parsing
            if (cl.hasOption("a")) {
                lister.setArchiveParsing(true);
            }

            // Allow mp3 parsing
            if (cl.hasOption("m")) {
                lister.setMp3Parsing(true);
            }

            // Allow picture parsing
            if (cl.hasOption("p")) {
                lister.setPictureParsing(true);
            }

            // Allow descript.ion parsing
            if (cl.hasOption("dion")) {
                lister.setDosDescriptIonParsing(true);
            }

            // Check for sort option and apply it to XmlDirectoryListing class
            if (cl.hasOption("s")) {
                lister.setSort(cl.getOptionValue("s"));
            }

            // Check for character encoding
            if (cl.hasOption("c")) {
                lister.setEncoding(cl.getOptionValue("c"));
            }

            // Check for reverse option and apply it to XmlDirectoryListing class
            if (cl.hasOption("r")) {
                lister.setSortReverse(true);
            }

            // Check for dateformat option and apply it to XmlDirectoryListing class
            if (cl.hasOption("f")) {
                lister.setDateFormat(cl.getOptionValue("f"));
            }

            // Check for dateformat option and apply it to XmlDirectoryListing class
            if (cl.hasOption("d")) {
                lister.setDepth(Integer.valueOf(cl.getOptionValue("d")).intValue());
            }

            // Check for includes option and apply it to XmlDirectoryListing class
            if (cl.hasOption("i")) {
                lister.setIncluded(cl.getOptionValue("i"));
            }

            // Check for excludes option and apply it to XmlDirectoryListing class
            if (cl.hasOption("e")) {
                lister.setExcluded(cl.getOptionValue("e"));
            }

            // Check for verbose flag. Set logger accordingly.
            if (cl.hasOption("v") || cl.hasOption("vv")) {
                if (cl.hasOption("v")) {
                    lister.log.setLevel(org.apache.log4j.Level.INFO);
                } else {
                    lister.log.setLevel(org.apache.log4j.Level.DEBUG);
                }
            } else {
                lister.log.setLevel(org.apache.log4j.Level.OFF);
            }

            // Check for directory as last remaining argument
            if (cl.getArgs().length == 1) {
                // Get specified directory
                dir = new File(cl.getArgs()[0]);
            } else if (cl.getArgs().length > 1) {
                System.out.println("Too many arguments specified.");
                return;
            } else {
                System.out.println("Please specify a directory to generate a listing for");
                return;
            }


            // Run Class ========================================================

            // Begin listing
            Date startDate = new Date();
            lister.generateXmlDirectoryListing(dir, out);

            Date endDate = new Date();
            long timeElapsed = endDate.getTime() - startDate.getTime();
            if (lister.processedFileCount > 0) {
                System.out.println("Processed " + lister.processedFileCount + " file(s) in " + timeElapsed / 1000 + " second(s).");
            } else {
                System.out.println("Haven't done anything.");
            }

            try {
                // Close output stream
                out.close();
            } catch (IOException ex) {
                System.err.println("Could not close the file." + ex);
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }

    }
}
