JDvdKat
=======

JDvdKat is a very simple DVD cataloging software with a crawler part and a gui part.

The crawler is based on Matt Haynes' [XML Directory Listing][1] application with added zip, 7zip, rar, id3, picture and descript.ion support; the crawler is a console application and uses XML to store the data.

The gui uses an integrated XML database called [BaseX][2]; all the files in the database can be imported from or exported to XML files for maximum transparency. With the gui one can store xml files in the database, manage databases and search for nodes using [XPath/XQuery][3].

[1]: http://code.google.com/p/xml-dir-listing/
[2]: http://www.inf.uni-konstanz.de/dbis/basex/
[3]: http://www.w3schools.com/xquery/xquery_reference.asp