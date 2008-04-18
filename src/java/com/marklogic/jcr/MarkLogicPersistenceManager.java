/*
 * Copyright (c) 2008,  Mark Logic Corporation. All Rights Reserved.
 */

package com.marklogic.jcr;

import org.apache.jackrabbit.core.NodeId;
import org.apache.jackrabbit.core.PropertyId;
import org.apache.jackrabbit.core.fs.BasedFileSystem;
import org.apache.jackrabbit.core.fs.FileSystem;
import org.apache.jackrabbit.core.fs.FileSystemException;
import org.apache.jackrabbit.core.fs.FileSystemPathUtil;
import org.apache.jackrabbit.core.fs.FileSystemResource;
import org.apache.jackrabbit.core.nodetype.NodeDefId;
import org.apache.jackrabbit.core.nodetype.PropDefId;
import org.apache.jackrabbit.core.persistence.AbstractPersistenceManager;
import org.apache.jackrabbit.core.persistence.PMContext;
import org.apache.jackrabbit.core.persistence.util.BLOBStore;
import org.apache.jackrabbit.core.persistence.util.ResourceBasedBLOBStore;
import org.apache.jackrabbit.core.state.ItemStateException;
import org.apache.jackrabbit.core.state.NoSuchItemStateException;
import org.apache.jackrabbit.core.state.NodeReferences;
import org.apache.jackrabbit.core.state.NodeReferencesId;
import org.apache.jackrabbit.core.state.NodeState;
import org.apache.jackrabbit.core.state.PropertyState;
import org.apache.jackrabbit.core.util.DOMWalker;
import org.apache.jackrabbit.core.value.BLOBFileValue;
import org.apache.jackrabbit.core.value.InternalValue;
import org.apache.jackrabbit.spi.Name;
import org.apache.jackrabbit.spi.NameFactory;
import org.apache.jackrabbit.spi.commons.name.NameFactoryImpl;
import org.apache.jackrabbit.util.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.PropertyType;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * <code>XMLPersistenceManager</code> is a <code>FileSystem</code>-based
 * <code>PersistenceManager</code> that persists <code>ItemState</code>
 * and <code>NodeReferences</code> objects in XML format.
 * @noinspection OverlyComplexClass,ClassWithTooManyMethods
 */
public class MarkLogicPersistenceManager extends AbstractPersistenceManager
{
	private static final Logger log = LoggerFactory.getLogger (MarkLogicPersistenceManager.class);

	/**
	 * hexdigits for toString
	 */
//	private static final char[] HEXDIGITS = "0123456789abcdef".toCharArray ();

	/**
	 * The default encoding used in serialization
	 */
	public static final String DEFAULT_ENCODING = "UTF-8";

	/**
	 * The XML elements and attributes used in serialization
	 */
	private static final String NODE_ELEMENT = "node";
	private static final String UUID_ATTRIBUTE = "uuid";
	private static final String NODETYPE_ATTRIBUTE = "nodeType";
	private static final String PARENTUUID_ATTRIBUTE = "parentUUID";
	private static final String DEFINITIONID_ATTRIBUTE = "definitionId";
	private static final String MODCOUNT_ATTRIBUTE = "modCount";

	private static final String MIXINTYPES_ELEMENT = "mixinTypes";
	private static final String MIXINTYPE_ELEMENT = "mixinType";

	private static final String PROPERTIES_ELEMENT = "properties";
	private static final String PROPERTY_ELEMENT = "property";
	private static final String NAME_ATTRIBUTE = "name";
	private static final String TYPE_ATTRIBUTE = "type";
	private static final String MULTIVALUED_ATTRIBUTE = "multiValued";

	private static final String VALUES_ELEMENT = "values";
	private static final String VALUE_ELEMENT = "value";

	private static final String NODES_ELEMENT = "nodes";

	private static final String NODEREFERENCES_ELEMENT = "references";
	private static final String TARGETID_ATTRIBUTE = "targetId";
	private static final String NODEREFERENCE_ELEMENT = "reference";
	private static final String PROPERTYID_ATTRIBUTE = "propertyId";

	private static final String NODEFILENAME = "node.xml";

	private static final String NODEREFSFILENAME = "references.xml";

	private boolean initialized;

	private MarkLogicFileSystem contextFS;
	private FileSystem itemStateFS;
	private BLOBStore blobStore;

