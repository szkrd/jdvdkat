package com.rosamez.dirlistex;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import javax.swing.filechooser.FileSystemView;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import net.matthaynes.xml.dirlist.XmlDirectoryListing;

/**
 * XmlDirectoryListing Extras: subclasses Matt's original class;
 * adds archive support
 */
public class XmlDirectoryListingEx extends XmlDirectoryListing {

    protected String[] archiveExtensions = {"zip", "7z", "rar"};
    protected String[] pictureExtensions = {"png", "gif", "tiff", "bmp", "jpg", "jpeg", "xmp"}; // @see: http://incubator.apache.org/sanselan/site/formatsupport.html; WARN: removed "ico", caused many problems
    protected boolean allowArchiveParsing = false;
    protected boolean allowMp3Parsing = false;
    protected boolean allowPictureParsing = false;
    protected boolean allowDosDescriptIonParsing = false;
    protected DescriptIonParser dosDescriptions = new DescriptIonParser();
    public long processedFileCount = 0;

    public void setArchiveParsing(boolean flag) {
        allowArchiveParsing = flag;
    }

    public void setMp3Parsing(boolean flag) {
        allowMp3Parsing = flag;
    }

    public void setPictureParsing(boolean flag) {
        allowPictureParsing = flag;
    }

    public void setDosDescriptIonParsing(boolean flag) {
        allowDosDescriptIonParsing = flag;
    }

    /**
     * Creates a new element based on the file based through.
     * (Copied from Matt's super class, adds extras)
     * @param file The file on which the element is based.
     */
    @Override
    public void createElement(final File file, final int depth, boolean isRoot) {

        log.debug("Analysing " + file.getAbsolutePath());
        processedFileCount++;

        if (file.isDirectory() || this.isIncluded(file) && !this.isExcluded(file) || isRoot == true) {

            if (isRoot) {
                this.setAttributes(file, true);
                // fix my abspath sanitization stuff here; @see setAttributes in Matt's code
                atts.addAttribute("", "", "absolutePath", "CDATA", SimpleFileUtils.sanitizePath(file.getAbsolutePath()));
            } else {
                this.setAttributes(file);
            }

            if ((file.isDirectory()) && (file.getParentFile() == null)) {
                nodeAsVolume(file);
            }

            // parse DOS descript.ion file ASAP (directory) or get comment from buffer (file)
            if (allowDosDescriptIonParsing) {
                parseDosDescriptIon(file);
            }

            String fileType = (file.isDirectory()) ? "directory" : "file";
            String fileName = file.getName();
            String fileExtension = SimpleFileUtils.getSuffix(fileName);
            boolean fileIsArchive = false;
            boolean fileIsMp3 = false;
            boolean fileIsPicture = false;

            if (fileExtension != null) {
                if (Arrays.asList(archiveExtensions).contains(fileExtension)) {
                    fileIsArchive = true;
                }
                if (fileExtension.equals("mp3")) {
                    fileIsMp3 = true;
                }
                if (Arrays.asList(pictureExtensions).contains(fileExtension)) {
                    fileIsPicture = true;
                }
            }

            try {

                log.debug("Starting element " + file.getAbsolutePath());

                if (fileIsMp3 && allowMp3Parsing) {
                    setMp3Attributes(file);
                }

                if (fileIsPicture && allowPictureParsing) {
                    setPictureAttributes(file);
                }

                // Output details of the file
                hd.startElement("", fileType, fileType, atts);

                if ("directory".equals(fileType)) {
                    this.parseDirectory(file, depth);
                } else if (fileIsArchive && allowArchiveParsing) {
                    this.parseArchive(file, fileExtension);
                }

                log.debug("Ending element " + file.getAbsolutePath());

                hd.endElement("", "", fileType);

            } catch (SAXException e) {
            }
        }

    }

