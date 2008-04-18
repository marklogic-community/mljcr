<%@ page import="org.apache.jackrabbit.j2ee.JCRWebdavServerServlet,
                 org.apache.jackrabbit.j2ee.RepositoryAccessServlet,
                 org.apache.jackrabbit.j2ee.SimpleWebdavServlet,
                 java.net.URI,
                 javax.jcr.Repository"
%><%--
  Licensed to the Apache Software Foundation (ASF) under one or more
  contributor license agreements.  See the NOTICE file distributed with
  this work for additional information regarding copyright ownership.
  The ASF licenses this file to You under the Apache License, Version 2.0
  (the "License"); you may not use this file except in compliance with
  the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
--%><%
request.setAttribute("title", "JCR WebDAV Server");

URI uri = new URI(request.getRequestURL().toString());
String href =
    uri.getScheme() + "://" + uri.getHost() + ":" + uri.getPort()
    + request.getContextPath()
    + JCRWebdavServerServlet.getPathPrefix(pageContext.getServletContext());
String shref = href + "/default/jcr:root";
%><jsp:include page="header.jsp"/>
<p>
  The JCR WebDAV server provides an item-based WebDAV view to the
  JCR repository, mapping the functionality provided by JSR 170 to the
  WebDAV protocol in order to allow remote content repository access
  via WebDAV.
</p>
<p>
  See the draft document
  <a href="http://www.day.com/jsr170/server/JCR_Webdav_Protocol.zip">JCR_Webdav_Protocol.zip</a>
  for more details regarding this remoting protocol.
</p>

<h3>Access the content repository</h3>
<p>
  Use the following URLs to access the content repository in your WebDAV client:
</p>
<dl>
<dt><a href="<%= href %>"><%= href %></a></dt>
<dd>to access all workspaces of your JCR repository</dd>
<dt><a href="<%= shref %>"><%= shref %></a></dt>
<dd>to access a single workspace (example with workspace named 'default')</dd>
</dl>

<h3>Supported WebDAV functionality</h3>
<p>
  This implementation focuses on replicating all JCR features for remote
  access instead of providing standard WebDAV functionality or compatibility
  with existing WebDAV clients.
</p>
<p>
  The following RFCs are used to implement the remoting functionality:
</p>
<ul>
  <li><a href="http://www.ietf.org/rfc/rfc2518.txt">RFC 2518</a> (WebDAV 1,2)</li>
  <li><a href="http://www.ietf.org/rfc/rfc3253.txt">RFC 3253</a> (DeltaV)</li>
  <li><a href="http://www.ietf.org/rfc/rfc3648.txt">RFC 3648</a> (Ordering)</li>
  <li><a href="http://greenbytes.de/tech/webdav/draft-reschke-webdav-search-latest.html">Internet Draft WebDAV Search</a></li>
</ul>
<p>
  For the client counterpart of this WebDAV servlet please take a look at the
  <a href="https://svn.apache.org/repos/asf/jackrabbit/sandbox/spi/spi2dav">spi2dav</a>
  project in the Apache Jackrabbit sandbox.
</p>

<h3>Configuration</h3>
<ul>
  <li>Context Path: <%= request.getContextPath() %></li>
  <li>Resource Path Prefix: <%= JCRWebdavServerServlet.getPathPrefix(pageContext.getServletContext()) %></li>
  <li>Workspace Name: <i>optional</i> (available workspaces are mapped as resources)</li>
  <li>Additional servlet configuration: see <i>/WEB-INF/web.xml</i></li>
</ul>
<jsp:include page="footer.jsp"/>
