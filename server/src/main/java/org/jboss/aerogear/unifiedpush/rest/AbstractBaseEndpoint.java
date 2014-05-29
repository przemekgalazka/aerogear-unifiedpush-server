/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors
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

package org.jboss.aerogear.unifiedpush.rest;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

/**
 * Base class for all RESTful endpoints. Offers hooks for common features like validation
 */
public abstract class AbstractBaseEndpoint {

    protected final Logger logger = Logger.getLogger(getClass().getName());

    @Inject
    private Validator validator;

    /**
     * Generic validator used to identify constraint violations of the given model class. 
     * 
     * @param model object to validate
     * @throws ConstraintViolationException if constraint violations on the given model have been identified.
     */
    protected void validateModelClass(Object model) {
        final Set<ConstraintViolation<Object>> violations = validator.validate(model);

        // in case of an invalid model, we throw a ConstraintViolationException, containing the violations:
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(
                    new HashSet<ConstraintViolation<?>>(violations));
        }
    }

    /**
     * Helper function to create a 400 Bad Request response, containing a JSON map giving details about the violations
     * 
     * @param violations set of occurred constraint violations
     * @return 400 Bad Request response, containing details on the constraint violations 
     */
    protected ResponseBuilder createBadRequestResponse(Set<ConstraintViolation<?>> violations) {
        final Map<String, String> responseObj = new HashMap<String, String>();

        for (ConstraintViolation<?> violation : violations) {
            responseObj.put(violation.getPropertyPath().toString(),
                                violation.getMessage());
        }

        return Response.status(Response.Status.BAD_REQUEST)
                           .entity(responseObj);
    }

}
