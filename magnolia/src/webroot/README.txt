===========================================================

              M A G N O L I A (TM)
     Simple Enterprise Content Management

               1. February 2008
             v 3.5.4
               Community Edition

           http://www.magnolia.info/

===========================================================

The basic Magnolia webapp on which projects can depend.

More info about Magnolia can be found at :
    http://documentation.magnolia.info

Change log and known issues can be found on Jira at :
    http://jira.magnolia.info/browse/MAGNOLIA

Community-powered documentation can be found at:
    http://wiki.magnolia.info

===========================================================
Overview
===========================================================
Welcome to Magnolia 3.5.4

These notes contain information to get you up & running
with Magnolia.

===========================================================
Magnolia Package
===========================================================
The Package contains:

   magnolia-core 3.5.4
   magnolia-editor-fckeditor 3.5.4
   magnolia-gui 3.5.4
   magnolia-jaas 3.5.4
   magnolia-module-admininterface 3.5.4
   magnolia-module-cache 3.5.4
   magnolia-module-exchange-simple 3.5.4
   magnolia-module-mail 3.5.4
   magnolia-module-templating 3.5.4
   magnolia-taglib-cms 3.5.4
   magnolia-taglib-utility 3.5.4


===========================================================
System Requirements
===========================================================

You need a J2EE application server to use Magnolia.
Depending on the application server you use, you might also
need a Java SDK, version 1.4.2 or 1.5 (A simple JRE (Java
Runtime Environment) will not be sufficient).
You can get a Java SDK from:
    http://java.sun.com/j2se/1.4.2/download.html (SDK)
    http://java.sun.com/j2se/1.5.0/download.jsp (SDK)
A JRE (Java Runtime Environment) will not be sufficient.

The authoring environment will need a browser. The latest
list of tested and compliant browsers can be found at:
    http://www.magnolia.info


===========================================================
Installation & setup: starting Magnolia
===========================================================

Deploy the war file to your application server. (this
procedure is dependent on the application server you use)
If the webapp's name is "magnoliaPublic" (i.e you renamed
the file to magnoliaPublic.war for instance), it will
automatically be deployed as a public instance, otherwise
it will be deployed as an author instance.

===========================================================
Connecting to Magnolia
===========================================================

Once your application server is started, Magnolia should be
accessible through a web browser. If you're accessing
an author instance, you will be redirected to the authoring
environment. On a public instance, if you want to access
the authoring environment, go to (for instance)
    http://localhost:8080/magnoliaPublic/.magnolia

You will have to follow the installation wizard before
using Magnolia.
The default username & password are:

- username : superuser
- password : superuser

===========================================================
Documentation, Licensing & Support
===========================================================

You can find documentation at:
    http://documentation.magnolia.info/

THIS SOFTWARE IS PROVIDED "AS-IS" AND FREE OF CHARGE, WITH
ABSOLUTELY NO WARRANTY OR SUPPORT OF ANY KIND, EXPRESSED OR
IMPLIED, UNDER THE TERMS OF THE INCLUDED LICENSE AGREEMENT.

Free help is available at the community mailing-list at:
    http://www.magnolia.info/en/developer.html
on a volunteer basis from members of the community and the
Magnolia staff. Please submit help requests and bug reports
concerning our free software distributions to the
mailing-list.

For email and phone support, commercial support packages
are available from Magnolia International. See:
    http://www.magnolia.info/en/services.html
for details on our commercial services regarding Magnolia.


Thank you for using Magnolia.

Magnolia International Ltd.
info@magnolia.info


===========================================================

Copyright 2003-2007 Magnolia International Ltd.

Magnolia is a registered trademark of
Magnolia International Ltd.

http://www.magnolia.info
All rights reserved.
