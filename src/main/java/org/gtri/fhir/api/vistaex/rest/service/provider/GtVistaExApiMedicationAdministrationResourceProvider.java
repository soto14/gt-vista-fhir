package org.gtri.fhir.api.vistaex.rest.service.provider;

import ca.uhn.fhir.model.dstu2.resource.Bundle;
import ca.uhn.fhir.model.dstu2.resource.MedicationAdministration;
import ca.uhn.fhir.rest.annotation.RequiredParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import org.hl7.fhir.instance.model.api.IBaseResource;

/**
 * Created by es130 on 9/15/2016.
 */
public class GtVistaExApiMedicationAdministrationResourceProvider extends GtVistaResourceProvider implements IResourceProvider {
    @Override
    public Class<MedicationAdministration> getResourceType() {
        return MedicationAdministration.class;
    }

    @Search
    public Bundle findProcedure(
            @RequiredParam(name= MedicationAdministration.SP_PATIENT)ReferenceParam patientId
    ){
        Bundle medicationAdminBundle = getVistaExResource().retrieveMedicationAdministrationForPatient(patientId.getValue());
        return medicationAdminBundle;
    }
}
