/*
 * Copyright (c) 2008,  Mark Logic Corporation. All Rights Reserved.
 */

package com.marklogic.jcr.test;

import org.apache.jackrabbit.test.RepositoryStub;
import org.apache.jackrabbit.test.RepositoryStubException;
import org.apache.jackrabbit.core.config.RepositoryConfig;
import org.apache.jackrabbit.core.config.ConfigurationException;
import org.apache.jackrabbit.core.RepositoryImpl;
import org.xml.sax.InputSource;

import javax.jcr.Repository;
import javax.jcr.RepositoryException;

import java.util.Properties;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

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
}
