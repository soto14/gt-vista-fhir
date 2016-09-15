package org.gtri.fhir.api.vistaex.rest.service.provider;

import ca.uhn.fhir.model.dstu2.resource.Bundle;
import ca.uhn.fhir.model.dstu2.resource.Procedure;
import ca.uhn.fhir.rest.annotation.RequiredParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import org.hl7.fhir.instance.model.api.IBaseResource;

/**
 * Created by es130 on 9/14/2016.
 */
public class GtVistaExApiProcedureResourceProvider extends GtVistaResourceProvider implements IResourceProvider{
    @Override
    public Class<Procedure> getResourceType() {
        return Procedure.class;
    }

    @Search
    public Bundle findProcedure(
            @RequiredParam(name=Procedure.SP_PATIENT)ReferenceParam patientId
    ){
        Bundle procedureBundle = getVistaExResource().retrieveProcedureForPatient(patientId.getValue());
        return procedureBundle;
    }
}