	private final NameFactory factory;

	private static final String workspaceDocUri = "/state.xml";
	private static final String workspaceStateTemplate = "<workspace />";

	// ---------------------------------------------------------

	/**
	 * Creates a new <code>XMLPersistenceManager</code> instance.
	 */
	public MarkLogicPersistenceManager()
	{
		initialized = false;
		factory = NameFactoryImpl.getInstance ();
	}

	//---------------------------------------------------< PersistenceManager >

	/**
	 * {@inheritDoc}
	 */
	public void init (PMContext context) throws Exception
	{
		if (initialized) {
			throw new IllegalStateException ("Already initialized");
		}

		if ( ! (context.getFileSystem() instanceof MarkLogicFileSystem)) {
			throw new IllegalArgumentException ("Must be used with MarkLogicFileSystem");
		}

		contextFS = (MarkLogicFileSystem) context.getFileSystem();
		itemStateFS = new BasedFileSystem(context.getFileSystem(), "/data");
		blobStore = new MarkLogicBlobStore (itemStateFS);

		insureStateDoc (contextFS, workspaceDocUri, workspaceStateTemplate);

		initialized = true;
	}

	private void insureStateDoc (FileSystem fs, String uri, String template)
		throws FileSystemException, IOException
	{
		if (fs.exists (uri)) return;

		Writer writer = new OutputStreamWriter (fs.getOutputStream (uri));

		writer.write (template);
		writer.flush();
		writer.close();
	}

	/**
	 * {@inheritDoc}
	 */
	public synchronized void close() throws Exception
	{
		if (!initialized) {
			throw new IllegalStateException ("not initialized");
		}

		initialized = false;
	}

	/**
	 * {@inheritDoc}
	 */
	public synchronized NodeState load (NodeId id)
		throws ItemStateException
	{

		if (!initialized) {
			throw new IllegalStateException ("not initialized");
		}

		Exception e = null;
		String nodeFilePath = buildNodeFilePath (id);

		try {
			if (!itemStateFS.isFile (nodeFilePath)) {
				throw new NoSuchItemStateException (id.toString ());
			}
			InputStream in = itemStateFS.getInputStream (nodeFilePath);

			try {
				DOMWalker walker = new DOMWalker (in);
				String ntName = walker.getAttribute (NODETYPE_ATTRIBUTE);

				NodeState state = createNew (id);
				state.setNodeTypeName (factory.create (ntName));
				readState (walker, state);
				return state;
			} finally {
				in.close ();
			}
		} catch (IOException ioe) {
			e = ioe;
			// fall through
		} catch (FileSystemException fse) {
			e = fse;
			// fall through
		}
		String msg = "failed to read node state: " + id;
		log.debug (msg);
		throw new ItemStateException (msg, e);
	}

	/**
	 * {@inheritDoc}
	 */
	public synchronized PropertyState load (PropertyId id)
		throws ItemStateException
	{

		if (!initialized) {
			throw new IllegalStateException ("not initialized");
		}

		Exception e = null;
		String propFilePath = buildPropFilePath (id);

		try {
			if (!itemStateFS.isFile (propFilePath)) {
				throw new NoSuchItemStateException (id.toString ());
			}
			InputStream in = itemStateFS.getInputStream (propFilePath);
			try {
				DOMWalker walker = new DOMWalker (in);
				PropertyState state = createNew (id);
				readState (walker, state);
				return state;
			} finally {
				in.close ();
			}
		} catch (IOException ioe) {
			e = ioe;
			// fall through
		} catch (FileSystemException fse) {
			e = fse;
			// fall through
		}
		String msg = "failed to read property state: " + id.toString();
		log.debug (msg);
		throw new ItemStateException (msg, e);
	}

