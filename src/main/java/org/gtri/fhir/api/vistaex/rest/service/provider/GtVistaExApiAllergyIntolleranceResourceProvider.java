package org.gtri.fhir.api.vistaex.rest.service.provider;

import ca.uhn.fhir.model.dstu2.resource.AllergyIntolerance;
import ca.uhn.fhir.model.dstu2.resource.Bundle;
import ca.uhn.fhir.rest.annotation.RequiredParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import org.hl7.fhir.instance.model.api.IBaseResource;

/**
 * Created by es130 on 9/14/2016.
 */
public class GtVistaExApiAllergyIntolleranceResourceProvider extends GtVistaResourceProvider implements IResourceProvider {

    @Override
    public Class<AllergyIntolerance> getResourceType() {
        return AllergyIntolerance.class;
    }

    /**
     * Method to find all AllergyIntollerance objects for a patient by ID
     *
     * @param patientId
     * @return
     */
    @Search
    public Bundle findAllergyIntolerance(
            @RequiredParam(name=AllergyIntolerance.SP_PATIENT)ReferenceParam patientId
    ){
        Bundle allergyBundle = getVistaExResource().retrieveAllergyIntoleranceForPatient(patientId.getValue());
        return allergyBundle;
    }

}
