JDvdKat
=======

Project status: **relic**

JDvdKat is a very simple DVD cataloging software with a crawler part and a gui part.

The crawler is based on Matt Haynes' [XML Directory Listing][1] application with added zip, 7zip, rar, id3, picture and descript.ion support; the crawler is a console application and uses XML to store the data.

The gui uses an integrated XML database called [BaseX][2]; all the files in the database can be imported from or exported to XML files for maximum transparency. With the gui one can store xml files in the database, manage databases and search for nodes using [XPath/XQuery][3].

Downloading
-----------

A built and zipped jar snapshot may be found at releases, this is the **last trunk/head build**. Download, unzip and launch with `java -Xms1024m -Dfile.encoding=UTF-8 -jar JDvdKat.jar` (see the wiki for more options).

Relic
-----

With the advance of cloud storage providers storing files on optical media is something of the past, I still have a couple of discs, but those have only a couple of bigger archives on them. This project is left here, but future releases are highly unlikely. TODOs:

- [ ] use current Netbeans
- [ ] update BaseX database
- [ ] use Netbeans' Swing GUI Builder (former Matisse)

[1]: http://code.google.com/p/xml-dir-listing/
[2]: http://www.inf.uni-konstanz.de/dbis/basex/
[3]: http://www.w3schools.com/xquery/xquery_reference.asp
[4]: https://drive.google.com/file/d/0BwHVlJsZjI6RVzRGT3RRUmZ1STA/edit?usp=sharing