	// ---------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	protected void store (NodeState state) throws ItemStateException
	{
		if (!initialized) {
			throw new IllegalStateException ("not initialized");
		}

		NodeId id = state.getNodeId();
		StringBuffer sb = new StringBuffer();

		sb.append ("<").append (NODE_ELEMENT).append (" ");
		sb.append (UUID_ATTRIBUTE).append ("=\"").append (id.getUUID()).append("\" ");
		sb.append (PARENTUUID_ATTRIBUTE).append ("=\"").append ((state.getParentId () == null ? "" : state.getParentId().getUUID().toString())).append ("\" ");
		sb.append (DEFINITIONID_ATTRIBUTE).append ("=\"").append (state.getDefinitionId().toString()).append ("\" ");
		sb.append (MODCOUNT_ATTRIBUTE).append ("=\"").append (state.getModCount()).append ("\" ");
		sb.append (NODETYPE_ATTRIBUTE).append ("=\"").append (Text.encodeIllegalXMLCharacters (state.getNodeTypeName().toString())).append ("\">\n");

		// mixin types
		sb.append ("<").append (MIXINTYPES_ELEMENT).append (">\n");
		for (Iterator it = state.getMixinTypeNames().iterator(); it.hasNext();)
		{
			sb.append (MIXINTYPE_ELEMENT).append (" ");
			sb.append (NAME_ATTRIBUTE).append ("=\"");
			sb.append (Text.encodeIllegalXMLCharacters (it.next().toString())).append ("\"/>\n");
		}
		sb.append ("</").append (MIXINTYPES_ELEMENT).append (">\n");

		// properties
		sb.append ("<").append (PROPERTIES_ELEMENT).append (">\n");
		for (Iterator it = state.getPropertyNames ().iterator (); it.hasNext ();)
		{
			Name propName = (Name) it.next ();
			sb.append ("<").append (PROPERTY_ELEMENT).append (" ");
			sb.append (NAME_ATTRIBUTE).append ("=\"");
			sb.append (Text.encodeIllegalXMLCharacters (propName.toString())).append ("\">\n");
			sb.append ("</").append (PROPERTY_ELEMENT).append (">\n");
		}
		sb.append ("</").append (PROPERTIES_ELEMENT).append (">\n");

		// child nodes
		sb.append ("<").append (NODES_ELEMENT).append (">\n");
		for (Iterator it = state.getChildNodeEntries ().iterator (); it.hasNext ();)
		{
			NodeState.ChildNodeEntry entry = (NodeState.ChildNodeEntry) it.next();
			sb.append ("<").append (NODE_ELEMENT).append (" ");
			sb.append (NAME_ATTRIBUTE).append ("=\"");
			sb.append (Text.encodeIllegalXMLCharacters (entry.getName ().toString ())).append ("\" ");
			sb.append (UUID_ATTRIBUTE).append ("=\"").append (entry.getId().getUUID().toString()).append ("\">\n");
			sb.append ("</").append (NODE_ELEMENT).append (">\n");
		}
		sb.append ("</").append (NODES_ELEMENT).append (">\n");
		sb.append ("</").append (NODE_ELEMENT).append (">\n");

		//noinspection UseOfSystemOutOrSystemErr
		System.out.println ("Node state:\n========\n" + sb.toString());

		persistItem (buildNodeFilePath (id), sb.toString());

		//noinspection UseOfSystemOutOrSystemErr
		System.out.println ("========");
	}

