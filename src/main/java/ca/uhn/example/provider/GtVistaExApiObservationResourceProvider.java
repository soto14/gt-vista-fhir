package ca.uhn.example.provider;

import ca.uhn.fhir.model.dstu2.resource.Bundle;
import ca.uhn.fhir.model.dstu2.resource.Observation;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.rest.annotation.RequiredParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import org.gtri.fhir.api.vistaex.resource.api.VistaExResource;
import org.gtri.fhir.api.vistaex.resource.impl.VistaExResourceImpl;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by es130 on 8/31/2016.
 */
public class GtVistaExApiObservationResourceProvider implements IResourceProvider {

//    @Autowired
    VistaExResource vistaExResource;

    public GtVistaExApiObservationResourceProvider(){
        vistaExResource = new VistaExResourceImpl();
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
            @RequiredParam(name=Observation.SP_PATIENT, chainWhitelist = {"id"})ReferenceParam patientId
    ){
        //TODO: At a later date may want to change the chained param from "id" to Patient.SP_IDENTIFIER, then
        //the incoming param will be "identifier".
        List<Observation> observations = new ArrayList<Observation>();
        vistaExResource.loginToVistaEx();
        Bundle observationBundle = vistaExResource.retrieveObservationForPatient(patientId.getValue());
        vistaExResource.logOutOfVistaEx();
        return observationBundle;
    }
}
