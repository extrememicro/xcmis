/**
 * Copyright (C) 2010 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.xcmis.sp.jcr.exo;

import org.exoplatform.services.jcr.access.AccessControlList;
import org.exoplatform.services.jcr.access.PermissionType;
import org.exoplatform.services.jcr.core.ExtendedNode;
import org.exoplatform.services.jcr.core.ExtendedSession;
import org.xcmis.spi.AccessControlEntry;
import org.xcmis.spi.CMIS;
import org.xcmis.spi.ConstraintException;
import org.xcmis.spi.ItemsIterator;
import org.xcmis.spi.ObjectNotFoundException;
import org.xcmis.spi.PropertyDefinition;
import org.xcmis.spi.Storage;
import org.xcmis.spi.TypeDefinition;
import org.xcmis.spi.UnfileObject;
import org.xcmis.spi.VersioningState;
import org.xcmis.spi.data.BaseContentStream;
import org.xcmis.spi.data.ContentStream;
import org.xcmis.spi.data.Document;
import org.xcmis.spi.data.Folder;
import org.xcmis.spi.data.ObjectData;
import org.xcmis.spi.data.Policy;
import org.xcmis.spi.data.Relationship;
import org.xcmis.spi.impl.AccessControlEntryImpl;
import org.xcmis.spi.object.Property;
import org.xcmis.spi.object.impl.StringProperty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jcr.ItemVisitor;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class StorageTest extends BaseTest
{

   protected Storage storage;

   protected Folder rootFolder;

   public void setUp() throws Exception
   {
      super.setUp();
      storage = storageProvider.getConnection(cmisRepositoryId, null).getStorage();
      rootFolder = (Folder)storage.getObject(JcrCMIS.ROOT_FOLDER_ID);
   }

   public void testApplyACL() throws Exception
   {
      Document document = createDocument(rootFolder, "applyACLTestDocument", "cmis:document", null, null);
      AccessControlEntry ace =
         new AccessControlEntryImpl("root", new HashSet<String>(Arrays.asList("cmis:read", "cmis:write")));
      document.setACL(Arrays.asList(ace));
      storage.saveObject(document);

      Node documentNode = (Node)session.getItem("/applyACLTestDocument");
      AccessControlList acl = ((ExtendedNode)documentNode).getACL();

      List<String> permissions = acl.getPermissions("root");
      assertTrue(permissions.contains(PermissionType.READ));
      assertTrue(permissions.contains(PermissionType.REMOVE));
      assertTrue(permissions.contains(PermissionType.SET_PROPERTY));
      assertTrue(permissions.contains(PermissionType.ADD_NODE));

      System.out.println(document.getACL(false));
   }

   public void testApplyPolicy() throws Exception
   {
      Document document = createDocument(rootFolder, "applyPolicyTestDocument", "cmis:document", null, null);
      Policy policy = createPolicy(rootFolder, "applyPolicyTestPolicy01", "test apply policy", "cmis:policy");
      document.applyPolicy(policy);
      storage.saveObject(document);

      Node documentNode = (Node)session.getItem("/applyPolicyTestDocument");
      assertTrue(documentNode.hasProperty(policy.getObjectId()));

      Collection<Policy> policies = document.getPolicies();
      assertEquals(1, policies.size());
      assertEquals(policy.getObjectId(), policies.iterator().next().getObjectId());
   }

   public void testCheckOut() throws Exception
   {
      //      Document document = createDocument(rootFolder, "checkoutTest", "cmis:document", null, null);
      //      document.checkout();
      //      for (NodeIterator i = root.getNodes(); i.hasNext();)
      //         System.out.println(">>>> "+i.nextNode().getName());
   }

   public void testChildren() throws Exception
   {
      Folder folder = createFolder(rootFolder, "folderChildrenTest", "cmis:folder");
      Set<String> source = new HashSet<String>();
      String name = "testChildren";
      for (int i = 1; i <= 20; i++)
      {
         Document document = createDocument(folder, name + i, "cmis:document", null, null);
         storage.saveObject(document);
         source.add(document.getObjectId());
      }
      // Check children viewing with paging. It should be close to real usage.
      int maxItems = 5;
      for (int i = 0, skipCount = 0; i < 4; i++, skipCount += maxItems)
      {
         ItemsIterator<ObjectData> children = folder.getChildren(null);
         children.skip(skipCount);
         for (int count = 0; children.hasNext() && count < maxItems; count++)
         {
            ObjectData next = children.next();
            //            System.out.println(next.getName());
            source.remove(next.getObjectId());
         }
      }
      if (source.size() > 0)
      {
         StringBuilder sb = new StringBuilder();
         for (String s : source)
         {
            if (sb.length() > 0)
            {
               sb.append(',');
            }
            sb.append(s);
         }
         fail("Object(s) " + sb.toString() + " were not found in children list.");
      }
   }

   public void testCreateDocument() throws Exception
   {
      PropertyDefinition<?> def = PropertyDefinitions.getPropertyDefinition("cmis:document", CMIS.NAME);
      Map<String, Property<?>> properties = new HashMap<String, Property<?>>();
      properties.put(CMIS.NAME, new StringProperty(def.getId(), def.getQueryName(), def.getLocalName(), def
         .getDisplayName(), "createDocumentTest"));

      ContentStream cs =
         new BaseContentStream("to be or not to be".getBytes(), /*"createDocumentTest"*/null, "text/plain");
      Document document = storage.createDocument(rootFolder, "cmis:document", VersioningState.MAJOR);
      document.setProperties(properties);
      //      document.setName("createDocumentTest");
      document.setContentStream(cs);
      AccessControlEntry ace =
         new AccessControlEntryImpl("root", new HashSet<String>(Arrays.asList("cmis:read", "cmis:write")));
      document.setACL(Arrays.asList(ace));
      storage.saveObject(document);

      assertTrue(session.itemExists("/createDocumentTest"));
      Node documentNode = (Node)session.getItem("/createDocumentTest");

      // check content.
      assertEquals("nt:file", documentNode.getPrimaryNodeType().getName());
      assertEquals("to be or not to be", documentNode.getProperty("jcr:content/jcr:data").getString());
      assertEquals("text/plain", documentNode.getProperty("jcr:content/jcr:mimeType").getString());

      // check permissions
      List<String> permissions = ((ExtendedNode)documentNode).getACL().getPermissions("root");
      assertTrue(permissions.size() > 0); // ACL applied to back-end node.
      System.out.println("root: " + permissions);

      // CMIS properties
      assertEquals(true, document.isLatestVersion());
      assertEquals(true, document.isMajorVersion());
      assertEquals(true, document.isLatestMajorVersion());
      assertEquals("root", document.getCreatedBy());
      assertEquals("root", document.getLastModifiedBy());
      assertNotNull(document.getCreationDate());
      assertNotNull(document.getLastModificationDate());
      assertEquals(documentNode.getVersionHistory().getUUID(), document.getVersionSeriesId());
      assertNull(document.getVersionSeriesCheckedOutBy());
      assertNull(document.getVersionSeriesCheckedOutId());
      assertFalse(document.isVersionSeriesCheckedOut());
      assertEquals("latest", document.getVersionLabel());
      assertEquals("text/plain", document.getContentStreamMimeType());
      assertEquals("createDocumentTest", document.getContentStream().getFileName());
   }

   public void testCreateDocumentFromSource() throws Exception
   {
      ContentStream cs = new BaseContentStream("to be or not to be".getBytes(), null, "text/plain");
      Document document = createDocument(rootFolder, "createDocumentSource", "cmis:document", cs, null);

      Document documentCopy = storage.createCopyOfDocument(document, rootFolder, VersioningState.MINOR);
      documentCopy.setName("createDocumentSourceCopy");
      storage.saveObject(documentCopy);

      // Check is node and content copied.
      assertTrue(session.itemExists("/createDocumentSourceCopy"));
      Node documentNode = (Node)session.getItem("/createDocumentSourceCopy");
      assertEquals("nt:file", documentNode.getPrimaryNodeType().getName());
      assertEquals("to be or not to be", documentNode.getProperty("jcr:content/jcr:data").getString());
      assertEquals("text/plain", documentNode.getProperty("jcr:content/jcr:mimeType").getString());

      assertFalse("Copy must have different name.", document.getName().equals(documentCopy.getName()));
      assertFalse("Copy must have different ID.", document.getObjectId().equals(documentCopy.getObjectId()));
      assertFalse("Copy must have different versionSeriesId.", document.getVersionSeriesId().equals(
         documentCopy.getVersionSeriesId()));
      assertFalse(documentCopy.isMajorVersion());
   }

   public void testCreateDocumentUnfiled() throws Exception
   {
      Document document = createDocument(null, "createUnfiledDocumentTest", "cmis:document", null, null);

      Node docNode = ((DocumentImpl)document).getNode();
      String path = docNode.getPath();
      assertTrue("Document must be created in unfiled store.", path.startsWith(StorageImpl.XCMIS_SYSTEM_PATH + "/"
         + StorageImpl.XCMIS_UNFILED));

      Collection<Folder> parents = document.getParents();
      assertEquals(0, parents.size());

      // Add document in root folder.
      rootFolder.addObject(document);
      parents = document.getParents();
      assertEquals(1, parents.size());
      assertEquals(rootFolder.getObjectId(), parents.iterator().next().getObjectId());
   }

   public void testCreateFolder() throws Exception
   {
      PropertyDefinition<?> def = PropertyDefinitions.getPropertyDefinition("cmis:folder", CMIS.NAME);
      Map<String, Property<?>> properties = new HashMap<String, Property<?>>();
      properties.put(CMIS.NAME, new StringProperty(def.getId(), def.getQueryName(), def.getLocalName(), def
         .getDisplayName(), "createFolderTest"));

      Folder newFolder = storage.createFolder(rootFolder, "cmis:folder");
      newFolder.setProperties(properties);
      storage.saveObject(newFolder);

      assertTrue(session.itemExists("/createFolderTest"));
      Node folderNode = (Node)session.getItem("/createFolderTest");
      assertEquals("nt:folder", folderNode.getPrimaryNodeType().getName());
   }

   public void testCreatePolicy() throws Exception
   {
      Map<String, Property<?>> properties = new HashMap<String, Property<?>>();

      PropertyDefinition<?> defName = PropertyDefinitions.getPropertyDefinition("cmis:policy", CMIS.NAME);
      properties.put(CMIS.NAME, new StringProperty(defName.getId(), defName.getQueryName(), defName.getLocalName(),
         defName.getDisplayName(), "createPolicyTest"));

      PropertyDefinition<?> defPolicyText = PropertyDefinitions.getPropertyDefinition("cmis:policy", CMIS.POLICY_TEXT);
      properties.put(CMIS.POLICY_TEXT, new StringProperty(defPolicyText.getId(), defPolicyText.getQueryName(),
         defPolicyText.getLocalName(), defPolicyText.getDisplayName(), "simple policy"));

      ObjectData policy = storage.createPolicy(rootFolder, "cmis:policy");
      policy.setProperties(properties);
      storage.saveObject(policy);

      String expectedPath = StorageImpl.XCMIS_SYSTEM_PATH + "/" + StorageImpl.XCMIS_POLICIES + "/createPolicyTest";
      assertTrue(session.itemExists(expectedPath));
      Node policyNode = (Node)session.getItem(expectedPath);

      assertEquals("cmis:policy", policyNode.getPrimaryNodeType().getName());
      assertEquals("simple policy", policyNode.getProperty("cmis:policyText").getString());
   }

   public void testCreateRelationship() throws Exception
   {
      ObjectData sourceDoc = createDocument(rootFolder, "createRelationshipSource", "cmis:document", null, null);
      ObjectData targetDoc = createDocument(rootFolder, "createRelationshipTarget", "cmis:document", null, null);

      Map<String, Property<?>> properties = new HashMap<String, Property<?>>();
      PropertyDefinition<?> defName = PropertyDefinitions.getPropertyDefinition("cmis:relationship", CMIS.NAME);
      properties.put(CMIS.NAME, new StringProperty(defName.getId(), defName.getQueryName(), defName.getLocalName(),
         defName.getDisplayName(), "createRelationshipTest"));

      Relationship relationship = storage.createRelationship(sourceDoc, targetDoc, "cmis:relationship");
      relationship.setProperties(properties);
      storage.saveObject(relationship);

      Node relationshipNode = ((ExtendedSession)session).getNodeByIdentifier(relationship.getObjectId());
      assertEquals("cmis:relationship", relationshipNode.getPrimaryNodeType().getName());
      assertEquals(sourceDoc.getObjectId(), relationshipNode.getProperty("cmis:sourceId").getString());
      assertEquals(targetDoc.getObjectId(), relationshipNode.getProperty("cmis:targetId").getString());
   }

   public void testDeleteContent() throws Exception
   {
      ContentStream cs = new BaseContentStream("to be or not to be".getBytes(), null, "text/plain");
      Document document = createDocument(rootFolder, "removeContentTest", "cmis:document", cs, null);
      Node documentNode = (Node)session.getItem("/removeContentTest");
      assertEquals("to be or not to be", documentNode.getProperty("jcr:content/jcr:data").getString());
      assertEquals("text/plain", documentNode.getProperty("jcr:content/jcr:mimeType").getString());

      document.setContentStream(null);
      storage.saveObject(document);

      documentNode = (Node)session.getItem("/removeContentTest");
      assertEquals("", documentNode.getProperty("jcr:content/jcr:data").getString());
      assertEquals("", documentNode.getProperty("jcr:content/jcr:mimeType").getString());
   }

   public void testDeleteDocument() throws Exception
   {
      Document document = createDocument(rootFolder, "deleteDocumentTest", "cmis:document", null, null);
      storage.deleteObject(document, true);
      assertFalse(session.itemExists("/deleteDocumentTest"));
   }

   public void testDeleteFolder() throws Exception
   {
      Folder folder = createFolder(rootFolder, "deleteFolderTest", "cmis:folder");
      storage.deleteObject(folder, true);
      assertFalse(session.itemExists("/deleteFolderTest"));
   }

   public void testDeleteFolderWithChildren() throws Exception
   {
      Folder folder = createFolder(rootFolder, "deleteFolderWithChildrenTest", "cmis:folder");
      Document document = createDocument(folder, "child1", "cmis:document", null, null);
      try
      {
         storage.deleteObject(folder, true);
         fail("ConstraintException should be thrown");
      }
      catch (ConstraintException e)
      {
         // OK
      }
      storage.deleteObject(document, true);
      // No children any more , should be able delete.
      storage.deleteObject(folder, true);
   }

   public void testDeleteMultifiledObject() throws Exception
   {
      Document document = createDocument(rootFolder, "deleteMultifiledTest", "cmis:document", null, null);

      Folder folder1 = createFolder(rootFolder, "deleteMultifiledTest01", "cmis:folder");
      Folder folder2 = createFolder(rootFolder, "deleteMultifiledTest02", "cmis:folder");
      Folder folder3 = createFolder(rootFolder, "deleteMultifiledTest03", "cmis:folder");
      folder1.addObject(document);
      folder2.addObject(document);
      folder3.addObject(document);

      assertTrue(folder1.getChildren(null).hasNext());
      assertTrue(folder2.getChildren(null).hasNext());
      assertTrue(folder3.getChildren(null).hasNext());
      assertTrue(session.itemExists("/deleteMultifiledTest"));

      storage.deleteObject(document, true);

      assertFalse(folder1.getChildren(null).hasNext());
      assertFalse(folder2.getChildren(null).hasNext());
      assertFalse(folder3.getChildren(null).hasNext());
      assertFalse(session.itemExists("/deleteMultifiledTest"));
   }

   public void testDeleteObjectWithRelationship() throws Exception
   {
      ObjectData sourceDoc =
         createDocument(rootFolder, "deleteObjectWithRelationshipSource", "cmis:document", null, null);
      ObjectData targetDoc =
         createDocument(rootFolder, "deleteObjectWithRelationshipTarget", "cmis:document", null, null);

      Relationship relationship = storage.createRelationship(sourceDoc, targetDoc, "cmis:relationship");
      relationship.setName("relationship01");
      storage.saveObject(relationship);

      try
      {
         storage.deleteObject(targetDoc, true);
         fail("ConstraintException should be thrown");
      }
      catch (ConstraintException e)
      {
         // OK
         System.out.println(e.getMessage());
      }
   }

   public void testDeletePolicy() throws Exception
   {
      Document document = createDocument(rootFolder, "deletePolicyTestDocument", "cmis:document", null, null);
      Policy policy = createPolicy(rootFolder, "deletePolicyTestPolicy01", "test delete policy", "cmis:policy");
      document.applyPolicy(policy);
      storage.saveObject(document);
      try
      {
         storage.deleteObject(policy, true);
         fail("ConstraintException should be thrown.");
      }
      catch (ConstraintException e)
      {
         // OK. Applied policy may not be deleted.
      }
      document.removePolicy(policy);
      storage.saveObject(document);

      // Should be able delete now.
      storage.deleteObject(policy, true);
   }

   public void testDeleteRootFolder() throws Exception
   {
      try
      {
         storage.deleteObject(rootFolder, true);
         fail("ConstraintException should be thrown");
      }
      catch (ConstraintException e)
      {
         // OK
      }
   }

   public void testDeleteTreeDelete() throws Exception
   {
      // Create tree.
      Folder folder1 = createFolder(rootFolder, "1", "cmis:folder");
      Folder folder2 = createFolder(folder1, "2", "cmis:folder");
      Folder folder3 = createFolder(folder2, "3", "cmis:folder");
      Folder folder4 = createFolder(folder3, "4", "cmis:folder");
      Folder folder5 = createFolder(folder1, "5", "cmis:folder");
      Folder folder6 = createFolder(folder5, "6", "cmis:folder");
      Folder folder7 = createFolder(folder3, "7", "cmis:folder");
      Document doc1 = createDocument(folder2, "doc1", "cmis:document", null, null);
      Document doc2 = createDocument(folder2, "doc2", "cmis:document", null, null);
      Document doc3 = createDocument(folder4, "doc3", "cmis:document", null, null);
      Document doc4 = createDocument(folder4, "doc4", "cmis:document", null, null);

      folder5.addObject(doc1);
      folder6.addObject(doc2);
      folder7.addObject(doc3);
      folder7.addObject(doc4);

      String doc1Id = doc1.getObjectId();
      String doc2Id = doc2.getObjectId();
      String doc3Id = doc3.getObjectId();
      String doc4Id = doc4.getObjectId();

      //      /
      //      |_ 1
      //        |_2
      //        | |_doc1
      //        | |_doc2
      //        | |_doc3
      //        | |_3
      //        |   |_4
      //        |   | |_doc4
      //        |   |_7
      //        |     |_doc3
      //        |     |_doc4
      //        |_5
      //          |_6
      //          | |_doc2
      //          |_doc1

      //      printTree(folder1);

      storage.deleteTree(folder2, true, UnfileObject.DELETE, true);

      // Expected result is
      //      /
      //      |_ 1
      //        |_5
      //          |_6

      try
      {
         doc1 = (Document)storage.getObject(doc1Id);
         fail(doc1 + " must be deleted.");
      }
      catch (ObjectNotFoundException e)
      {
         // ok
      }
      try
      {
         doc2 = (Document)storage.getObject(doc2Id);
         fail(doc2 + " must be deleted.");
      }
      catch (ObjectNotFoundException e)
      {
         // ok
      }
      try
      {
         doc3 = (Document)storage.getObject(doc3Id);
         fail(doc3 + " must be deleted.");
      }
      catch (ObjectNotFoundException e)
      {
         // ok
      }
      try
      {
         doc4 = (Document)storage.getObject(doc4Id);
         fail(doc4 + " must be deleted.");
      }
      catch (ObjectNotFoundException e)
      {
         // ok
      }
      //      printTree(folder1);
   }

   public void testDeleteTreeDeletesinglefiled() throws Exception
   {
      // Create tree.
      Folder folder1 = createFolder(rootFolder, "1", "cmis:folder");
      Folder folder2 = createFolder(folder1, "2", "cmis:folder");
      Folder folder3 = createFolder(folder2, "3", "cmis:folder");
      Folder folder4 = createFolder(folder3, "4", "cmis:folder");
      Folder folder5 = createFolder(folder1, "5", "cmis:folder");
      Folder folder6 = createFolder(folder5, "6", "cmis:folder");
      Folder folder7 = createFolder(folder3, "7", "cmis:folder");
      Document doc1 = createDocument(folder2, "doc1", "cmis:document", null, null);
      Document doc2 = createDocument(folder2, "doc2", "cmis:document", null, null);
      Document doc3 = createDocument(folder4, "doc3", "cmis:document", null, null);
      Document doc4 = createDocument(folder4, "doc4", "cmis:document", null, null);

      folder5.addObject(doc1);
      folder6.addObject(doc2);
      folder7.addObject(doc3);
      folder7.addObject(doc4);

      String doc1Id = doc1.getObjectId();
      String doc2Id = doc2.getObjectId();
      String doc3Id = doc3.getObjectId();
      String doc4Id = doc4.getObjectId();

      //      /
      //      |_ 1
      //        |_2
      //        | |_doc1
      //        | |_doc2
      //        | |_doc3
      //        | |_3
      //        |   |_4
      //        |   | |_doc4
      //        |   |_7
      //        |     |_doc3
      //        |     |_doc4
      //        |_5
      //          |_6
      //          | |_doc2
      //          |_doc1

      //      printTree(folder1);

      storage.deleteTree(folder2, true, UnfileObject.DELETESINGLEFILED, true);

      // Expected result is
      //      /
      //      |_ 1
      //        |_5
      //          |_6
      //          | |_doc2
      //          |_doc1

      doc1 = (Document)storage.getObject(doc1Id);
      doc2 = (Document)storage.getObject(doc2Id);
      try
      {
         doc3 = (Document)storage.getObject(doc3Id);
         fail(doc3 + " must be deleted.");
      }
      catch (ObjectNotFoundException e)
      {
         //ok
      }
      try
      {
         doc4 = (Document)storage.getObject(doc4Id);
         fail(doc3 + " must be deleted.");
      }
      catch (ObjectNotFoundException e)
      {
         //ok
      }

      Collection<Folder> doc1Parents = doc1.getParents();
      assertEquals(1, doc1Parents.size());
      assertEquals(folder5.getObjectId(), doc1Parents.iterator().next().getObjectId());
      Collection<Folder> doc2Parents = doc2.getParents();
      assertEquals(1, doc2Parents.size());
      assertEquals(folder6.getObjectId(), doc2Parents.iterator().next().getObjectId());

      //      printTree(folder1);
   }

   public void testDeleteTreeUnfile() throws Exception
   {
      // Create tree.
      Folder folder1 = createFolder(rootFolder, "1", "cmis:folder");
      Folder folder2 = createFolder(folder1, "2", "cmis:folder");
      Folder folder3 = createFolder(folder2, "3", "cmis:folder");
      Folder folder4 = createFolder(folder3, "4", "cmis:folder");
      Folder folder5 = createFolder(folder1, "5", "cmis:folder");
      Folder folder6 = createFolder(folder5, "6", "cmis:folder");
      Folder folder7 = createFolder(folder3, "7", "cmis:folder");
      Document doc1 = createDocument(folder2, "doc1", "cmis:document", null, null);
      Document doc2 = createDocument(folder2, "doc2", "cmis:document", null, null);
      Document doc3 = createDocument(folder4, "doc3", "cmis:document", null, null);
      Document doc4 = createDocument(folder4, "doc4", "cmis:document", null, null);

      folder5.addObject(doc1);
      folder6.addObject(doc2);
      folder7.addObject(doc3);
      folder7.addObject(doc4);

      String doc1Id = doc1.getObjectId();
      String doc2Id = doc2.getObjectId();
      String doc3Id = doc3.getObjectId();
      String doc4Id = doc4.getObjectId();

      //      /
      //      |_ 1
      //        |_2
      //        | |_doc1
      //        | |_doc2
      //        | |_doc3
      //        | |_3
      //        |   |_4
      //        |   | |_doc4
      //        |   |_7
      //        |     |_doc3
      //        |     |_doc4
      //        |_5
      //          |_6
      //          | |_doc2
      //          |_doc1

      //      printTree(folder1);

      storage.deleteTree(folder2, true, UnfileObject.UNFILE, true);

      // Expected result is
      //      /
      //      |_ 1
      //        |_5
      //          |_6
      //          | |_doc2
      //          |_doc1
      // doc3 <unfiled>
      // doc4 <unfiled>

      doc1 = (Document)storage.getObject(doc1Id);
      doc2 = (Document)storage.getObject(doc2Id);
      doc3 = (Document)storage.getObject(doc3Id);
      doc4 = (Document)storage.getObject(doc4Id);

      Collection<Folder> doc1Parents = doc1.getParents();
      assertEquals(1, doc1Parents.size());
      assertEquals(folder5.getObjectId(), doc1Parents.iterator().next().getObjectId());
      Collection<Folder> doc2Parents = doc2.getParents();
      assertEquals(1, doc2Parents.size());
      assertEquals(folder6.getObjectId(), doc2Parents.iterator().next().getObjectId());
      Collection<Folder> doc3Parents = doc3.getParents();
      assertEquals(0, doc3Parents.size());
      Collection<Folder> doc4Parents = doc4.getParents();
      assertEquals(0, doc4Parents.size());

      //      printTree(folder1);
   }

   public void testGetParent() throws Exception
   {
      Document document = createDocument(rootFolder, "getParentTest", "cmis:document", null, null);
      assertEquals(rootFolder.getObjectId(), document.getParent().getObjectId());
   }

   public void testGetParents() throws Exception
   {
      Document document = createDocument(rootFolder, "getParentsTest", "cmis:document", null, null);
      Collection<Folder> parents = document.getParents();
      assertEquals(1, parents.size());
      assertEquals(rootFolder.getObjectId(), parents.iterator().next().getObjectId());
      Folder folder = createFolder(rootFolder, "getParentsTestFolder01", "cmis:folder");
      folder.addObject(document);
      parents = document.getParents();
      assertEquals(2, parents.size());
      try
      {
         document.getParent();
         fail("ConstraintException should be thrown. Object has more then one parent.");
      }
      catch (ConstraintException e)
      {
         // OK. Object has more then one parent.
      }
   }

   public void testGetRootParent() throws Exception
   {
      try
      {
         rootFolder.getParent();
         fail("ConstraintException must be throw. No parent for root folder.");
      }
      catch (ConstraintException ce)
      {
         // OK
      }
   }

   public void testGetRootParents() throws Exception
   {
      assertTrue("Must be empty collection.", rootFolder.getParents().isEmpty());
   }

   public void testGetTypeChildren()
   {
      ItemsIterator<TypeDefinition> iterator = storage.getTypeChildren(null, true);
      List<String> result = new ArrayList<String>();
      while (iterator.hasNext())
      {
         TypeDefinition next = iterator.next();
         result.add(next.getId() + "," + next.getLocalName());
      }
      assertEquals(4, result.size());
      assertTrue(result.contains("cmis:document,nt:file"));;
      assertTrue(result.contains("cmis:folder,nt:folder"));;
      assertTrue(result.contains("cmis:policy,cmis:policy"));;
      assertTrue(result.contains("cmis:relationship,cmis:relationship"));;
   }

   public void testMoveDocument() throws Exception
   {
      ObjectData document = createDocument(rootFolder, "moveDocumentTest", "cmis:document", null, null);
      Folder targetFolder = createFolder(rootFolder, "moveDocumentTestDestination", "cmis:folder");

      assertTrue(session.itemExists("/moveDocumentTest"));
      assertFalse(session.itemExists("/moveDocumentTestDestination/moveDocumentTest"));
      storage.moveObject(document, targetFolder, rootFolder);
      assertFalse(session.itemExists("/moveDocumentTest"));
      assertTrue(session.itemExists("/moveDocumentTestDestination/moveDocumentTest"));
   }

   public void testMoveFolder() throws Exception
   {
      Folder folder = createFolder(rootFolder, "moveFolderTest", "cmis:folder");
      createDocument(folder, "childDocument", "cmis:document", null, null);
      Folder targetFolder = createFolder(rootFolder, "moveFolderTestDestination", "cmis:folder");

      assertTrue(session.itemExists("/moveFolderTest/childDocument"));
      assertTrue(session.itemExists("/moveFolderTest"));
      assertFalse(session.itemExists("/moveFolderTestDestination/moveFolderTest/childDocument"));
      assertFalse(session.itemExists("/moveFolderTestDestination/moveFolderTest"));
      storage.moveObject(folder, targetFolder, rootFolder);
      assertFalse(session.itemExists("/moveFolderTest/childDocument"));
      assertFalse(session.itemExists("/moveFolderTest"));
      assertTrue(session.itemExists("/moveFolderTestDestination/moveFolderTest"));
      assertTrue(session.itemExists("/moveFolderTestDestination/moveFolderTest/childDocument"));
   }

   public void testMultifiledChild() throws Exception
   {
      Document document = createDocument(rootFolder, "multifiledChildTest", "cmis:document", null, null);
      Folder folder1 = createFolder(rootFolder, "multifiledChildFolderTest01", "cmis:folder");
      Document child1 = createDocument(folder1, "child1", "cmis:document", null, null);

      List<String> chs = new ArrayList<String>();
      for (ItemsIterator<ObjectData> children = folder1.getChildren(null); children.hasNext();)
      {
         chs.add(children.next().getObjectId());
      }
      assertEquals(1, chs.size());

      folder1.addObject(document);

      chs.clear();
      for (ItemsIterator<ObjectData> children = folder1.getChildren(null); children.hasNext();)
      {
         chs.add(children.next().getObjectId());
      }

      assertEquals(2, chs.size());
   }

   public void testMultifiling() throws Exception
   {
      Document document = createDocument(rootFolder, "multifilingDocumentTest", "cmis:document", null, null);
      Folder folder1 = createFolder(rootFolder, "multifilingFolderTest1", "cmis:folder");
      Folder folder2 = createFolder(rootFolder, "multifilingFolderTest2", "cmis:folder");
      Folder folder3 = createFolder(rootFolder, "multifilingFolderTest3", "cmis:folder");
      Folder folder4 = createFolder(rootFolder, "multifilingFolderTest4", "cmis:folder");
      folder1.addObject(document);
      folder2.addObject(document);
      folder3.addObject(document);
      folder4.addObject(document);

      Set<String> expectedParents =
         new HashSet<String>(Arrays.asList(rootFolder.getObjectId(), folder1.getObjectId(), folder2.getObjectId(),
            folder3.getObjectId(), folder4.getObjectId()));
      Collection<Folder> parents = document.getParents();

      assertEquals(expectedParents.size(), parents.size());
      for (Folder f : parents)
      {
         assertTrue("Folder " + f.getObjectId() + " must be in parents list.", expectedParents
            .contains(f.getObjectId()));
      }

      // remove from three folders and check parents again
      folder1.removeObject(document);
      folder3.removeObject(document);
      rootFolder.removeObject(document);
      expectedParents = new HashSet<String>(Arrays.asList(folder2.getObjectId(), folder4.getObjectId()));

      parents = document.getParents();

      assertEquals(expectedParents.size(), parents.size());
      for (Folder f : parents)
      {
         assertTrue("Folder " + f.getObjectId() + " must be in parents list.", expectedParents
            .contains(f.getObjectId()));
      }
      System.out.println(">>> new location: " + ((DocumentImpl)document).getNode().getPath());
   }

   public void testRenameDocument() throws Exception
   {
      ContentStream cs = new BaseContentStream("to be or not to be".getBytes(), null, "text/plain");
      Document document = createDocument(rootFolder, "renameDocumentTest", "cmis:document", cs, null);
      document.setName("renameDocumentTest01");
      storage.saveObject(document);

      assertTrue(session.itemExists("/renameDocumentTest01"));

      assertEquals("renameDocumentTest01", document.getName());
      assertEquals("renameDocumentTest01", document.getProperty(CMIS.CONTENT_STREAM_FILE_NAME).getValues().get(0));
   }

   public void testRenameFolder() throws Exception
   {
      Folder folder = createFolder(rootFolder, "renameFolderTest", "cmis:folder");
      createDocument(folder, "child1", "cmis:document", null, null);
      folder.setName("renameFolderTest01");
      storage.saveObject(folder);

      assertTrue(session.itemExists("/renameFolderTest01"));
      assertTrue(session.itemExists("/renameFolderTest01/child1"));

      assertEquals("renameFolderTest01", folder.getName());
   }

   public void testSetContent() throws Exception
   {
      Document document = createDocument(rootFolder, "setContentTest", "cmis:document", null, null);
      Node documentNode = (Node)session.getItem("/setContentTest");
      assertEquals("", documentNode.getProperty("jcr:content/jcr:data").getString());
      assertEquals("", documentNode.getProperty("jcr:content/jcr:mimeType").getString());

      ContentStream cs = new BaseContentStream("to be or not to be".getBytes(), null, "text/plain");
      document.setContentStream(cs);
      storage.saveObject(document);

      documentNode = (Node)session.getItem("/setContentTest");
      assertEquals("to be or not to be", documentNode.getProperty("jcr:content/jcr:data").getString());
      assertEquals("text/plain", documentNode.getProperty("jcr:content/jcr:mimeType").getString());
   }

   public void testUnfileAll() throws Exception
   {
      Document document = createDocument(rootFolder, "unfilingDocumentAllTest", "cmis:document", null, null);

      Folder folder1 = createFolder(rootFolder, "unfilingFolderAllTest01", "cmis:folder");
      Folder folder2 = createFolder(rootFolder, "unfilingFolderAllTest02", "cmis:folder");
      Folder folder3 = createFolder(rootFolder, "unfilingFolderAllTest03", "cmis:folder");
      folder1.addObject(document);
      folder2.addObject(document);
      folder3.addObject(document);

      assertEquals(4, document.getParents().size());
      storage.unfileObject(document);
      assertNull(document.getParent());
      assertEquals(0, document.getParents().size());
   }

   public void testUnfiling() throws Exception
   {
      Document document = createDocument(rootFolder, "unfilingDocumentTest", "cmis:document", null, null);
      assertTrue(rootFolder.getChildren(null).hasNext());
      rootFolder.removeObject(document);
      assertFalse(rootFolder.getChildren(null).hasNext());

      assertFalse(session.itemExists("/unfilingDocumentTest"));

      Collection<Folder> parents = document.getParents();
      assertEquals(0, parents.size());
   }

   private void printTree(Folder folder) throws Exception
   {
      System.out.println("--------- TREE --------");
      System.out.println(folder.getPath());
      ((FolderImpl)folder).getNode().accept(new ItemVisitor()
      {
         int l = 0;

         public void visit(javax.jcr.Property property) throws RepositoryException
         {
         }

         public void visit(Node node) throws RepositoryException
         {
            l++;
            for (int i = 0; i < l; i++)
            {
               System.out.print("  ");
            }
            System.out.println(node.getName() + " <" + node.getPrimaryNodeType().getName() + ">");
            for (NodeIterator children = node.getNodes(); children.hasNext();)
            {
               children.nextNode().accept(this);
            }
            l--;
         }
      });

      // Unfiled storage
      System.out.println("------- UNFILED -------");
      for (NodeIterator iter =
         ((Node)session.getItem(StorageImpl.XCMIS_SYSTEM_PATH + "/" + StorageImpl.XCMIS_UNFILED)).getNodes(); iter
         .hasNext();)
      {
         for (NodeIterator iterator = iter.nextNode().getNodes(); iterator.hasNext();)
         {
            System.out.println(iterator.nextNode().getPath());
         }
      }
      System.out.println("-----------------------");
   }

   protected Document createDocument(Folder folder, String name, String typeId, ContentStream content,
      VersioningState versioningState) throws Exception
   {
      Document document =
         storage.createDocument(folder, typeId, versioningState == null ? VersioningState.MAJOR : versioningState);
      document.setName(name);
      document.setContentStream(content);
      storage.saveObject(document);
      return document;
   }

   protected Folder createFolder(Folder folder, String name, String typeId) throws Exception
   {
      Folder newFolder = storage.createFolder(folder, typeId);
      newFolder.setName(name);
      storage.saveObject(newFolder);
      return newFolder;
   }

   protected Policy createPolicy(Folder folder, String name, String policyText, String typeId) throws Exception
   {
      Map<String, Property<?>> properties = new HashMap<String, Property<?>>();

      PropertyDefinition<?> defName = PropertyDefinitions.getPropertyDefinition("cmis:policy", CMIS.NAME);
      properties.put(CMIS.NAME, new StringProperty(defName.getId(), defName.getQueryName(), defName.getLocalName(),
         defName.getDisplayName(), name));

      PropertyDefinition<?> defPolicyText = PropertyDefinitions.getPropertyDefinition("cmis:policy", CMIS.POLICY_TEXT);
      properties.put(CMIS.POLICY_TEXT, new StringProperty(defPolicyText.getId(), defPolicyText.getQueryName(),
         defPolicyText.getLocalName(), defPolicyText.getDisplayName(), policyText));

      Policy policy = storage.createPolicy(folder, typeId);
      policy.setProperties(properties);
      storage.saveObject(policy);

      return policy;
   }
}