    /**
     * Sets all attributes for the file
     * @param file The file to set attributes for
     */
    @Override
    public void setAttributes(File file) {
        log.debug("Setting attributes for: " + file.getAbsolutePath());
        atts.clear();
        atts.addAttribute("", "", "name", "CDATA", file.getName());
        atts.addAttribute("", "", "size", "CDATA", String.valueOf(file.length()));
        atts.addAttribute("", "", "lastModified", "CDATA", String.valueOf(file.lastModified()));
        // filename suffix, like in BaseX's DeepFile xml
        String suffix = SimpleFileUtils.getSuffix(file); // TODO: with archives too!
        if (suffix != null) {
            atts.addAttribute("", "", "suffix", "CDATA", suffix);
        }
        atts.addAttribute("", "", "date", "CDATA", this.dateFormat.format(new Date(file.lastModified())));
        atts.addAttribute("", "", "absolutePath", "CDATA", SimpleFileUtils.sanitizePath(file.getAbsolutePath()));
    }

    /**
     * Calls the appropriate ArchiveReader class, then passes
     * the items in the resulting flat list to the node builder
     * (createArchiveElement).
     * @param file	archive file
     * @param ext	filename extension
     */
    protected void parseArchive(File file, String ext) {
        log.debug("Archive: " + file.getName());
        ArrayList<ArchivedFile> flatList = new ArrayList<ArchivedFile>();
        boolean hasParser = false;
        ZipReader reader = null;

        if (ext.equals("zip")) {
            reader = new ZipReader(file);
            hasParser = true;
        } else if (ext.equals("7z")) {
            reader = new SevenZipReader(file);
            hasParser = true;
        } else if (ext.equals("rar")) {
            reader = new RarReader(file);
            hasParser = true;
        }

        if (hasParser) {
            try {
                flatList = reader.getListOfFiles();
                for (int i = 0; i < flatList.size(); i++) {
                    createArchiveElement(flatList.get(i), file);
                }
            } catch (Exception e) {
                log.error("Could not parse reliably the archive \"" + file.getAbsolutePath() + "\"");
            }
        }

    }

    /**
     * Creates flat/single node for a file inside an archive;
     * based on createElement; does not support depth /
     * fake directory traversal.
     * @param item	item inside the archived file
     */
    protected void createArchiveElement(ArchivedFile item, File forFile) {

        String fileType = "file";
        AttributesImpl arAttr = new AttributesImpl();
        String name = SimpleFileUtils.sanitizePath((String) item.name);
        String suffix = SimpleFileUtils.getSuffix(name);

        arAttr.clear();
        arAttr.addAttribute("", "", "packed", "CDATA", "true");
        arAttr.addAttribute("", "", "name", "CDATA", name);
        if (suffix != null) {
            arAttr.addAttribute("", "", "suffix", "CDATA", suffix);
        }
        arAttr.addAttribute("", "", "size", "CDATA", String.valueOf(item.size));
        arAttr.addAttribute("", "", "lastModified", "CDATA", String.valueOf(item.date));
        // absPath is needed for the exact xpath loaction
        String fakePath = SimpleFileUtils.sanitizePath(forFile.getAbsolutePath() + "/" + (String) item.name);
        arAttr.addAttribute("", "", "absolutePath", "CDATA", fakePath);

        try {
            log.debug("Starting archive element " + item.name);
            hd.startElement("", fileType, fileType, arAttr);
            log.debug("Ending archive element " + item.name);
            hd.endElement("", "", fileType);
        } catch (SAXException e) {
        }
    }

    /**
     * Adds some of the mp3 metadata as attributes
     * @param file	mp3 file
     */
    protected void setMp3Attributes(File file) {
        try {
            Mp3File mp3File = new Mp3File(file);//todo
            if (!mp3File.title.equals("")) {
                atts.addAttribute("", "", "title", "CDATA", stripNonValidXMLCharacters((String) mp3File.title));
            }
            if (!mp3File.album.equals("")) {
                atts.addAttribute("", "", "album", "CDATA", stripNonValidXMLCharacters((String) mp3File.album));
            }
            if (!mp3File.artist.equals("")) {
                atts.addAttribute("", "", "artist", "CDATA", stripNonValidXMLCharacters((String) mp3File.artist));
            }
            if (mp3File.year > -1) {
                atts.addAttribute("", "", "year", "CDATA", String.valueOf(mp3File.year));
            }
            if (mp3File.playTime > -1) {
                atts.addAttribute("", "", "playTime", "CDATA", stripNonValidXMLCharacters(String.valueOf(mp3File.playTime)));
            }
            if (!mp3File.genre.equals("")) {
                atts.addAttribute("", "", "genre", "CDATA", stripNonValidXMLCharacters(mp3File.genre));
            }
        } catch (IOException e) {
            log.error("Mp3 file idv1/idv2 read error in \"" + file.getAbsolutePath() + "\"");
        }
    }

