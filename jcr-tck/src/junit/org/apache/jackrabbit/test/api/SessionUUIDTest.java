/*
 * Copyright 2004-2005 The Apache Software Foundation or its licensors,
 *                     as applicable.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jackrabbit.test.api;

import org.apache.jackrabbit.test.AbstractJCRTest;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.ReferentialIntegrityException;
import javax.jcr.Session;

/**
 * <code>SessionUUIDTest</code> contains all tests for the {@link javax.jcr.Session}
 * class that require a UUID (and therefore are optional).
 *
 * @test
 * @sources SessionUUIDTest.java
 * @executeClass org.apache.jackrabbit.test.api.SessionUUIDTest
 * @keywords uuid
 */
public class SessionUUIDTest extends AbstractJCRTest {

    /**
     * Tries to remove a node that is a reference target using {@link Session#save()}.<br>
     * <br>Procedure:
     * <ul>
     * <li>Creates two nodes with same session</li>
     * <li>One has a referencing property pointing to the other node</li>
     * <li>Target node gets removed.</code></li>
     * </ul>
     * This should generate a {@link javax.jcr.ReferentialIntegrityException} upon save.
     * <br><br>Prerequisites:
     * <ul>
     * <li><code>javax.jcr.tck.SessionUUIDTest.nodetype</code> must allow a property of type {@link javax.jcr.PropertyType#REFERENCE}</li>
     * <li><code>javax.jcr.tck.SessionUUIDTest.propertyname1</code> name of the property of type {@link javax.jcr.PropertyType#REFERENCE}</li>
     * <li><code>javax.jcr.tck.SessionUUIDTest.nodetype2</code> must have the mixin type <code>mix:referenceable</code> assigned.</li>
     * </ul>
     */
    public void testSaveReferentialIntegrityException() throws RepositoryException {
        // get default workspace test root node using superuser session
        Node defaultRootNode = (Node) superuser.getItem(testRootNode.getPath());

        // create a node with a property of type PropertyType.REFERENCE
        Node referencingNode = defaultRootNode.addNode(nodeName1, testNodeType);

        // create a node with a jcr:uuid property to serve as target
        Node refTargetNode = defaultRootNode.addNode(nodeName2, getProperty("nodetype2"));

        // set the reference
        referencingNode.setProperty(propertyName1, refTargetNode);

        // save the new nodes
        superuser.save();

        // remove the referenced node
        refTargetNode.remove();

        // try to save
        try {
            superuser.save();
            fail("Saving a deleted node using Session.save() that is a reference target should throw ReferentialIntegrityException");
        } catch (ReferentialIntegrityException e) {
            // ok, works as expected
        }
    }

    /**
     * Moves a referencable node using {@link javax.jcr.Session#move(String, String)} with one session and
     * saves afterward changes made with a second session to the moved node using {@link Session#save()}.<br/>
     * <br/>
     * Procedure:
     * <ul>
     * <li>Creates node 1 and node 2 with session 1</li>
     * <li>Gets reference to node 1 using session 2</li>
     * <li>Session 1 moves node 1 under node 2, saves changes</li>
     * <li>Session 2 modifes node 1, saves</li>
     * </ul>
     * This should work since the modified node is identified by its UUID, not by position in repository.
     * <br><br>Prerequisites:
     * <ul>
     * <li><code>javax.jcr.tck.SessionUUIDTest.nodetype2</code> must have the mixin type <code>mix:referenceable</code> assigned.</li>
     * <li><code>javax.jcr.tck.SessionUUIDTest.testSaveMovedRefNode.propertyname1</code>
     * name of a property that can be modified in <code>nodetype2</code> for testing</li>
     * </ul>
     */
    public void testSaveMovedRefNode() throws RepositoryException {
        // get default workspace test root node using superuser session
        Node defaultRootNode = (Node) superuser.getItem(testRootNode.getPath());

        // create a node
        Node newParentNode = defaultRootNode.addNode(nodeName1, testNodeType);

        // create a referenceable node
        Node refTargetNode = defaultRootNode.addNode(nodeName2, getProperty("nodetype2"));

        // save the new nodes
        superuser.save();

        // get the moving node with session 2
        Session testSession = helper.getReadWriteSession();

        try {
            Node refTargetNodeSession2 = (Node) testSession.getItem(refTargetNode.getPath());

            // move the node with session 1
            superuser.move(refTargetNode.getPath(), newParentNode.getPath() + "/" + nodeName2);

            // make the move persistent with session 1
            superuser.save();

            // modify some prop of the moved node with session 2
            refTargetNodeSession2.setProperty(propertyName1, "test");

            // save it
            testSession.save();

            // ok, works as expected
        } finally {
            testSession.logout();
        }
    }
}