/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.unifiedpush.jpa.dao.impl;

import org.jboss.aerogear.unifiedpush.model.AndroidVariant;
import org.jboss.aerogear.unifiedpush.model.InstallationImpl;
import org.jboss.aerogear.unifiedpush.model.PushApplication;
import org.jboss.aerogear.unifiedpush.model.SimplePushVariant;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.*;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

public class InstallationDaoTest {

    private EntityManager entityManager;
    private InstallationDaoImpl installationDao;
    private String androidVariantID;
    private String simplePushVariantID;

    @Before
    public void setUp() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("UnifiedPush");
        entityManager = emf.createEntityManager();

        // start the shindig
        entityManager.getTransaction().begin();

        this.createTestData(entityManager);
    }

    private void createTestData(EntityManager entityManager) {

        // create abd configure all the DAOs:
        PushApplicationDaoImpl pushApplicationDao = new PushApplicationDaoImpl();

        // generic variant DAO:
        VariantDaoImpl variantDao = new VariantDaoImpl();

        pushApplicationDao.setEntityManager(entityManager);
        variantDao.setEntityManager(entityManager);

        this.installationDao = new InstallationDaoImpl();
        this.installationDao.setEntityManager(entityManager);

        // create the PushApplication and a few variants:
        PushApplication pa = new PushApplication();
        pa.setName("PushApplication");
        pushApplicationDao.create(pa);

        AndroidVariant av = new AndroidVariant();
        av.setGoogleKey("Key");
        av.setName("Android");
        // stash the ID:
        this.androidVariantID = av.getVariantID();
        variantDao.create(av);

        SimplePushVariant sp = new SimplePushVariant();
        sp.setName("SimplePush");
        // stash the ID:
        this.simplePushVariantID = sp.getVariantID();
        variantDao.create(sp);

        // register the variants with the Push Application:
        pa.getAndroidVariants().add(av);
        pa.getSimplePushVariants().add(sp);
        pushApplicationDao.update(pa);

        // ============== Android client installations =========
        InstallationImpl android1 = new InstallationImpl();
        android1.setAlias("foo@bar.org");
        android1.setDeviceToken("123456");
        android1.setDeviceType("Android Phone");
        final Set<String> categoriesOne = new HashSet<String>();
        categoriesOne.add("soccer");
        android1.setCategories(categoriesOne);

        installationDao.create(android1);

        InstallationImpl android2 = new InstallationImpl();
        android2.setAlias("foo@bar.org");
        android2.setDeviceToken("678901");
        android2.setDeviceType("Android Tablet");
        final Set<String> categoriesTwo = new HashSet<String>();
        categoriesTwo.add("news");
        android2.setCategories(categoriesTwo);

        installationDao.create(android2);

        // disabled
        InstallationImpl android3 = new InstallationImpl();
        android3.setAlias("foo@bar.org");
        android3.setDeviceToken("543234234");
        android3.setDeviceType("Android Tablet");
        android3.setEnabled(false);

        installationDao.create(android3);

        // register them:
        av.getInstallations().add(android1);
        av.getInstallations().add(android2);
        variantDao.update(av);

        // ============== SimplePush client installations =========
        InstallationImpl simplePush1 = new InstallationImpl();
        simplePush1.setAlias("foo@bar.org");
        simplePush1.setSimplePushEndpoint("http://server:8080/update/" + UUID.randomUUID().toString());
        simplePush1.setDeviceToken("123456");
        simplePush1.setCategories(categoriesOne);

        installationDao.create(simplePush1);

        InstallationImpl simplePush2 = new InstallationImpl();
        simplePush2.setAlias("foo@bar.org");
        simplePush2.setSimplePushEndpoint("http://server:8080/update/" + UUID.randomUUID().toString());
        simplePush2.setCategories(categoriesTwo);
        simplePush2.setDeviceToken("1234567865432");

        installationDao.create(simplePush2);

        InstallationImpl simplePush3 = new InstallationImpl();
        simplePush3.setAlias("foo@bar.org");
        simplePush3.setSimplePushEndpoint("http://server:8080/update/" + UUID.randomUUID().toString());
        simplePush3.setCategories(categoriesTwo);
        simplePush3.setDeviceToken("167865432");
        simplePush3.setDeviceType("JavaFX Monitor");

        installationDao.create(simplePush3);

        // register the installation:
        sp.getInstallations().add(simplePush1);
        sp.getInstallations().add(simplePush2);
        sp.getInstallations().add(simplePush3);
        variantDao.update(sp);
    }

    @After
    public void tearDown() {
        entityManager.getTransaction().commit();

        entityManager.close();
    }

    @Test
    public void findDeviceTokensForOneInstallationOfOneVariant() {
        String[] alias = { "foo@bar.org" };
        List<String> tokens = installationDao.findAllDeviceTokenForVariantIDByCriteria(androidVariantID, null, Arrays.asList(alias), null);
        assertEquals(2, tokens.size());

        InstallationImpl one = installationDao.findInstallationForVariantByDeviceToken(androidVariantID, "123456");
        assertEquals("123456", one.getDeviceToken());

        final Set<String> tokenz = new HashSet<String>();
        tokenz.add("123456");
        tokenz.add("foobar223");
        List<InstallationImpl> list = installationDao.findInstallationsForVariantByDeviceTokens(androidVariantID, tokenz);
        assertEquals(1, list.size());
        assertEquals("123456", list.get(0).getDeviceToken());
    }

    @Test
    public void findDeviceTokensForAliasOfVariant() {
        String[] alias = { "foo@bar.org" };
        List<String> tokens = installationDao.findAllDeviceTokenForVariantIDByCriteria(androidVariantID, null, Arrays.asList(alias), null);
        assertEquals(2, tokens.size());
    }

    @Test
    public void findNoDeviceTokensForAliasOfVariant() {
        String[] alias = { "bar@foo.org" };
        List<String> tokens = installationDao.findAllDeviceTokenForVariantIDByCriteria(androidVariantID, null, Arrays.asList(alias), null);
        assertEquals(0, tokens.size());
    }

    @Test
    public void findDeviceTokensForAliasAndDeviceType() {
        String[] alias = { "foo@bar.org" };
        String[] types = { "Android Tablet" };
        List<String> tokens = installationDao.findAllDeviceTokenForVariantIDByCriteria(androidVariantID, null, Arrays.asList(alias), Arrays.asList(types));
        assertEquals(1, tokens.size());
        assertEquals("678901", tokens.get(0));
    }

    @Test
    public void findNoDeviceTokensForAliasAndUnusedDeviceType() {
        String[] alias = { "foo@bar.org" };
        String[] types = { "Android Clock" };
        List<String> tokens = installationDao.findAllDeviceTokenForVariantIDByCriteria(androidVariantID, null, Arrays.asList(alias), Arrays.asList(types));
        assertEquals(0, tokens.size());
    }

    @Test
    public void findZeroDeviceTokensForAliasAndCategoriesAndDeviceType() {
        String[] alias = { "foo@bar.org" };
        String[] types = { "Android Tablet" };
        String[] categories = { "soccer" };
        List<String> tokens = installationDao.findAllDeviceTokenForVariantIDByCriteria(androidVariantID, Arrays.asList(categories), Arrays.asList(alias), Arrays
                .asList(types));
        assertEquals(0, tokens.size());
    }

    @Test
    public void findOneDeviceTokensForAliasAndCategoriesAndDeviceType() {
        String[] alias = { "foo@bar.org" };
        String[] types = { "Android Phone" };
        String[] cats = { "soccer", "news", "weather" };
        List<String> tokens = installationDao.findAllDeviceTokenForVariantIDByCriteria(androidVariantID, Arrays.asList(cats), Arrays.asList(alias), Arrays.asList(types));
        assertEquals(1, tokens.size());
        assertEquals("123456", tokens.get(0));
    }

    @Test
    public void findTwoDeviceTokensForAliasAndCategories() {
        String[] alias = { "foo@bar.org" };
        String[] cats = { "soccer", "news", "weather" };
        List<String> tokens = installationDao.findAllDeviceTokenForVariantIDByCriteria(androidVariantID, Arrays.asList(cats), Arrays.asList(alias), null);
        assertEquals(2, tokens.size());
    }

    @Test
    public void findTwoDeviceTokensCategories() {
        String[] cats = { "soccer", "news", "weather" };
        List<String> tokens = installationDao.findAllDeviceTokenForVariantIDByCriteria(androidVariantID, Arrays.asList(cats), null, null);
        assertEquals(2, tokens.size());

    }

    @Test
    public void findAndDeleteOneInstallation() {
        final Set<String> tokenz = new HashSet<String>();
        tokenz.add("123456");
        tokenz.add("foobar223");
        List<InstallationImpl> list = installationDao.findInstallationsForVariantByDeviceTokens(androidVariantID, tokenz);
        assertEquals(1, list.size());

        InstallationImpl installation = list.get(0);
        assertEquals("123456", installation.getDeviceToken());

        installationDao.delete(installation);

        list = installationDao.findInstallationsForVariantByDeviceTokens(androidVariantID, tokenz);
        assertEquals(0, list.size());
    }

    @Test
    public void findAndDeleteTwoInstallations() {
        final Set<String> tokenz = new HashSet<String>();
        tokenz.add("123456");
        tokenz.add("678901");
        List<InstallationImpl> list = installationDao.findInstallationsForVariantByDeviceTokens(androidVariantID, tokenz);
        assertEquals(2, list.size());

        for (InstallationImpl installation : list) {
            installationDao.delete(installation);
        }

        list = installationDao.findInstallationsForVariantByDeviceTokens(androidVariantID, tokenz);
        assertEquals(0, list.size());
    }

    @Test
    public void deleteNonExistingInstallation() {
        InstallationImpl installation = new InstallationImpl();

        installationDao.delete(installation);
    }

    @Test
    public void findPushEndpointsForAlias() {
        String[] alias = { "foo@bar.org" };
        List<String> tokens = installationDao.findAllPushEndpointURLsForVariantIDByCriteria(simplePushVariantID, null, Arrays.asList(alias), null);
        assertEquals(3, tokens.size());
        assertTrue(tokens.get(0).startsWith("http://server:8080/update/"));
        assertTrue(tokens.get(1).startsWith("http://server:8080/update/"));
    }

    @Test
    public void findZeroPushEndpointsForAliasAndCategories() {
        String[] alias = { "foo@bar.org" };
        String[] categories = { "US Football" };
        List<String> tokens = installationDao.findAllPushEndpointURLsForVariantIDByCriteria(simplePushVariantID, Arrays.asList(categories), Arrays.asList(alias), null);
        assertEquals(0, tokens.size());
    }

    @Test
    public void findOnePushEndpointForAliasAndCategories() {
        String[] alias = { "foo@bar.org" };
        String[] cats = { "soccer", "weather" };
        List<String> tokens = installationDao.findAllPushEndpointURLsForVariantIDByCriteria(simplePushVariantID, Arrays.asList(cats), Arrays.asList(alias), null);
        assertEquals(1, tokens.size());
        assertTrue(tokens.get(0).startsWith("http://server:8080/update/"));

    }

    @Test
    public void findThreePushEndpointsForAliasAndCategories() {
        String[] alias = { "foo@bar.org" };
        String[] cats = { "soccer", "news", "weather" };
        List<String> tokens = installationDao.findAllPushEndpointURLsForVariantIDByCriteria(simplePushVariantID, Arrays.asList(cats), Arrays.asList(alias), null);
        assertEquals(3, tokens.size());
        assertTrue(tokens.get(0).startsWith("http://server:8080/update/"));
        assertTrue(tokens.get(1).startsWith("http://server:8080/update/"));
        assertTrue(tokens.get(2).startsWith("http://server:8080/update/"));
    }

    @Test
    public void findThreePushEndpointsForCategories() {
        String[] cats = { "soccer", "news", "weather" };
        List<String> tokens = installationDao.findAllPushEndpointURLsForVariantIDByCriteria(simplePushVariantID, Arrays.asList(cats), null, null);
        assertEquals(3, tokens.size());
        assertTrue(tokens.get(0).startsWith("http://server:8080/update/"));
        assertTrue(tokens.get(1).startsWith("http://server:8080/update/"));
        assertTrue(tokens.get(2).startsWith("http://server:8080/update/"));
    }

    @Test
    public void findPushEndpointsWithDeviceType() {
        String[] types = {"JavaFX Monitor"};
        List<String> tokens = installationDao.findAllPushEndpointURLsForVariantIDByCriteria(simplePushVariantID, null, null, Arrays.asList(types));
        assertEquals(1, tokens.size());
        assertTrue(tokens.get(0).startsWith("http://server:8080/update/"));
    }

    @Test
    public void findPushEndpointsWithoutDeviceType() {
        List<String> tokens = installationDao.findAllPushEndpointURLsForVariantIDByCriteria(simplePushVariantID, null, null, null);
        assertEquals(3, tokens.size());
        assertTrue(tokens.get(0).startsWith("http://server:8080/update/"));
    }

    @Test
    public void primaryKeyUnmodifiedAfterUpdate() {
        InstallationImpl android1 = new InstallationImpl();
        android1.setAlias("foo@bar.org");
        android1.setDeviceToken("123456");
        android1.setDeviceType("Android Phone");
        final Set<String> categoriesOne = new HashSet<String>();
        categoriesOne.add("soccer");
        android1.setCategories(categoriesOne);
        final String id = android1.getId();

        installationDao.create(android1);

        // flush to be sure that it's in the database
        entityManager.flush();
        // clear the cache otherwise finding the entity will not perform a select but get the entity from cache
        entityManager.clear();

        InstallationImpl installation = installationDao.find(InstallationImpl.class, id);

        assertEquals(id, installation.getId());
        assertEquals("Android Phone", installation.getDeviceType());

        final String alias = "foobar@bar.org";
        android1.setAlias(alias);
        installationDao.update(android1);
        entityManager.flush();
        entityManager.clear();

        installation = installationDao.find(InstallationImpl.class, id);

        assertEquals(alias, installation.getAlias());
    }
}