    /**
     * Add width and height as attributes using Sanselan
     * @param file	picture file
     */
    protected void setPictureAttributes(File file) {
        try {
            PictureFile picFile = new PictureFile(file);
            if (picFile.width > -1) {
                atts.addAttribute("", "", "width", "CDATA", String.valueOf(picFile.width));
            }
            if (picFile.height > -1) {
                atts.addAttribute("", "", "height", "CDATA", String.valueOf(picFile.height));
            }
        } catch (Exception e) { // it may be a null pointer too, not just IOEx
            log.error("Picture file read error in \"" + file.getAbsolutePath() + "\"");
        }
    }

    /**
     * Looks for DOS descript.ion file and parses its contents
     * or sets the global attribute (adds comment) if it's a file with comment
     * @param file	directory or file
     */
    protected void parseDosDescriptIon(File file) {
        if (file.isDirectory()) {
            try {
                dosDescriptions.testDirectory(file);
            } catch (IOException e) {
                log.error("Found a DOS descript.ion file, but could not parse it.");
            }
        }

        if (dosDescriptions.hasDIonComments(file)) {
            String dionDesc = dosDescriptions.getCommentsFor(file);
            atts.addAttribute("", "", "comment", "CDATA", stripNonValidXMLCharacters(dionDesc));
        }
    }

    /**
     * Sets the volume and label attributes, based on the file
     * @param file
     */
    protected void nodeAsVolume(File file) {
        atts.addAttribute("", "", "volume", "CDATA", "true");

        FileSystemView view = FileSystemView.getFileSystemView();
        String volName = view.getSystemDisplayName(file);
        if (volName == null) {
            return;
        }

        // "try" to remove (DRIVELETTER:)
        volName = volName.trim();
        int idx = volName.lastIndexOf(" (");
        boolean endsWithPar = volName.substring(volName.length() - 2).equals(":)");
        if ((idx > -1) && (endsWithPar)) {
            volName = volName.substring(0, idx);
        }

        if (!volName.equals("")) {
            atts.addAttribute("", "", "label", "CDATA", volName);
        }
    }

    /**
     * from: http://cse-mjmcl.cse.bris.ac.uk/blog/2007/02/14/1171465494443.html
     *
     * This method ensures that the output String has only
     * valid XML unicode characters as specified by the
     * XML 1.0 standard. For reference, please see
     * <a href="http://www.w3.org/TR/2000/REC-xml-20001006#NT-Char">the
     * standard</a>. This method will return an empty
     * String if the input is null or empty.
     *
     * @param in The String whose non-valid characters we want to remove.
     * @return The in String, stripped of non-valid characters.
     */
    protected String stripNonValidXMLCharacters(String in) {
        StringBuffer out = new StringBuffer(); // Used to hold the output.
        char current; // Used to reference the current character.

        if (in == null || ("".equals(in))) return ""; // vacancy test.
        for (int i = 0; i < in.length(); i++) {
            current = in.charAt(i); // NOTE: No IndexOutOfBoundsException caught here; it should not happen.
            if ((current == 0x9) ||
                (current == 0xA) ||
                (current == 0xD) ||
                ((current >= 0x20) && (current <= 0xD7FF)) ||
                ((current >= 0xE000) && (current <= 0xFFFD)) ||
                ((current >= 0x10000) && (current <= 0x10FFFF)))
                out.append(current);
        }
        return out.toString();
    }

}
