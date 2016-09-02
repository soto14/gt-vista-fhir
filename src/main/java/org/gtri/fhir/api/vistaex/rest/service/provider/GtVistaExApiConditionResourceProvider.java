package org.gtri.fhir.api.vistaex.rest.service.provider;

import ca.uhn.fhir.model.dstu2.resource.Bundle;
import ca.uhn.fhir.model.dstu2.resource.Condition;
import ca.uhn.fhir.model.dstu2.resource.Observation;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.rest.annotation.RequiredParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.server.IResourceProvider;

/**
 * Created by es130 on 8/31/2016.
 */
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
