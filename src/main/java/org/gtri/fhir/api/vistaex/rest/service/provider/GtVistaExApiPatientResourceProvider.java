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

import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.model.primitive.StringDt;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class GtVistaExApiPatientResourceProvider extends GtVistaResourceProvider implements IResourceProvider {

    private static final Logger logger = LoggerFactory.getLogger(GtVistaExApiPatientResourceProvider.class);

    public GtVistaExApiPatientResourceProvider(){
        super();
    }

    /**
     * The getResourceType method comes from IResourceProvider, and must be overridden to indicate what type of resource this provider supplies.
     */
    @Override
    public Class<Patient> getResourceType() {
        return Patient.class;
    }

    @Read()
    public Patient getResourceById(@IdParam IdDt patientId){
        logger.debug("Searching patient {}", patientId.getValue());
        Patient patient = getVistaExResource().retrievePatient(patientId.getIdPart());
        logger.debug("Finished searching for patient");
        return patient;
    }

    @Search
    public List<Patient> searchById(@OptionalParam(name="id") StringParam patientId,
                                    @OptionalParam(name=Patient.SP_IDENTIFIER) StringParam patientIdentifier,
                                    @OptionalParam(name="_id") StringParam patientUnderScoreId){
        List<Patient> returnVals = new ArrayList<Patient>();
        //make call to VistA Ex API
        String idToUse = getPatientId(patientId, patientIdentifier, patientUnderScoreId);
        if( !idToUse.isEmpty() ) {
            logger.debug("Retrieving Patient {}", idToUse);
            Patient patient = getVistaExResource().retrievePatient(idToUse);
            logger.debug("Retrieved Patient");
            returnVals.add(patient);
        }
        return returnVals;
    }

    private String getPatientId( StringParam patientID, StringParam patientIdentifier, StringParam patientUnderScoreId){
        String idToUse = "";
        if( patientID != null ){
            idToUse = patientID.getValue();
        }
        else if( patientIdentifier != null ){
            idToUse = patientIdentifier.getValue();
        }
        else if( patientUnderScoreId != null ){
            idToUse = patientUnderScoreId.getValue();
        }
        return idToUse;
    }

}
