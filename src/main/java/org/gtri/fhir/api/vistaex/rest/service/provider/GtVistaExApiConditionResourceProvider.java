/*
 * Copyright 2016 Georgia Tech Research Corporation.
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
 *
 */

package org.gtri.fhir.api.vistaex.rest.service.provider;

import ca.uhn.fhir.model.dstu2.resource.Bundle;
import ca.uhn.fhir.model.dstu2.resource.Condition;
import ca.uhn.fhir.model.dstu2.resource.Observation;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.rest.annotation.RequiredParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.server.IResourceProvider;

public class GtVistaExApiConditionResourceProvider extends GtVistaResourceProvider implements IResourceProvider {

    public GtVistaExApiConditionResourceProvider() {
        super();
   }

    @Override
    public Class<Condition> getResourceType() {
        return Condition.class;
    }

    /**
     * Method to find all Conditions for a patient by ID.
     *
     * @param patientId
     * @return
     */
    @Search
    public Bundle findConditionWithChain(
            @RequiredParam(name= Observation.SP_PATIENT)ReferenceParam patientId
    ){
        Bundle observationBundle = getVistaExResource().retrieveConditionForPatient(patientId.getValue());
        return observationBundle;
    }

}
