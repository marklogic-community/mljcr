/*
 * Copyright (c) 2008,  Mark Logic Corporation. All Rights Reserved.
 */

package com.marklogic.jcr.test;

import com.marklogic.io.LogFormatter;

import org.apache.jackrabbit.core.RepositoryImpl;
import org.apache.jackrabbit.core.WorkspaceImpl;
import org.apache.jackrabbit.core.config.ConfigurationException;
import org.apache.jackrabbit.core.config.RepositoryConfig;
import org.apache.jackrabbit.core.nodetype.NodeTypeManagerImpl;
import org.apache.jackrabbit.test.RepositoryStub;
import org.apache.jackrabbit.test.RepositoryStubException;
import org.xml.sax.InputSource;

import javax.jcr.ImportUUIDBehavior;
import javax.jcr.NamespaceRegistry;
import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NoSuchNodeTypeException;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: ron
 * Date: May 19, 2008
 * Time: 5:17:59 PM
 */
public class MarkLogicRepoStub extends RepositoryStub
{
	public static final String REPO_CONFIG_FILE_PROP = "com.marklogic.jcr.test.repoconfig";
	public static final String REPO_HOME_PROP = "com.marklogic.jcr.test.repohome";
	public static final String REPO_HOME_DEFAULT = "/tmp/jcrtck";

	private static final String SERIALIZED_TESTDATA = "com/marklogic/jcr/test/testdata-docview.xml";
	private static final String SERIALIZED_TESTROOT = "com/marklogic/jcr/test/testroot-docview.xml";
	private static final String NAMESPACES_PROPS = "com/marklogic/jcr/test/test-namespaces.properties";
	private static final String NODETYPE_DEFS = "com/marklogic/jcr/test/test-nodetypes.xml";

	private RepositoryImpl repository = null;

	public MarkLogicRepoStub (Properties env)
	{
		super (env);

		Logger logger = Logger.getLogger ("com.marklogic.jcr");

		Handler handler = new ConsoleHandler();

		handler.setFormatter (new LogFormatter());
		logger.addHandler (handler);
		logger.setUseParentHandlers (false);
	}

	public Repository getRepository() throws RepositoryStubException
	{
		if (repository == null) {
			String repoHome = environment.getProperty (REPO_HOME_PROP, REPO_HOME_DEFAULT);
			String repoConfigFile = environment.getProperty(REPO_CONFIG_FILE_PROP);

			if (repoConfigFile == null) {
			    throw new RepositoryStubException("Property " + REPO_CONFIG_FILE_PROP + " is not defined");
			}

			InputStream in = null;
			RepositoryConfig repositoryConfig = null;

			try {
				in = new FileInputStream (repoConfigFile);
				repositoryConfig = RepositoryConfig.create (new InputSource (in), repoHome);
				repository = RepositoryImpl.create (repositoryConfig);
				Session session = repository.login (getSuperuserCredentials(), null);

				createTestWorkspace (repository, session);
				registerTestNamespaces (session);
				registerTestNodeTypes (session);
				loadTestData (session);
			} catch (FileNotFoundException e) {
				throw new RepositoryStubException ("Cannot open file " + repoConfigFile + ": " + e);
			} catch (ConfigurationException e) {
				throw new RepositoryStubException ("Cannot parse file " + repoConfigFile + ": " + e);
			} catch (RepositoryException e) {
				throw new RepositoryStubException ("Cannot instantiate repo: " + e);
			}
		}

		return repository;
	}

	private void createTestWorkspace (Repository repository, Session session) throws RepositoryException
	{
		try {
			repository.login (getSuperuserCredentials(), "test");
			return;		// already exists
		} catch (NoSuchWorkspaceException e) {
			// fall through
		}

		WorkspaceImpl workspace = (WorkspaceImpl) session.getWorkspace();

		workspace.createWorkspace ("test");
		session.save();
	}

	private void registerTestNodeTypes (Session session) throws RepositoryException, RepositoryStubException
	{
		NodeTypeManagerImpl ntMgr = (NodeTypeManagerImpl) session.getWorkspace().getNodeTypeManager();

		try {
			ntMgr.getNodeType ("test:canSetProperty");
			return;		// already registered
		} catch (NoSuchNodeTypeException e) {
			// fall through
		}

		InputStream in = ClassLoader.getSystemClassLoader().getResourceAsStream (NODETYPE_DEFS);

		try {
			ntMgr.registerNodeTypes (in, "text/xml");
		} catch (IOException e) {
			throw new RepositoryStubException ("Cannot register node types " + NODETYPE_DEFS + ": " + e);
		}

		session.save();
	}

	private void registerTestNamespaces (Session session) throws RepositoryException, RepositoryStubException
	{
		NamespaceRegistry nsReg = session.getWorkspace ().getNamespaceRegistry ();
		InputStream in = ClassLoader.getSystemClassLoader().getResourceAsStream (NAMESPACES_PROPS);
		Properties props = new Properties();

		try {
			props.load (in);
			in.close();
		} catch (IOException e) {
			throw new RepositoryStubException ("Cannot open properties " + NAMESPACES_PROPS + ": " + e);
		}

		for (Iterator it = props.entrySet().iterator (); it.hasNext ();)
		{
			Map.Entry entry = (Map.Entry) it.next();
			String prefix = (String) entry.getKey();
			String uri = (String) entry.getValue();

			try {
				nsReg.getURI (prefix);
				// if no exception, then it's already registered
			} catch (RepositoryException e) {
				nsReg.registerNamespace (prefix, uri);
			}
		}

		session.save();
	}


	private void loadTestData (Session session) throws RepositoryException, RepositoryStubException
	{
		importDocView (session, SERIALIZED_TESTDATA);
		importDocView (session, SERIALIZED_TESTROOT);

		session.save();
	}

	private void importDocView (Session session, String resourceName)
		throws RepositoryException, RepositoryStubException
	{
		InputStream in = ClassLoader.getSystemClassLoader().getResourceAsStream (resourceName);

		try {
			session.importXML("/", in, ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING);
			session.save();
			in.close();
		} catch (IOException e) {
			throw new RepositoryStubException ("Cannot open file " + resourceName + ": " + e);
		}
	}
}
