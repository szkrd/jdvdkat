package com.rosamez.dirlistex;

import java.io.File;
import java.io.IOException;
import org.blinkenlights.jid3.*;
import org.blinkenlights.jid3.v1.*;
import org.blinkenlights.jid3.v2.*;

public class Mp3File {

    public String title = "";
    public String artist = "";
    public String album = "";
    public int year = -1;
    public long playTime = -1;
    public String genre = "";

    public Mp3File(File file) throws IOException {
        File oSourceFile = file;
        MediaFile oMediaFile = new MP3File(oSourceFile);
        ID3Tag[] aoID3Tag = null;
        try {
            aoID3Tag = oMediaFile.getTags();
        } catch (ID3Exception e1) {
            throw new IOException();
        }

        // let's loop through and see what we've got
        for (int i = 0; i < aoID3Tag.length; i++) {
            // check to see if we read a v1.0 tag, or a v2.3.0 tag
            if (aoID3Tag[i] instanceof ID3V1_0Tag) {

                ID3V1_0Tag oID3V1_0Tag = (ID3V1_0Tag) aoID3Tag[i];

                if (oID3V1_0Tag.getTitle() != null) {
                    this.title = oID3V1_0Tag.getTitle().trim();
                }

                if (oID3V1_0Tag.getArtist() != null) {
                    this.artist = oID3V1_0Tag.getArtist().trim();
                }

                if (oID3V1_0Tag.getAlbum() != null) {
                    this.album = oID3V1_0Tag.getAlbum().trim();
                }

                if (oID3V1_0Tag.getYear() != null) {
                    String yearStr = oID3V1_0Tag.getYear().trim();
                    if (!"".equals(yearStr)) {
                        try {
                            this.year = Integer.parseInt(yearStr);
                        } catch (NumberFormatException e) {
                            this.year = -1;
                        }
                    }
                }

                if (oID3V1_0Tag.getGenre() != null) {
                    this.genre = oID3V1_0Tag.getGenre().toString().trim();
                }

            } else if (aoID3Tag[i] instanceof ID3V2_3_0Tag) {

                ID3V2_3_0Tag oID3V2_3_0Tag = (ID3V2_3_0Tag) aoID3Tag[i];
                try {
                    if (oID3V2_3_0Tag.getTitle() != null) {
                        this.title = oID3V2_3_0Tag.getTitle().trim();
                    }

                    if (oID3V2_3_0Tag.getArtist() != null) {
                        this.artist = oID3V2_3_0Tag.getArtist().trim();
                    }

                    if (oID3V2_3_0Tag.getAlbum() != null) {
                        this.title = oID3V2_3_0Tag.getAlbum().trim();
                    }

                    if (oID3V2_3_0Tag.getYear() > 0) {
                        this.year = oID3V2_3_0Tag.getYear(); // is int
                    }

                    if (oID3V2_3_0Tag.getGenre() != null) {
                        this.genre = oID3V2_3_0Tag.getGenre(); // coming from a list
                    }

                    TLENTextInformationID3V2Frame tLen = oID3V2_3_0Tag.getTLENTextInformationFrame();
                    if (tLen != null) {
                        this.playTime = tLen.getTrackLength();
                    }

                } catch (ID3Exception e) {
                }
            }
        }
    }
}
