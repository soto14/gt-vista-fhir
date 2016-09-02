package org.gtri.fhir.api.vistaex.rest.service.provider;

import ca.uhn.fhir.model.dstu2.resource.Bundle;
import ca.uhn.fhir.model.dstu2.resource.MedicationOrder;
import ca.uhn.fhir.model.dstu2.resource.Observation;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.rest.annotation.RequiredParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.server.IResourceProvider;

/**
 * Created by es130 on 8/31/2016.
 */
public class GtVistaExApiMedicationOrderResourceProvider extends GtVistaResourceProvider implements IResourceProvider {
    @Override
    public Class<MedicationOrder> getResourceType() {
        return MedicationOrder.class;
    }

    /**
     * Method to find all MedicationOrder for a patient by ID.
     *
     * @param patientId
     * @return
     */
    @Search
    public Bundle findMedicationOrderWithChain(
            @RequiredParam(name= Observation.SP_PATIENT)ReferenceParam patientId
    ){
        Bundle observationBundle = getVistaExResource().retrieveMedicationOrderForPatient(patientId.getValue());
        return observationBundle;
    }
}
