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
package org.jboss.aerogear.unifiedpush.rest.security;

import org.jboss.aerogear.unifiedpush.users.Developer;
import org.jboss.aerogear.security.auth.AuthenticationManager;
import org.jboss.aerogear.security.authz.IdentityManagement;
import org.jboss.aerogear.security.exception.AeroGearSecurityException;
import org.picketlink.idm.model.basic.Agent;
import org.picketlink.idm.model.basic.User;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

@Stateless
@Path("/auth")
public class AuthenticationEndpoint {

    @Inject
    private AuthenticationManager<Agent> authenticationManager;

    @Inject
    private IdentityManagement<User> configuration;

    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(final Developer developer) {

        authenticationManager.login(developer, developer.getPassword());

        return Response.ok().build();
    }

    @POST
    @Path("/logout")
    public Response logout() {
        try {
            authenticationManager.logout();
        } catch (AeroGearSecurityException agse) {
            return Response.status(Status.UNAUTHORIZED).build();
        }
        return Response.ok().build();
    }

    @PUT
    @Path("/update")
    public Response updateUserPasswordAndRole(final Developer developer) {

        User simpleUser = configuration.findByUsername(developer.getLoginName());
        configuration.reset(simpleUser, developer.getPassword(), developer.getNewPassword());

        return Response.ok().build();
    }
}
