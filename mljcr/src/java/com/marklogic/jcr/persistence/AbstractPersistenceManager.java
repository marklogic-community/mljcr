/*
 * Copyright (c) 2008,  Mark Logic Corporation. All Rights Reserved.
 */

package com.marklogic.jcr.persistence;

import com.marklogic.jcr.compat.PMAdapter;
import com.marklogic.jcr.fs.MarkLogicFileSystem;
import com.marklogic.jcr.fs.PropertyBlob;

import org.apache.jackrabbit.core.NodeId;
import org.apache.jackrabbit.core.PropertyId;
import org.apache.jackrabbit.core.fs.FileSystem;
import org.apache.jackrabbit.core.fs.FileSystemException;
import org.apache.jackrabbit.core.fs.FileSystemPathUtil;
import org.apache.jackrabbit.core.fs.FileSystemResource;
import org.apache.jackrabbit.core.nodetype.NodeDefId;
import org.apache.jackrabbit.core.nodetype.PropDefId;
import org.apache.jackrabbit.core.persistence.PMContext;
import org.apache.jackrabbit.core.persistence.PersistenceManager;
import org.apache.jackrabbit.core.state.ChangeLog;
import org.apache.jackrabbit.core.state.ItemState;
import org.apache.jackrabbit.core.state.ItemStateException;
import org.apache.jackrabbit.core.state.NoSuchItemStateException;
import org.apache.jackrabbit.core.state.NodeReferences;
import org.apache.jackrabbit.core.state.NodeReferencesId;
import org.apache.jackrabbit.core.state.NodeState;
import org.apache.jackrabbit.core.state.PropertyState;
import org.apache.jackrabbit.core.util.DOMWalker;
import org.apache.jackrabbit.core.value.BLOBFileValue;
import org.apache.jackrabbit.core.value.InternalValue;
import org.apache.jackrabbit.util.Text;

import javax.jcr.PropertyType;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @noinspection OverlyComplexClass,ClassWithTooManyMethods
 */
abstract public class AbstractPersistenceManager implements PersistenceManager
{
	private static final Logger logger = Logger.getLogger (AbstractPersistenceManager.class.getName());

	public static final String WORKSPACE_DOC_NAME = "state.xml";

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

	private static final String CHANGE_LIST_ELEMENT = "change-list";
	private static final String TX_DIR_ELEMENT = "tx-dir";
	private static final String DATA_DIR_ELEMENT = "data-dir";
	private static final String TXID_ELEMENT = "tx-id";

	private static final String MLJCR_VERSION = "1.0";

	private static final String XSI_NAMESPACE = "http://www.w3.org/2001/XMLSchema-instance";
	private static final String XS_NAMESPACE = "http://www.w3.org/2001/XMLSchema";
	private static final String JCR_NAMESPACE = "http://marklogic.com/jcr";
	private static final String workspaceStateTemplate =
		"<workspace xmlns=\"" + JCR_NAMESPACE + "\"\n" +
			" xmlns:xsi=\"" + XSI_NAMESPACE + "\"\n" +
			" version=\"" + MLJCR_VERSION + "\" ";

	private static final String changeListDocName = "change-list.xml";

	private static final String BLOB_TX_DIR = "tx-tmp";
	private static final String BLOB_DATA_DIR = "data";

	private static final Map jcrToSchemaTypeMap;
	static {
		jcrToSchemaTypeMap = new HashMap();
		jcrToSchemaTypeMap.put ("" + PropertyType.STRING, "xs:string");
		jcrToSchemaTypeMap.put ("" + PropertyType.BOOLEAN, "xs:boolean");
		jcrToSchemaTypeMap.put ("" + PropertyType.DATE, "xs:dateTime");
		jcrToSchemaTypeMap.put ("" + PropertyType.DOUBLE, "xs:double");
		jcrToSchemaTypeMap.put ("" + PropertyType.LONG, "xs:integer");
		jcrToSchemaTypeMap.put ("" + PropertyType.NAME, "xs:string");   // contains {namespace}localname
		jcrToSchemaTypeMap.put ("" + PropertyType.PATH, "xs:string");   // contains a JCR path
		jcrToSchemaTypeMap.put ("" + PropertyType.REFERENCE, "xs:string");  // contains a UUID
		jcrToSchemaTypeMap.put ("" + PropertyType.BINARY, "xs:string");	// value element contains a URI
	}

