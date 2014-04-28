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
package org.jboss.aerogear.unifiedpush.keycloak;

import org.keycloak.adapters.AdapterDeploymentContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.representations.adapters.config.AdapterConfig;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.services.managers.RealmManager;
import org.keycloak.services.resources.KeycloakApplication;

import org.jboss.resteasy.core.Dispatcher;
import org.keycloak.util.JsonSerialization;
import org.keycloak.util.KeycloakUriBuilder;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class KeycloakServerApplication extends KeycloakApplication {

    private final Logger logger = Logger.getLogger(KeycloakServerApplication.class.getName());

    public KeycloakServerApplication(@Context ServletContext servletContext, @Context Dispatcher dispatcher) {
        super(servletContext, dispatcher);

        KeycloakSession session = factory.createSession();
        session.getTransaction().begin();
        try {
            InputStream is = servletContext.getResourceAsStream("/WEB-INF/testrealm.json");
            RealmRepresentation rep = loadJson(is, RealmRepresentation.class);
            RealmModel realm = importRealm(session, rep);
            AdapterDeploymentContext deploymentContext = (AdapterDeploymentContext)servletContext.getAttribute(AdapterDeploymentContext.class.getName());
            AdapterConfig adapterConfig = new AdapterConfig();
            String uri = KeycloakUriBuilder.fromUri("http://localhost:8080" ).path(servletContext.getContextPath()).path("auth").build().toString();
            logger.info("**** auth server url: " + uri);
            adapterConfig.setRealm("demo");
            adapterConfig.setResource("customer-portal");
            adapterConfig.setRealmKey(realm.getPublicKeyPem());
            Map<String, String> creds = new HashMap<String, String>();
            creds.put(CredentialRepresentation.SECRET, "password");
            adapterConfig.setCredentials(creds);
            adapterConfig.setAuthServerUrl(uri);
            adapterConfig.setSslNotRequired(true);
            deploymentContext.updateDeployment(adapterConfig);
            session.getTransaction().commit();
        } finally {
            session.close();
        }

    }

    public RealmModel importRealm(KeycloakSession session, RealmRepresentation rep) {
        RealmManager manager = new RealmManager(session);

        RealmModel realm = manager.getRealmByName(rep.getRealm());
        if (realm != null) {
            logger.info("Not importing realm " + rep.getRealm() + " realm already exists");
            return realm;
        }

        realm = manager.createRealm(rep.getId(), rep.getRealm());
        manager.importRealm(rep, realm);

        logger.info("Imported realm " + realm.getName());
        return realm;
    }

    private static <T> T loadJson(InputStream is, Class<T> type) {
        try {
            return JsonSerialization.readValue(is, type);
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse json", e);
        }
    }


}