	/**
	 * {@inheritDoc}
	 */
	protected void store (PropertyState state) throws ItemStateException
	{
		if (!initialized) {
			throw new IllegalStateException ("not initialized");
		}

		int type = state.getType();
		String typeName;

		try {
			typeName = PropertyType.nameFromValue (type);
		} catch (IllegalArgumentException e) {
			// should never be getting here
			throw new ItemStateException ("unexpected property-type ordinal: " + type, e);
		}

		StringBuffer sb = new StringBuffer();

		sb.append ("<").append (PROPERTY_ELEMENT).append (" ");
		sb.append (NAME_ATTRIBUTE).append ("=\"");
		sb.append (Text.encodeIllegalXMLCharacters (state.getName().toString())).append ("\" ");
		sb.append (PARENTUUID_ATTRIBUTE).append ("=\"").append (state.getParentId().getUUID()).append ("\" ");
		sb.append (MULTIVALUED_ATTRIBUTE).append ("=\"").append (Boolean.toString (state.isMultiValued())).append ("\" ");
		sb.append (DEFINITIONID_ATTRIBUTE).append ("=\"").append (state.getDefinitionId().toString()).append ("\" ");
		sb.append (MODCOUNT_ATTRIBUTE).append ("=\"").append (state.getModCount()).append ("\" ");
		sb.append (TYPE_ATTRIBUTE).append ("=\"").append (typeName).append ("\">\n");
		sb.append ("\t<").append (VALUES_ELEMENT).append (">\n");

		InternalValue[] values = state.getValues();

		if (values == null) values = new InternalValue [0];

		for (int i = 0; i < values.length; i++)
		{
			InternalValue val = values[i];

			if (val == null) continue;	// does this ever happen?

			sb.append ("\t\t<").append (VALUE_ELEMENT).append (">");

			if (type == PropertyType.BINARY) {
				try {
					// put binary value in BLOB store
					BLOBFileValue blobVal = val.getBLOBFileValue();
					InputStream in = blobVal.getStream();
					String blobId = blobStore.createId (state.getPropertyId(), i);

					try {
						blobStore.put (blobId, in, blobVal.getLength());
					} finally {
						try {
							in.close();
						} catch (IOException e) {
							// ignore
						}
					}
					// store id of BLOB as property value
					sb.append (blobId);

					// replace value instance with value backed by resource
					// in BLOB store and discard old value instance (e.g. temp file)
					if (blobStore instanceof ResourceBasedBLOBStore) {
						// optimization: if the BLOB store is resource-based
						// retrieve the resource directly rather than having
						// to read the BLOB from an input stream
						FileSystemResource fsRes =
							((ResourceBasedBLOBStore) blobStore).getResource (blobId);
						values[i] = InternalValue.create (fsRes);
					} else {
						in = blobStore.get (blobId);
						try {
							values[i] = InternalValue.create (in);
						} finally {
							try {
								in.close ();
							} catch (IOException e) {
								// ignore
							}
						}
					}
					blobVal.discard();
				} catch (Exception e) {
					throw new ItemStateException ("Cannot store BLOB : " + e, e);
				}
			} else {
				sb.append (Text.encodeIllegalXMLCharacters (val.toString()));
			}

			sb.append ("</").append (VALUE_ELEMENT).append (">\n");
		}

		sb.append ("\t</").append (VALUES_ELEMENT).append (">\n");
		sb.append ("</").append (PROPERTY_ELEMENT).append (">\n");

		//noinspection UseOfSystemOutOrSystemErr
		System.out.println ("Property state:\n========\n" + sb.toString());

		persistItem (buildPropFilePath (state.getPropertyId()), sb.toString());

		//noinspection UseOfSystemOutOrSystemErr
		System.out.println ("========");
	}

	/**
	 * {@inheritDoc}
	 */
	protected void store (NodeReferences refs) throws ItemStateException
	{
		if (!initialized) {
			throw new IllegalStateException ("not initialized");
		}

		StringBuffer sb = new StringBuffer();

		sb.append ("<").append (NODEREFERENCES_ELEMENT).append (" ");
		sb.append (TARGETID_ATTRIBUTE).append ("=\"").append (refs.getId()).append ("\">\n");

		// write references (i.e. the id's of the REFERENCE properties)

		for (Iterator it = refs.getReferences ().iterator (); it.hasNext ();)
		{
			PropertyId propId = (PropertyId) it.next ();
			sb.append ("\t<").append (NODEREFERENCE_ELEMENT).append (" ");
			sb.append (PROPERTYID_ATTRIBUTE).append ("=\"").append (propId).append ("\"/>\n");
		}
		sb.append ("</").append (NODEREFERENCES_ELEMENT).append (">\n");

		persistItem (buildNodeReferencesFilePath (refs.getId()), sb.toString());
	}

