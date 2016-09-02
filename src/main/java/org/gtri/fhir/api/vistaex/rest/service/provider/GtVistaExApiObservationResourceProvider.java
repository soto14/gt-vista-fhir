package org.gtri.fhir.api.vistaex.rest.service.provider;

import ca.uhn.fhir.model.dstu2.resource.Bundle;
import ca.uhn.fhir.model.dstu2.resource.Observation;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.rest.annotation.RequiredParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.server.IResourceProvider;

/**
 * Created by es130 on 8/31/2016.
 */
public class GtVistaExApiObservationResourceProvider extends GtVistaResourceProvider implements IResourceProvider {

    public GtVistaExApiObservationResourceProvider(){
        super();
    }

    @Override
    public Class<Observation> getResourceType() {
        return Observation.class;
    }

    /**
     * Method to find all Observations for a patient by ID.
     *
     * @param patientId
     * @return
     */
    @Search
    public Bundle findObservationWithChain(
            @RequiredParam(name=Observation.SP_PATIENT)ReferenceParam patientId
    ){
        //TODO: At a later date may want to change the chained param from "id" to Patient.SP_IDENTIFIER, then
        //the incoming param will be "identifier".
        Bundle observationBundle = getVistaExResource().retrieveObservationForPatient(patientId.getValue());
        return observationBundle;
    }
}
