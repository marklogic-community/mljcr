/*
 * Copyright (c) 2008,  Mark Logic Corporation. All Rights Reserved.
 */

package com.marklogic.jcr.servlet;

import org.apache.jackrabbit.core.RepositoryImpl;
import org.apache.jackrabbit.core.config.ClusterConfig;
import org.apache.jackrabbit.core.config.ConfigurationException;
import org.apache.jackrabbit.core.config.DataStoreConfig;
import org.apache.jackrabbit.core.config.FileSystemConfig;
import org.apache.jackrabbit.core.config.RepositoryConfig;
import org.apache.jackrabbit.core.config.RepositoryConfigurationParser;
import org.apache.jackrabbit.core.config.SearchConfig;
import org.apache.jackrabbit.core.config.SecurityConfig;
import org.apache.jackrabbit.core.config.VersioningConfig;
import org.apache.jackrabbit.j2ee.RepositoryStartupServlet;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import javax.jcr.Repository;
import javax.jcr.RepositoryException;

import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: ron
 * Date: Apr 14, 2008
 * Time: 5:05:36 PM
 */
public class MarkLogicRepoStartupServlet extends RepositoryStartupServlet
{

	protected Repository createRepository (InputSource is, File homedir) throws RepositoryException
	{
		RepositoryConfig config = RepoConfig.create (is, homedir.getAbsolutePath());

		return RepositoryImpl.create (config);
	}

	private static class RepoConfig extends RepositoryConfig
	{
		/**
		 * Creates a repository configuration object.
		 *
		 * @param template		 workspace configuration template
		 * @param home		     repository home directory
		 * @param sec		      the security configuration
		 * @param fsc		      file system configuration
		 * @param workspaceDirectory       workspace root directory
		 * @param workspaceConfigDirectory optional workspace configuration directory
		 * @param workspaceMaxIdleTime     maximum workspace idle time in seconds
		 * @param defaultWorkspace	 name of the default workspace
		 * @param vc		       versioning configuration
		 * @param sc		       search configuration for system search manager.
		 * @param cc		       optional cluster configuration
		 * @param parser		   configuration parser
		 */
		public RepoConfig (String home, SecurityConfig sec, FileSystemConfig fsc,
			String workspaceDirectory, String workspaceConfigDirectory,
			String defaultWorkspace, int workspaceMaxIdleTime,
			Element template, VersioningConfig vc, SearchConfig sc,
			ClusterConfig cc, DataStoreConfig dataStoreConfig,
			RepositoryConfigurationParser parser)
		{
			super (home, sec, fsc, workspaceDirectory, workspaceConfigDirectory,
				defaultWorkspace, workspaceMaxIdleTime, template,
				vc, sc, cc, dataStoreConfig, parser);
		}


		public void init () throws ConfigurationException, IllegalStateException
		{
			super.init ();
			
//			if (!workspaces.isEmpty()) {
//			    throw new IllegalStateException(
//				    "Repository configuration has already been initialized.");
//			}
//
//			// Get the physical workspace root directory (create it if not found)
//			File directory = new File(workspaceDirectory);
//			if (!directory.exists()) {
//			    directory.mkdirs();
//			}
//
//			// Get all workspace subdirectories
//			if (workspaceConfigDirectory != null) {
//			    // a configuration directoy had been specified; search for
//			    // workspace configurations in virtual repository file system
//			    // rather than in physical workspace root directory on disk
//			    FileSystem fs = fsc.createFileSystem();
//			    try {
//				if (!fs.exists(workspaceConfigDirectory)) {
//				    fs.createFolder(workspaceConfigDirectory);
//				} else {
//				    String[] dirNames = fs.listFolders(workspaceConfigDirectory);
//				    for (int i = 0; i < dirNames.length; i++) {
//					String configDir = workspaceConfigDirectory
//						+ FileSystem.SEPARATOR + dirNames[i];
//					WorkspaceConfig wc = loadWorkspaceConfig(fs, configDir);
//					if (wc != null) {
//					    addWorkspaceConfig(wc);
//					}
//				    }
//
//				}
//			    } catch (FileSystemException e) {
//				throw new ConfigurationException(
//					"error while loading workspace configurations from path "
//					+ workspaceConfigDirectory, e);
//			    } finally {
//				try {
//				    fs.close();
//				} catch (FileSystemException ignore) {
//				}
//			    }
//			} else {
//			    // search for workspace configurations in physical workspace root
//			    // directory on disk
//			    File[] files = directory.listFiles();
//			    if (files == null) {
//				throw new ConfigurationException(
//					"Invalid workspace root directory: " + workspaceDirectory);
//			    }
//
//			    for (int i = 0; i < files.length; i++) {
//				WorkspaceConfig wc = loadWorkspaceConfig(files[i]);
//				if (wc != null) {
//				    addWorkspaceConfig(wc);
//				}
//			    }
//			}
//			if (!workspaces.containsKey(defaultWorkspace)) {
//			    if (!workspaces.isEmpty()) {
//				log.warn("Potential misconfiguration. No configuration found "
//					+ "for default workspace: " + defaultWorkspace);
//			    }
//			    // create initial default workspace
//			    createWorkspaceConfig(defaultWorkspace);
//			}
		}
	}
}
