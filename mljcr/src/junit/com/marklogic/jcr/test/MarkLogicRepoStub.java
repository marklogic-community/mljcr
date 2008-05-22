/*
 * Copyright (c) 2008,  Mark Logic Corporation. All Rights Reserved.
 */

package com.marklogic.jcr.test;

import org.apache.jackrabbit.core.RepositoryImpl;
import org.apache.jackrabbit.core.config.ConfigurationException;
import org.apache.jackrabbit.core.config.RepositoryConfig;
import org.apache.jackrabbit.test.RepositoryStub;
import org.apache.jackrabbit.test.RepositoryStubException;
import org.xml.sax.InputSource;

import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.ImportUUIDBehavior;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: ron
 * Date: May 19, 2008
 * Time: 5:17:59 PM
 */
public class MarkLogicRepoStub extends RepositoryStub
{
	public static final String REPO_CONFIG_FILE_PROP = "com.marklogic.jcr.test.repoconfig";
	public static final String REPO_HOME = "/tmp/JackRabbitRepo";

	private static final String SERIALIZED_TESTDATA = "com/marklogic/jcr/test/testdata-docview.xml";
	private static final String SERIALIZED_TESTROOT = "com/marklogic/jcr/test/testroot-docview.xml";
	private static final String SERIALIZED_JCR_SYSTEM = "com/marklogic/jcr/test/jcr_system-docview.xml";

	private Repository repository = null;

	public MarkLogicRepoStub (Properties env)
	{
		super (env);
	}

	public Repository getRepository () throws RepositoryStubException
	{
		if (repository == null) {
			String repoConfigFile = environment.getProperty(REPO_CONFIG_FILE_PROP);

			if (repoConfigFile == null) {
			    throw new RepositoryStubException("Property " + REPO_CONFIG_FILE_PROP + " is not defined");
			}

			InputStream in = null;
			RepositoryConfig repositoryConfig = null;

			try {
				in = new FileInputStream (repoConfigFile);
				repositoryConfig = RepositoryConfig.create (new InputSource (in), REPO_HOME);
				repository = RepositoryImpl.create (repositoryConfig);
				loadTestData (repository);
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


	private void loadTestData (Repository repository) throws RepositoryException, RepositoryStubException
	{
		Session session = repository.login (getSuperuserCredentials(), null);
		importDocView (session, SERIALIZED_TESTDATA);
		importDocView (session, SERIALIZED_TESTROOT);
//		importDocView (session, SERIALIZED_JCR_SYSTEM);
	}

	private void importDocView (Session session, String resourceName)
		throws RepositoryException, RepositoryStubException
	{
		InputStream in = ClassLoader.getSystemClassLoader().getResourceAsStream (resourceName);

		try {
			session.importXML("/", in, ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING);
			session.save();
		} catch (IOException e) {
			throw new RepositoryStubException ("Cannot open file " + resourceName + ": " + e);
		}
	}
}
