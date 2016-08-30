package ca.uhn.example.provider;

import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.server.IResourceProvider;
import org.hl7.fhir.instance.model.api.IBaseResource;

/**
 * Created by es130 on 8/29/2016.
 */
public class GtVistaExApiResourceProvider implements IResourceProvider {

    /**
     * The getResourceType method comes from IResourceProvider, and must be overridden to indicate what type of resource this provider supplies.
     */
    @Override
    public Class<Patient> getResourceType() {
        return Patient.class;
    }

    @Read()
    public Patient getResourceById(@IdParam IdDt theId){

        //make call to VistA Ex API

        return null;
    }
}
