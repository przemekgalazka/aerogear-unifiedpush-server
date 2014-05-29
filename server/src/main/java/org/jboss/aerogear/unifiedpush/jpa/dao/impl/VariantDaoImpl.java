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

import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.jpa.AbstractGenericDao;
import org.jboss.aerogear.unifiedpush.jpa.dao.VariantDao;
import org.jboss.aerogear.unifiedpush.model.AbstractVariant;

public class VariantDaoImpl extends AbstractGenericDao<Variant, String> implements VariantDao {

    @Override
    public Variant findByVariantID(String variantID) {
        return getSingleResultForQuery(createQuery("select t from " + AbstractVariant.class.getSimpleName() + " t where t.variantID = :variantID")
                .setParameter("variantID", variantID));
    }

    @Override
    public Variant findByVariantIDForDeveloper(String variantID, String loginName) {
        return getSingleResultForQuery(createQuery("select t from " + AbstractVariant.class.getSimpleName() + " t where t.variantID = :variantID and t.developer = :developer")
                .setParameter("variantID", variantID)
                .setParameter("developer", loginName));
    }

}
