package org.gtri.fhir.api.vistaex.rest.service.provider;

import ca.uhn.fhir.model.dstu2.resource.*;
import ca.uhn.fhir.rest.annotation.RequiredParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.server.IResourceProvider;

import java.util.List;

/**
 * Created by es130 on 9/1/2016.
 */
public class GtVistaExApiEncounterResourceProvider extends GtVistaResourceProvider implements IResourceProvider {
    public GtVistaExApiEncounterResourceProvider() {
        super();
    }

    @Override
    public Class<Encounter> getResourceType() {
        return Encounter.class;
    }

    /**
     * Method to find all Encounters for a patient by ID.
     *
     * @param patientId
     * @return
     */
    @Search
    public List<Encounter> findEncounterWithChain(
            @RequiredParam(name= Observation.SP_PATIENT)ReferenceParam patientId
    ){
        List<Encounter> encounters = getVistaExResource().retrieveEncountersForPatient(patientId.getValue());
        return encounters;
    }
}
