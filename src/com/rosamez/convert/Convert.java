package com.rosamez.convert;

import java.io.File;
import java.io.IOException;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;

/**
 * convert some kind of a catalog file to an xml file(s)
 * currently only the DiskDir Total Commander plugin is supported and
 * probably I'll not hassle with adding more formats ( I have checked
 * Hyper's CDCat, but his format is a lossy garbage).
 *
 * @author Szabolcs Kurdi
 */
public final class Convert {

    private static Options opt = new Options();
    private static File inputFile;
    private static File outputFile;

    public static void main(String aArguments[]) {
        opt.addOption("h", "help", false, "print this message");
        opt.addOption("f", "format", true, "input format; currently supported (default): DiskDir");

        BasicParser parser = new BasicParser();
        boolean mayProceed = false;
        boolean maySave = false;
        String format = null;

        try {
            CommandLine cl = parser.parse(opt, aArguments);
            if (cl.hasOption("h") || aArguments.length == 0) {
                HelpFormatter f = new HelpFormatter();
                f.printHelp("JDvdKat convert [options] input_file output_XML_file", "\n options:", opt, "");
                return;
            }

            if (cl.hasOption("f")) {
                format = cl.getOptionValue("f");
            } else {
                format = "DiskDir";
            }

            if (!"DiskDir".equals(format)) {
                System.out.println("Unknown format.");
                return;
            }

            if ("DiskDir".equals(format)) {
                System.out.println("Warning! DiskDir XML format will slightly"
                        + " differ from JDvdKat's original format (archive handling)"
                        + " and the DiskDir plugin has some bugs in it"
                        + " (international characters, directories vs archives)!");
            }

            if (cl.getArgs().length == 2) {
                inputFile = new File(cl.getArgs()[0]);
                outputFile = new File(cl.getArgs()[1]);
                mayProceed = true;
            } else if (cl.getArgs().length > 2) {
                System.out.println("Too many arguments specified.");
                return;
            } else if (cl.getArgs().length < 2) {
                System.out.println("Not enough arguments specified.");
                return;
            }

        } catch (ParseException ex) {
            System.err.println("Commandline parse error. " + ex.getMessage());
        }

        if (!mayProceed) {
            System.err.println("Could not start parsing.");
            return;
        }

        String result = null;

        // do the DiskDir format conversion
        if ("DiskDir".equals(format)) {
            DiskDirConverter conv = new DiskDirConverter(inputFile);
            result = conv.convert();
        }

        // add header
        if (result != null && !"".equals(result)) {
            result = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + result; // add header
            maySave = true;
        }

        // save result string to file
        try {
            if (maySave) {
                FileUtils.writeStringToFile(outputFile, result, "utf8");
                System.out.println("Done.");
            }
        } catch (IOException ex) {
            System.err.println("Could not save the file. " + ex.getMessage());
        }

    }
}
