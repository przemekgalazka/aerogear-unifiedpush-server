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

import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.jpa.AbstractGenericDao;
import org.jboss.aerogear.unifiedpush.model.AndroidVariant;
import org.jboss.aerogear.unifiedpush.model.InstallationImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class VariantDaoTest {

    private EntityManager entityManager;
    private VariantDaoImpl variantDao;
    private InstallationDaoImpl installationDao;


    @Before
    public void setUp() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("UnifiedPush");
        entityManager = emf.createEntityManager();

        // start the shindig
        entityManager.getTransaction().begin();

        variantDao = new VariantDaoImpl();
        variantDao.setEntityManager(entityManager);
        installationDao = new InstallationDaoImpl();
        installationDao.setEntityManager(entityManager);
    }

    @After
    public void tearDown() {
        entityManager.getTransaction().commit();

        entityManager.close();
    }

    @Test
    public void findVariantByIdForDeveloper() {

        final AndroidVariant av = new AndroidVariant();
        av.setGoogleKey("KEY");
        av.setDeveloper("admin");
        final String uuid  = av.getVariantID();

        variantDao.create(av);

        assertNotNull(variantDao.findByVariantIDForDeveloper(uuid, "admin"));
        assertNull(variantDao.findByVariantIDForDeveloper(null, "admin"));
        assertNull(variantDao.findByVariantIDForDeveloper(uuid, "mr x"));
    }

    @Test
    public void findVariantById() {

        final AndroidVariant av = new AndroidVariant();
        av.setGoogleKey("KEY");
        av.setDeveloper("admin");
        final String uuid  = av.getVariantID();

        variantDao.create(av);

        assertNotNull(variantDao.findByVariantID(uuid));
        assertNull(variantDao.findByVariantID(null));
    }

    @Test
    public void updateVariant() {

        final AndroidVariant av = new AndroidVariant();
        av.setGoogleKey("KEY");
        av.setDeveloper("admin");
        final String uuid  = av.getVariantID();

        variantDao.create(av);

        AndroidVariant queriedVariant = (AndroidVariant) variantDao.findByVariantID(uuid);
        final String primaryKey = queriedVariant.getId();
        assertNotNull(queriedVariant);
        assertEquals("KEY", queriedVariant.getGoogleKey());

        queriedVariant.setGoogleKey("NEW_KEY");
        variantDao.update(queriedVariant);

        queriedVariant = (AndroidVariant) variantDao.findByVariantID(uuid);
        assertNotNull(queriedVariant);
        assertEquals("NEW_KEY", queriedVariant.getGoogleKey());
        assertEquals(primaryKey, queriedVariant.getId());
    }

    @Test
    public void updateAndDeleteVariant() {

        final AndroidVariant av = new AndroidVariant();
        av.setGoogleKey("KEY");
        av.setDeveloper("admin");
        final String uuid  = av.getVariantID();

        variantDao.create(av);

        AndroidVariant queriedVariant = (AndroidVariant) variantDao.findByVariantID(uuid);
        final String primaryKey = queriedVariant.getId();
        assertNotNull(queriedVariant);
        assertEquals("KEY", queriedVariant.getGoogleKey());

        queriedVariant.setGoogleKey("NEW_KEY");
        variantDao.update(queriedVariant);

        queriedVariant = (AndroidVariant) variantDao.findByVariantID(uuid);
        assertNotNull(queriedVariant);
        assertEquals("NEW_KEY", queriedVariant.getGoogleKey());
        assertEquals(primaryKey, queriedVariant.getId());

        variantDao.delete(queriedVariant);
        assertNull(variantDao.findByVariantID(uuid));
    }

    @Test
    public void lookupNonExistingVariant() {
        Variant variant = variantDao.findByVariantIDForDeveloper("NOT-IN-DATABASE", "admin");
        assertNull(variant);
    }

    @Test
    public void variantIDUnmodifiedAfterUpdate() {

        final AndroidVariant av = new AndroidVariant();
        av.setGoogleKey("KEY");
        av.setDeveloper("admin");
        final String uuid  = av.getVariantID();

        variantDao.create(av);

        AndroidVariant queriedVariant = (AndroidVariant) variantDao.findByVariantID(uuid);
        final String primaryKey = queriedVariant.getId();
        assertEquals(uuid, queriedVariant.getVariantID());
        assertNotNull(queriedVariant);

        queriedVariant.setGoogleKey("NEW_KEY");
        variantDao.update(queriedVariant);

        queriedVariant = (AndroidVariant) variantDao.findByVariantID(uuid);
        assertNotNull(queriedVariant);
        assertEquals(uuid, queriedVariant.getVariantID());
        assertEquals(primaryKey, queriedVariant.getId());
    }

    @Test
    public void primaryKeyUnmodifiedAfterUpdate() {
        AndroidVariant av = new AndroidVariant();
        av.setGoogleKey("KEY");
        av.setDeveloper("admin");
        final String id  = av.getId();

        variantDao.create(av);

        // flush to be sure that it's in the database
        entityManager.flush();
        // clear the cache otherwise finding the entity will not perform a select but get the entity from cache
        entityManager.clear();

        AndroidVariant variant = (AndroidVariant) variantDao.find(AndroidVariant.class, id);

        assertEquals(id, variant.getId());

        av.setGoogleKey("NEW_KEY");
        variantDao.update(av);

        entityManager.flush();
        entityManager.clear();

        variant = (AndroidVariant) variantDao.find(AndroidVariant.class, id);

        assertEquals("NEW_KEY", variant.getGoogleKey());

        assertEquals(id, av.getId());
    }

    @Test
    public void deleteVariantIncludingInstallations() {

        final AndroidVariant av = new AndroidVariant();
        av.setGoogleKey("KEY");
        av.setDeveloper("admin");
        final String uuid  = av.getVariantID();

        variantDao.create(av);

        AndroidVariant queriedVariant = (AndroidVariant) variantDao.findByVariantID(uuid);
        assertNotNull(queriedVariant);
        assertEquals("KEY", queriedVariant.getGoogleKey());

        InstallationImpl androidInstallation1 = new InstallationImpl();
        androidInstallation1.setDeviceToken("12345432122323");
        installationDao.create(androidInstallation1);

        queriedVariant.getInstallations().add(androidInstallation1);
        variantDao.update(queriedVariant);

        assertNotNull(((AbstractGenericDao) variantDao).find(InstallationImpl.class, androidInstallation1.getId()));
        InstallationImpl storedInstallation = (InstallationImpl) ((AbstractGenericDao) variantDao).find(InstallationImpl.class, androidInstallation1.getId());
        assertEquals(androidInstallation1.getId(), storedInstallation.getId());

        variantDao.delete(queriedVariant);
        assertNull(variantDao.findByVariantID(uuid));

        // Installation should be gone...
        assertNull(((AbstractGenericDao) variantDao).find(InstallationImpl.class, androidInstallation1.getId()));
    }
}