	private volatile boolean initialized = false;
	private final Random random = new Random (System.currentTimeMillis());
	private final PMAdapter pmAdapter;
	private MarkLogicFileSystem contextFS;
	private String collections = null;

	// ---------------------------------------------------------

	/**
	 * Creates a new <code>XMLPersistenceManager</code> instance.
	 * @param pmAdapter A PMAdapter instance for JackRabbit interface
	 */
	public AbstractPersistenceManager (PMAdapter pmAdapter)
	{
		this.pmAdapter = pmAdapter;
	}

	// ---------------------------------------------------------

	public String getCollections()
	{
		return collections;
	}

	/**
	 * A comma separated list of collection names for documents
	 * in this Workspace.
	 * @param collections A string containing a comma-separated list of names.
	 */
	public void setCollections (String collections)
	{
		this.collections = collections;
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

		insureStateDoc (contextFS, WORKSPACE_DOC_NAME, workspaceStateTemplate, collections);

		contextFS.getUriRoot();

		initialized = true;
	}

	/**
	 * {@inheritDoc}
	 */
	public NodeState createNew (NodeId id) {
		return pmAdapter.newNodeState (id);
	}

	/**
	 * {@inheritDoc}
	 */
	public PropertyState createNew (PropertyId id) {
	    return new PropertyState(id, PropertyState.STATUS_NEW, false);
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
	public synchronized boolean exists (NodeId id) throws ItemStateException
	{
		if (!initialized) {
			throw new IllegalStateException ("not initialized");
		}

		try {
			return contextFS.itemExists (WORKSPACE_DOC_NAME, id);
		} catch (FileSystemException e) {
			String msg = "failed to check existence of item state: " + id + ", msg=" + e;
			logger.log (Level.FINE, msg, e);
			throw new ItemStateException (msg, e);
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
			return contextFS.itemExists (WORKSPACE_DOC_NAME, id);
		} catch (FileSystemException e) {
			String msg = "failed to check existence of item state: " + id;
			logger.log (Level.SEVERE, msg, e);
			throw new ItemStateException (msg, e);
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
			return contextFS.itemExists (WORKSPACE_DOC_NAME, id);
		} catch (FileSystemException e) {
			String msg = "failed to check existence of item state: " + id + ", msg=" + e;
			logger.log (Level.FINE, msg, e);
			throw new ItemStateException (msg, e);
		}
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

		try {
			InputStream in = contextFS.nodeStateAsStream (WORKSPACE_DOC_NAME, id);

			if (in == null) {
				throw new NoSuchItemStateException (id.toString());
			}

			try {
				DOMWalker walker = new DOMWalker (in);
				String ntName = walker.getAttribute (NODETYPE_ATTRIBUTE);

				NodeState state = createNew (id);
				pmAdapter.setNodeTypeName (state, ntName);
				readState (walker, state);
				return state;
			} finally {
				in.close();
			}
		} catch (IOException ioe) {
			e = ioe;
			// fall through
		} catch (FileSystemException fse) {
			e = fse;
			// fall through
		}
		String msg = "failed to read node state: " + id;
		logger.fine (msg);
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

		try {
			InputStream in = contextFS.propertyStateAsStream (WORKSPACE_DOC_NAME, id);

			if (in == null) {
				throw new NoSuchItemStateException (id.toString());
			}

			try {
				DOMWalker walker = new DOMWalker (in);
				PropertyState state = createNew (id);
				readState (walker, state);
				return state;
			} finally {
				in.close();
			}
		} catch (IOException ioe) {
			e = ioe;
			// fall through
		} catch (FileSystemException fse) {
			e = fse;
			// fall through
		}
		String msg = "failed to read property state: " + id.toString();
		logger.fine (msg);
		throw new ItemStateException (msg, e);
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

		try {
			InputStream in = contextFS.referencesStateAsStream (WORKSPACE_DOC_NAME, id.getTargetId());

			if (in == null) {
				throw new NoSuchItemStateException (id.toString());
			}

			try {
				DOMWalker walker = new DOMWalker (in);

				NodeReferences refs = new NodeReferences (id);
				readState (walker, refs);
				return refs;
			} finally {
				in.close();
			}
		} catch (IOException ioe) {
			e = ioe;
			// fall through
		} catch (FileSystemException fse) {
			e = fse;
			// fall through
		}
		String msg = "failed to read node state: " + id;
		logger.fine (msg);
		throw new ItemStateException (msg, e);
	}

	// =============================================================

	private void txDirRoot (String txId, StringBuffer sb)
	{
		sb.append (FileSystem.SEPARATOR_CHAR).append (BLOB_TX_DIR);
		sb.append (FileSystem.SEPARATOR_CHAR).append (txId);
		sb.append (FileSystem.SEPARATOR_CHAR);
	}

	private String txDirRootString (String txId)
	{
		StringBuffer sb = new StringBuffer();

		txDirRoot (txId, sb);

		return sb.toString();
	}

	private String createBlobPath (PropertyId id, String txId)
	{
		StringBuffer sb = new StringBuffer();

		txDirRoot (txId, sb);
		sb.append (id.getParentId().getUUID().toString()).append (FileSystem.SEPARATOR);
		sb.append (FileSystemPathUtil.escapeName(pmAdapter.getPropertyIdNameAsString (id))).append (".blob");

		return sb.toString();
	}

	private String generateTxId()
	{
		Calendar cal = new GregorianCalendar();
		cal.setTime (new Date());
		DateFormat df = new SimpleDateFormat ("yyyyMMdd-HHmmss-");

		return "tx-" + df.format (new Date()) + random.nextInt (1000000);
	}

	// ---------------------------------------------------------

	/** @noinspection UseOfSystemOutOrSystemErr*/
	public void store (ChangeLog changeLog) throws ItemStateException
	{
		// TODO: Gather blobs in a list
		// assign unique names to each
		// assign unique name to change list XML
		// insert all into temp dir
		// invoke module to process them and delete

		String txId = generateTxId();
		List contentList = new ArrayList (16);

		StringBuffer sb = new StringBuffer();

		sb.append ("<").append (CHANGE_LIST_ELEMENT);
		sb.append (" xmlns=\"").append (JCR_NAMESPACE).append ("\"\n\t");
		sb.append (" xmlns:xsi=\"").append (XSI_NAMESPACE).append ("\"\n\t");
		sb.append (" xmlns:xs=\"").append (XS_NAMESPACE).append ("\"\n\t");
		sb.append (">\n");

		sb.append ("\t<").append (TXID_ELEMENT).append (">");
		sb.append (txId);
		sb.append ("</").append (TXID_ELEMENT).append (">\n");

		sb.append ("\t<").append (TX_DIR_ELEMENT).append (">");
		sb.append (BLOB_TX_DIR);
		sb.append ("</").append (TX_DIR_ELEMENT).append (">\n");

		sb.append ("\t<").append (DATA_DIR_ELEMENT).append (">");
		sb.append (BLOB_DATA_DIR);
		sb.append ("</").append (DATA_DIR_ELEMENT).append (">\n");

		sb.append ("\t<").append ("deleted-states").append (">\n");

		for (Iterator it = changeLog.deletedStates(); it.hasNext();) {
			ItemState state = (ItemState) it.next();

			if (state.isNode()) {
				sb.append ("\t\t<node uuid=\"");
				sb.append (((NodeState) state).getNodeId().getUUID());
				sb.append ("\"/>\n");
			} else {
				sb.append ("\t\t<property parentUUID=\"");
				sb.append (state.getParentId().getUUID().toString());
				sb.append ("\" name=\"");
				sb.append (Text.encodeIllegalXMLCharacters (pmAdapter.getPropertyStateNameAsString ((PropertyState) state)));
				sb.append ("\"/>\n");
			}
		}

		sb.append ("\t</").append ("deleted-states").append (">\n");

		sb.append ("\t<").append ("added-states").append (">\n");

		for (Iterator it = changeLog.addedStates(); it.hasNext();)
		{
			ItemState state = (ItemState) it.next();

			if (state.isNode ()) {
				formatElement ((NodeState) state, sb);
			} else {
				// FIXME: encode blobs inline?
				formatElement ((PropertyState) state, txId, contentList, sb);
			}
		}

		sb.append ("\t</").append ("added-states").append (">\n");

		sb.append ("\t<").append ("modified-states").append (">\n");

		for (Iterator it = changeLog.modifiedStates(); it.hasNext();)
		{
			ItemState state = (ItemState) it.next();

			if (state.isNode ()) {
				formatElement ((NodeState) state, sb);
			} else {
				// FIXME: encode blobs inline?
				formatElement ((PropertyState) state, txId, contentList, sb);
			}
		}

		sb.append ("\t</").append ("modified-states").append (">\n");

		sb.append ("\t<").append ("modified-refs").append (">\n");

		for (Iterator it = changeLog.modifiedRefs(); it.hasNext ();)
		{
			NodeReferences refs = (NodeReferences) it.next ();

			formatElement (refs, sb);
		}

		sb.append ("\t</").append ("modified-refs").append (">\n");

		sb.append ("</").append (CHANGE_LIST_ELEMENT).append (">\n");


//System.out.println ("========================================");
//System.out.println (sb.toString());
//System.out.println ("========================================");

		String deltas = sb.toString();
		String deltasDocPath = txDirRootString (txId) + changeListDocName;

		try {
			contextFS.applyStateUpdate (WORKSPACE_DOC_NAME, deltasDocPath, deltas, contentList);
		} catch (FileSystemException e) {
			throw new ItemStateException ("Updating state: " + e, e);
		}
	}

	// ---------------------------------------------------------

	private void formatElement (NodeState state, StringBuffer sb)
	{
		sb.append ("<").append (NODE_ELEMENT).append (" ");
		sb.append (UUID_ATTRIBUTE).append ("=\"").append (state.getNodeId().getUUID()).append("\" ");
		sb.append (PARENTUUID_ATTRIBUTE).append ("=\"").append ((state.getParentId () == null ? "" : state.getParentId().getUUID().toString())).append ("\" ");
		sb.append (DEFINITIONID_ATTRIBUTE).append ("=\"").append (state.getDefinitionId().toString()).append ("\" ");
		sb.append (MODCOUNT_ATTRIBUTE).append ("=\"").append (state.getModCount()).append ("\" ");
		sb.append (NODETYPE_ATTRIBUTE).append ("=\"").append (Text.encodeIllegalXMLCharacters (pmAdapter.getTypeNameAsString (state))).append ("\">\n");

		// mixin types
		sb.append ("<").append (MIXINTYPES_ELEMENT).append (">\n");
		for (Iterator it = state.getMixinTypeNames().iterator(); it.hasNext();)
		{
			sb.append ("<").append (MIXINTYPE_ELEMENT).append (" ");
			sb.append (NAME_ATTRIBUTE).append ("=\"");
			sb.append (Text.encodeIllegalXMLCharacters (it.next().toString())).append ("\"/>\n");
		}
		sb.append ("</").append (MIXINTYPES_ELEMENT).append (">\n");

		// properties
		sb.append ("<").append (PROPERTIES_ELEMENT).append (">\n");
		for (Iterator it = state.getPropertyNames ().iterator (); it.hasNext ();)
		{
//			Name propName = (Name) it.next ();
			String propName = it.next().toString();
			sb.append ("<").append (PROPERTY_ELEMENT).append (" ");
			sb.append (NAME_ATTRIBUTE).append ("=\"");
			sb.append (Text.encodeIllegalXMLCharacters (propName)).append ("\">\n");
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
			sb.append (Text.encodeIllegalXMLCharacters (pmAdapter.getChildNodeEntryAsString (entry))).append ("\" ");
			sb.append (UUID_ATTRIBUTE).append ("=\"").append (entry.getId().getUUID().toString()).append ("\">\n");
			sb.append ("</").append (NODE_ELEMENT).append (">\n");
		}
		sb.append ("</").append (NODES_ELEMENT).append (">\n");
		sb.append ("</").append (NODE_ELEMENT).append (">\n");
	}

	private void formatElement (PropertyState state, String txId, List contentList, StringBuffer sb)
		throws ItemStateException
	{
		int type = state.getType();
		String typeName;

		try {
			typeName = PropertyType.nameFromValue (type);
		} catch (IllegalArgumentException e) {
			// should never be getting here
			throw new ItemStateException ("unexpected property-type ordinal: " + type, e);
		}

		sb.append ("<").append (PROPERTY_ELEMENT).append (" ");
		sb.append (NAME_ATTRIBUTE).append ("=\"");
		sb.append (Text.encodeIllegalXMLCharacters (pmAdapter.getPropertyStateNameAsString (state))).append ("\" ");
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

			sb.append ("\t\t<").append (VALUE_ELEMENT);

			String schemaType = (String) jcrToSchemaTypeMap.get ("" + type);

			if (schemaType != null) {
				sb.append (" xsi:type=\"").append (schemaType).append ("\"");
			}

			sb.append (">");

			if (type == PropertyType.BINARY) {
				try {
					BLOBFileValue blobVal = val.getBLOBFileValue();
					long blobLen = blobVal.getLength();
					String blobId = (blobLen == 0)
						? blobId = MarkLogicFileSystem.MAGIC_EMPTY_BLOB_ID
						: createBlobPath (state.getPropertyId(), txId);

					if (blobLen != 0) {
						contentList.add (new PropertyBlob (state, i, blobVal, blobId));
					}

					sb.append (blobId);





//					// put binary value in BLOB store
//					BLOBFileValue blobVal = val.getBLOBFileValue();
//					long blobLen = blobVal.getLength();
//
//					if (blobLen == 0) {
//						blobId = MarkLogicBlobStore.MAGIC_EMPTY_BLOB_ID;
//					} else {
//						InputStream in = blobVal.getStream();
//						try {
//							blobStore.put (blobId, in, blobLen);
//						} finally {
//							try {
//								in.close();
//							} catch (IOException e) {
//								// ignore
//							}
//						}
//					}
//					// store id of BLOB as property value
//					sb.append (blobId);
//
//					// replace value instance with value backed by resource
//					// in BLOB store and discard old value instance (e.g. temp file)
//					if (blobStore instanceof ResourceBasedBLOBStore) {
//						// optimization: if the BLOB store is resource-based
//						// retrieve the resource directly rather than having
//						// to read the BLOB from an input stream
//						FileSystemResource fsRes = blobStore.getResource (blobId);
//						values[i] = InternalValue.create (fsRes);
//					} else {
//						InputStream in = blobStore.get (blobId);
//						try {
//							values[i] = InternalValue.create (in);
//						} finally {
//							try {
//								in.close ();
//							} catch (IOException e) {
//								// ignore
//							}
//						}
//					}
//					blobVal.discard();
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
	}

	private void formatElement (NodeReferences refs, StringBuffer sb)
	{
		sb.append ("<").append (NODEREFERENCES_ELEMENT).append (" ");
		sb.append (TARGETID_ATTRIBUTE).append ("=\"").append (refs.getId()).append ("\">\n");

		// write references (i.e. the id's of the REFERENCE properties)

		for (Iterator it = refs.getReferences().iterator(); it.hasNext();)
		{
			PropertyId propId = (PropertyId) it.next ();
			sb.append ("\t<").append (NODEREFERENCE_ELEMENT).append (" ");
			sb.append (PARENTUUID_ATTRIBUTE).append ("=\"").append (propId.getParentId().getUUID().toString()).append ("\" ");
			sb.append (NAME_ATTRIBUTE).append ("=\"").append (pmAdapter.getPropertyIdNameAsString (propId)).append ("\"/>\n");

//			sb.append (PROPERTYID_ATTRIBUTE).append ("=\"").append (propId).append ("\"/>\n");
		}

		sb.append ("</").append (NODEREFERENCES_ELEMENT).append (">\n");
	}


	private void readState (DOMWalker walker, NodeState state)
		throws ItemStateException
	{
		// first do some paranoid sanity checks
		if (!walker.getName ().equals (NODE_ELEMENT)) {
			String msg = "invalid serialization format (unexpected element: "
				+ walker.getName () + ")";
			logger.fine (msg);
			throw new ItemStateException (msg);
		}
		// check uuid
		if (!state.getNodeId().getUUID().toString().equals (walker.getAttribute (UUID_ATTRIBUTE))) {
			String msg = "invalid serialized state: uuid mismatch";
			logger.fine (msg);
			throw new ItemStateException (msg);
		}
		// check nodetype
		String ntName = walker.getAttribute (NODETYPE_ATTRIBUTE);

		if ( ! pmAdapter.sameNodeTypeName (state, ntName)) {
			String msg = "invalid serialized state: nodetype mismatch";
			logger.fine (msg);
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
			Set mixins = new HashSet();
			while (walker.iterateElements (MIXINTYPE_ELEMENT)) {
				pmAdapter.addName (mixins, walker.getAttribute (NAME_ATTRIBUTE));
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
				pmAdapter.addPropertyName (state, propName);
			}
			walker.leaveElement ();
		}

		// child node entries
		if (walker.enterElement (NODES_ELEMENT)) {
			while (walker.iterateElements (NODE_ELEMENT)) {
				String childName = walker.getAttribute (NAME_ATTRIBUTE);
				String childUUID = walker.getAttribute (UUID_ATTRIBUTE);
				pmAdapter.addChildNodeEntry (state, childName, NodeId.valueOf (childUUID));
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
			logger.fine (msg);
			throw new ItemStateException (msg);
		}
		// check name
		if ( ! pmAdapter.samePropertyName (state, walker.getAttribute (NAME_ATTRIBUTE))) {
			String msg = "invalid serialized state: name mismatch";
			logger.fine (msg);
			throw new ItemStateException (msg);
		}
		// check parentUUID
		NodeId parentId = NodeId.valueOf (walker.getAttribute (PARENTUUID_ATTRIBUTE));
		if (!parentId.equals (state.getParentId ())) {
			String msg = "invalid serialized state: parentUUID mismatch";
			logger.fine (msg);
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
							FileSystemResource fsRes =
								new FileSystemResource (contextFS, content);
							values.add (InternalValue.create (fsRes));
						} catch (Exception e) {
							String msg = "error while reading serialized binary value";
							logger.fine (msg);
							throw new ItemStateException (msg, e);
						}
					} else {
						// non-empty non-STRING non-BINARY value
						values.add (InternalValue.valueOf (content, type));
					}
				} else {
					// empty non-STRING value
					logger.info (state.getPropertyId() + ": ignoring empty value of type "
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
			logger.fine (msg);
			throw new ItemStateException (msg);
		}
		// check targetId
		if (!refs.getId ().equals (NodeReferencesId.valueOf (walker.getAttribute (TARGETID_ATTRIBUTE)))) {
			String msg = "invalid serialized state: targetId  mismatch";
			logger.fine (msg);
			throw new ItemStateException (msg);
		}

		// now we're ready to read the references data

		// property id's
		refs.clearAllReferences ();
		while (walker.iterateElements (NODEREFERENCE_ELEMENT)) {
			refs.addReference (PropertyId.valueOf (walker.getAttribute (PROPERTYID_ATTRIBUTE)));
		}
	}

	private void insureStateDoc (FileSystem fs, String stateDocName,
		String template, String collectionName)
		throws FileSystemException, IOException
	{
		String uri = "/" + stateDocName;
		if (fs.exists (uri)) return;

		Writer writer = new OutputStreamWriter (fs.getOutputStream (uri));

		writer.write (template);

		if (collectionName != null) {
			writer.write (" collections=\"");
			writer.write (collectionName);
			writer.write ("\"");
		}

		writer.write ("/>");
		writer.flush();
		writer.close();
	}

	// --------------------------------------------------------------
}
