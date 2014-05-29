/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.unifiedpush.jpa.dao.impl;

import java.util.List;

import org.jboss.aerogear.unifiedpush.jpa.AbstractGenericDao;
import org.jboss.aerogear.unifiedpush.jpa.dao.PushApplicationDao;
import org.jboss.aerogear.unifiedpush.model.PushApplication;

public class PushApplicationDaoImpl extends AbstractGenericDao<PushApplication, String> implements PushApplicationDao {

    @SuppressWarnings("unchecked")
    @Override
    public List<PushApplication> findAllForDeveloper(String loginName) {
        return createQuery("select pa from " + PushApplication.class.getSimpleName() + " pa where pa.developer = :developer")
                .setParameter("developer", loginName).getResultList();
    }

    @Override
    public PushApplication findByPushApplicationIDForDeveloper(String pushApplicationID, String loginName) {
        return getSingleResultForQuery(createQuery(
                "select pa from " + PushApplication.class.getSimpleName() + " pa where pa.pushApplicationID = :pushApplicationID and pa.developer = :developer")
                .setParameter("pushApplicationID", pushApplicationID)
                .setParameter("developer", loginName));
    }

    @Override
    public PushApplication findByPushApplicationID(String pushApplicationID) {
        return getSingleResultForQuery(createQuery("select pa from " + PushApplication.class.getSimpleName() + " pa where pa.pushApplicationID = :pushApplicationID")
                .setParameter("pushApplicationID", pushApplicationID));
    }
}