	private void persistItem (String path, String xml)
		throws ItemStateException
	{
		FileSystemResource file = new FileSystemResource (itemStateFS, path);

		try {
			file.makeParentDirs();
			OutputStream os = file.getOutputStream();
			Writer writer = null;

			String encoding = DEFAULT_ENCODING;
			try {
				writer = new BufferedWriter (new OutputStreamWriter (os, encoding));
			} catch (UnsupportedEncodingException e) {
				// should never get here!
				OutputStreamWriter osw = new OutputStreamWriter (os);
				encoding = osw.getEncoding();
				writer = new BufferedWriter (osw);
			}

			writer.write (xml);
			writer.close ();
		} catch (Exception e) {
			String msg = "failed to write item state: " + path;
			log.error (msg);
			throw new ItemStateException (msg, e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	protected void destroy (NodeState state) throws ItemStateException
	{
		if (!initialized) {
			throw new IllegalStateException ("not initialized");
		}

		NodeId id = state.getNodeId ();
		String nodeFilePath = buildNodeFilePath (id);
		FileSystemResource nodeFile = new FileSystemResource (itemStateFS, nodeFilePath);
		try {
			if (nodeFile.exists ()) {
				// delete resource and prune empty parent folders
				nodeFile.delete (true);
			}
		} catch (FileSystemException fse) {
			String msg = "failed to delete node state: " + id;
			log.debug (msg);
			throw new ItemStateException (msg, fse);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	protected void destroy (PropertyState state) throws ItemStateException
	{
		if (!initialized) {
			throw new IllegalStateException ("not initialized");
		}

		// delete binary values (stored as files)
		InternalValue[] values = state.getValues ();
		if (values != null) {
			for (int i = 0; i < values.length; i++) {
				InternalValue val = values[i];
				if (val != null) {
					if (val.getType () == PropertyType.BINARY) {
						BLOBFileValue blobVal = val.getBLOBFileValue ();
						// delete blob file and prune empty parent folders
						blobVal.delete (true);
					}
				}
			}
		}
		// delete property file
		String propFilePath = buildPropFilePath (state.getPropertyId ());
		FileSystemResource propFile = new FileSystemResource (itemStateFS, propFilePath);
		try {
			if (propFile.exists ()) {
				// delete resource and prune empty parent folders
				propFile.delete (true);
			}
		} catch (FileSystemException fse) {
			String msg = "failed to delete property state: " + state.getParentId () + "/" + state.getName ();
			log.debug (msg);
			throw new ItemStateException (msg, fse);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public synchronized NodeReferences load (NodeReferencesId id)
		throws ItemStateException
	{

		if (!initialized) {
			throw new IllegalStateException ("not initialized");
		}

		Exception e = null;
		String refsFilePath = buildNodeReferencesFilePath (id);
		try {
			if (!itemStateFS.isFile (refsFilePath)) {
				throw new NoSuchItemStateException (id.toString ());
			}

			InputStream in = itemStateFS.getInputStream (refsFilePath);

			try {
				DOMWalker walker = new DOMWalker (in);
				NodeReferences refs = new NodeReferences (id);
				readState (walker, refs);
				return refs;
			} finally {
				in.close ();
			}
		} catch (IOException ioe) {
			e = ioe;
			// fall through
		} catch (FileSystemException fse) {
			e = fse;
			// fall through
		}
		String msg = "failed to load references: " + id;
		log.debug (msg);
		throw new ItemStateException (msg, e);
	}


	// ----------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	protected void destroy (NodeReferences refs) throws ItemStateException
	{
		if (!initialized) {
			throw new IllegalStateException ("not initialized");
		}

		NodeReferencesId id = refs.getId ();
		String refsFilePath = buildNodeReferencesFilePath (id);
		FileSystemResource refsFile = new FileSystemResource (itemStateFS, refsFilePath);
		try {
			if (refsFile.exists ()) {
				// delete resource and prune empty parent folders
				refsFile.delete (true);
			}
		} catch (FileSystemException fse) {
			String msg = "failed to delete references: " + id;
			log.debug (msg);
			throw new ItemStateException (msg, fse);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public synchronized boolean exists (NodeId id) throws ItemStateException
	{
		if (!initialized) {
			throw new IllegalStateException ("not initialized");
		}

		try {
			String nodeFilePath = buildNodeFilePath (id);
			FileSystemResource nodeFile = new FileSystemResource (itemStateFS, nodeFilePath);
			return nodeFile.exists ();
		} catch (FileSystemException fse) {
			String msg = "failed to check existence of item state: " + id;
			log.debug (msg);
			throw new ItemStateException (msg, fse);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public synchronized boolean exists (PropertyId id) throws ItemStateException
	{
		if (!initialized) {
			throw new IllegalStateException ("not initialized");
		}

		try {
			String propFilePath = buildPropFilePath (id);
			FileSystemResource propFile = new FileSystemResource (itemStateFS, propFilePath);
			return propFile.exists ();
		} catch (FileSystemException fse) {
			String msg = "failed to check existence of item state: " + id;
			log.error (msg, fse);
			throw new ItemStateException (msg, fse);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public synchronized boolean exists (NodeReferencesId id)
		throws ItemStateException
	{

		if (!initialized) {
			throw new IllegalStateException ("not initialized");
		}

		try {
			String refsFilePath = buildNodeReferencesFilePath (id);
			FileSystemResource refsFile = new FileSystemResource (itemStateFS, refsFilePath);
			return refsFile.exists ();
		} catch (FileSystemException fse) {
			String msg = "failed to check existence of references: " + id;
			log.debug (msg);
			throw new ItemStateException (msg, fse);
		}
	}

	// ----------------------------------------------------------

	/**
	 * Builds the path of the node folder for the given node identifier
	 * based on the configured node path template.
	 *
	 * @param id node identifier
	 * @return node folder path
	 */
	private String buildNodeFolderPath (NodeId id)
	{
		return id.getUUID().toString();
	}

	private String buildPropFilePath (PropertyId id)
	{
		return buildNodeFolderPath (id.getParentId()) + "/" +
			FileSystemPathUtil.escapeName (id.getName().toString()) + ".xml";
	}

	private String buildNodeFilePath (NodeId id)
	{
		return buildNodeFolderPath (id) + "/" + NODEFILENAME;
	}

	private String buildNodeReferencesFilePath (NodeReferencesId id)
	{
		return buildNodeFolderPath (id.getTargetId ()) + "/" + NODEREFSFILENAME;
	}

	private void readState (DOMWalker walker, NodeState state)
		throws ItemStateException
	{
		// first do some paranoid sanity checks
		if (!walker.getName ().equals (NODE_ELEMENT)) {
			String msg = "invalid serialization format (unexpected element: "
				+ walker.getName () + ")";
			log.debug (msg);
			throw new ItemStateException (msg);
		}
		// check uuid
		if (!state.getNodeId ().getUUID ().toString ().equals (walker.getAttribute (UUID_ATTRIBUTE))) {
			String msg = "invalid serialized state: uuid mismatch";
			log.debug (msg);
			throw new ItemStateException (msg);
		}
		// check nodetype
		String ntName = walker.getAttribute (NODETYPE_ATTRIBUTE);
		if (!factory.create (ntName).equals (state.getNodeTypeName ())) {
			String msg = "invalid serialized state: nodetype mismatch";
			log.debug (msg);
			throw new ItemStateException (msg);
		}

		// now we're ready to read state

		// primary parent
		String parentUUID = walker.getAttribute (PARENTUUID_ATTRIBUTE);
		if (parentUUID.length () > 0) {
			state.setParentId (NodeId.valueOf (parentUUID));
		}

		// definition id
		String definitionId = walker.getAttribute (DEFINITIONID_ATTRIBUTE);
		state.setDefinitionId (NodeDefId.valueOf (definitionId));

		// modification count
		String modCount = walker.getAttribute (MODCOUNT_ATTRIBUTE);
		state.setModCount (Short.parseShort (modCount));

		// mixin types
		if (walker.enterElement (MIXINTYPES_ELEMENT)) {
			Set mixins = new HashSet ();
			while (walker.iterateElements (MIXINTYPE_ELEMENT)) {
				mixins.add (factory.create (walker.getAttribute (NAME_ATTRIBUTE)));
			}
			if (mixins.size () > 0) {
				state.setMixinTypeNames (mixins);
			}
			walker.leaveElement ();
		}

		// property entries
		if (walker.enterElement (PROPERTIES_ELEMENT)) {
			while (walker.iterateElements (PROPERTY_ELEMENT)) {
				String propName = walker.getAttribute (NAME_ATTRIBUTE);
				// @todo deserialize type and values
				state.addPropertyName (factory.create (propName));
			}
			walker.leaveElement ();
		}

		// child node entries
		if (walker.enterElement (NODES_ELEMENT)) {
			while (walker.iterateElements (NODE_ELEMENT)) {
				String childName = walker.getAttribute (NAME_ATTRIBUTE);
				String childUUID = walker.getAttribute (UUID_ATTRIBUTE);
				state.addChildNodeEntry (factory.create (childName), NodeId.valueOf (childUUID));
			}
			walker.leaveElement ();
		}
	}

	private void readState (DOMWalker walker, PropertyState state)
		throws ItemStateException
	{
		// first do some paranoid sanity checks
		if (!walker.getName ().equals (PROPERTY_ELEMENT)) {
			String msg = "invalid serialization format (unexpected element: "
				+ walker.getName () + ")";
			log.debug (msg);
			throw new ItemStateException (msg);
		}
		// check name
		if (!state.getName ().equals (factory.create (walker.getAttribute (NAME_ATTRIBUTE)))) {
			String msg = "invalid serialized state: name mismatch";
			log.debug (msg);
			throw new ItemStateException (msg);
		}
		// check parentUUID
		NodeId parentId = NodeId.valueOf (walker.getAttribute (PARENTUUID_ATTRIBUTE));
		if (!parentId.equals (state.getParentId ())) {
			String msg = "invalid serialized state: parentUUID mismatch";
			log.debug (msg);
			throw new ItemStateException (msg);
		}

		// now we're ready to read state

		// type
		String typeName = walker.getAttribute (TYPE_ATTRIBUTE);
		int type;
		try {
			type = PropertyType.valueFromName (typeName);
		} catch (IllegalArgumentException iae) {
			// should never be getting here
			throw new ItemStateException ("unexpected property-type: " + typeName, iae);
		}
		state.setType (type);

		// multiValued
		String multiValued = walker.getAttribute (MULTIVALUED_ATTRIBUTE);
		state.setMultiValued (Boolean.getBoolean (multiValued));

		// definition id
		String definitionId = walker.getAttribute (DEFINITIONID_ATTRIBUTE);
		state.setDefinitionId (PropDefId.valueOf (definitionId));

		// modification count
		String modCount = walker.getAttribute (MODCOUNT_ATTRIBUTE);
		state.setModCount (Short.parseShort (modCount));

		// values
		ArrayList values = new ArrayList ();
		if (walker.enterElement (VALUES_ELEMENT)) {
			while (walker.iterateElements (VALUE_ELEMENT)) {
				// read serialized value
				String content = walker.getContent ();
				if (PropertyType.STRING == type) {
					// STRING value can be empty; ignore length
					values.add (InternalValue.valueOf (content, type));
				} else if (content.length () > 0) {
					// non-empty non-STRING value
					if (type == PropertyType.BINARY) {
						try {
							// special handling required for binary value:
							// the value stores the id of the BLOB data
							// in the BLOB store
							if (blobStore instanceof ResourceBasedBLOBStore) {
								// optimization: if the BLOB store is resource-based
								// retrieve the resource directly rather than having
								// to read the BLOB from an input stream
								FileSystemResource fsRes =
									((ResourceBasedBLOBStore) blobStore).getResource (content);
								values.add (InternalValue.create (fsRes));
							} else {
								InputStream in = blobStore.get (content);
								try {
									values.add (InternalValue.create (in));
								} finally {
									try {
										in.close ();
									} catch (IOException e) {
										// ignore
									}
								}
							}
						} catch (Exception e) {
							String msg = "error while reading serialized binary value";
							log.debug (msg);
							throw new ItemStateException (msg, e);
						}
					} else {
						// non-empty non-STRING non-BINARY value
						values.add (InternalValue.valueOf (content, type));
					}
				} else {
					// empty non-STRING value
					log.warn (state.getPropertyId () + ": ignoring empty value of type "
						+ PropertyType.nameFromValue (type));
				}
			}
			walker.leaveElement ();
		}
		state.setValues ((InternalValue[])
			values.toArray (new InternalValue[values.size ()]));
	}

	private void readState (DOMWalker walker, NodeReferences refs)
		throws ItemStateException
	{
		// first do some paranoid sanity checks
		if (!walker.getName ().equals (NODEREFERENCES_ELEMENT)) {
			String msg = "invalid serialization format (unexpected element: " + walker.getName () + ")";
			log.debug (msg);
			throw new ItemStateException (msg);
		}
		// check targetId
		if (!refs.getId ().equals (NodeReferencesId.valueOf (walker.getAttribute (TARGETID_ATTRIBUTE)))) {
			String msg = "invalid serialized state: targetId  mismatch";
			log.debug (msg);
			throw new ItemStateException (msg);
		}

		// now we're ready to read the references data

		// property id's
		refs.clearAllReferences ();
		while (walker.iterateElements (NODEREFERENCE_ELEMENT)) {
			refs.addReference (PropertyId.valueOf (walker.getAttribute (PROPERTYID_ATTRIBUTE)));
		}
	}
}