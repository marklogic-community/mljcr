
   There are three jar files produced:

   mljcr-core.jar
   	This contains the core code for the Mark Logic adapter.  This
   	should be placed in the classpath of your JackRabbit-based app.

   mljcr-compat-1.4.jar
   mljcr-compat-1.3.jar
   	These jar files contain code that is specific to either JackRabbit
   	version 1.3 or 1.4.  If your app uses JackRabbit 1.3 (still the
   	most common), then place mljcr-compat-1.3.jar in the classpath.
   	Use mljcr-compat-1.4.jar if you are using JackRabbit 1.4.  We have
   	not yet written compatability code for JackRabbit 1.5, which is
   	still very new as of this writing.


   In addtion to mljcr-core.jar and one of the compatability jars, you
will also need xcc.jar in your classpath.  The xcc.jar jar may be downloaded
from http://developer.marklogic.com/download/

   You should always use the latest available version of xcc.jar for
production applications.

-------
Feb 2009