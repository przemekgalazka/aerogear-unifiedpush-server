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
package org.jboss.aerogear.unifiedpush.rest;

import org.jboss.aerogear.unifiedpush.rest.registry.applications.AndroidVariantEndpoint;
import org.jboss.aerogear.unifiedpush.rest.registry.applications.ChromePackagedAppEndpoint;
import org.jboss.aerogear.unifiedpush.rest.registry.applications.InstallationManagementEndpoint;
import org.jboss.aerogear.unifiedpush.rest.registry.applications.PushApplicationEndpoint;
import org.jboss.aerogear.unifiedpush.rest.registry.applications.SimplePushVariantEndpoint;
import org.jboss.aerogear.unifiedpush.rest.registry.applications.iOSVariantEndpoint;
import org.jboss.aerogear.unifiedpush.rest.registry.installations.InstallationRegistrationEndpoint;
import org.jboss.aerogear.unifiedpush.rest.sender.PushNotificationSenderEndpoint;
import org.jboss.aerogear.unifiedpush.rest.util.Ping;

import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;
//import javax.ws.rs.ApplicationPath;

/**
 * The JAX-RS {@link Application} representing the base
 * entry point for all RESTful HTTP requests.
 */
//@ApplicationPath("/rest")
public class UnifiedPushApplication extends Application {

    private final Set<Object> singletons = new HashSet<Object>();

    public UnifiedPushApplication() {

        // utils
        singletons.add(new Ping());

        // Sender
        singletons.add(new PushNotificationSenderEndpoint());

        // Device Registration:
        singletons.add(new InstallationRegistrationEndpoint());

        // Management Endpoints:
        singletons.add(new InstallationManagementEndpoint());
        singletons.add(new PushApplicationEndpoint());
        singletons.add(new AndroidVariantEndpoint());
        singletons.add(new ChromePackagedAppEndpoint());
        singletons.add(new iOSVariantEndpoint());
        singletons.add(new SimplePushVariantEndpoint());
    }



    @Override
    public Set<Object> getSingletons() {
        return singletons;
    }



}
