<%@ page import="javax.jcr.Session,
                     javax.jcr.Repository,
                     javax.jcr.SimpleCredentials,
                     javax.naming.InitialContext,
                     javax.rmi.PortableRemoteObject,
                     java.util.Properties,
                     java.io.ByteArrayOutputStream"
%>
<%@page session="false" %>

<%
// This is a hack JSP to export test data from the repository bundled with
// the TCK.  It was used manually to obtain the data files in this dir, it
// is not needed for testing or anything else.
Properties env = new Properties();
env.setProperty ("java.naming.provider.url", "http://www.apache.org/jackrabbit");
env.setProperty ("java.naming.factory.initial", "com.day.crx.jndi.provider.MemoryInitialContextFactory");

InitialContext initial = new InitialContext(env);
Object obj = initial.lookup("jackrabbit.repository");

Repository repository = (Repository)PortableRemoteObject.narrow(obj, Repository.class);

Session session = repository.login(new SimpleCredentials ("superuser", "superuser".toCharArray()));

ByteArrayOutputStream bos = new ByteArrayOutputStream();

session.exportDocumentView("/jcr:system", bos, false, false);

out.write (bos.toString());
%>
