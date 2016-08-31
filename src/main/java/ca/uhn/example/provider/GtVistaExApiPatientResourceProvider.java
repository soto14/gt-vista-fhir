package ca.uhn.example.provider;

import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.model.primitive.StringDt;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.RequiredParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import org.gtri.fhir.api.vistaex.resource.api.VistaExResource;
import org.gtri.fhir.api.vistaex.resource.impl.VistaExResourceImpl;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestContextListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by es130 on 8/29/2016.
 */
public class GtVistaExApiPatientResourceProvider implements IResourceProvider {

    private static final Logger logger = LoggerFactory.getLogger(GtVistaExApiPatientResourceProvider.class);

//    @Autowired
    protected VistaExResource vistaExResource;

    public GtVistaExApiPatientResourceProvider(){
        //I don't like doing this, but the Autowired annotation does not work, and this
        //method was the only way I could figure to get the VistaExResource Injected.
        WebApplicationContext parentAppCtx = ContextLoaderListener.getCurrentWebApplicationContext();
        vistaExResource = parentAppCtx.getBean(VistaExResource.class);
    }

    /**
     * The getResourceType method comes from IResourceProvider, and must be overridden to indicate what type of resource this provider supplies.
     */
    @Override
    public Class<Patient> getResourceType() {
        return Patient.class;
    }

    //TODO: would like to use read to get a Patient, but the vistaEx API ID
    //does not follow the format of the IdDt object regex: [a-z-Z0-9\-\.]{1,36}
    //as a result I am using search
    //@Read()
    //public Patient getResourceById(@IdParam IdDt theId){

    @Search
    public List<Patient> searchById(@RequiredParam(name="id") StringParam patientId){
//    public List<Patient> searchById(@RequiredParam(name=Patient.SP_IDENTIFIER) StringParam patientId){
        List<Patient> returnVals = new ArrayList<Patient>();
        logger.debug("Retrieving Patient {}", patientId.getValue());
        //make call to VistA Ex API
//        vistaExResource.loginToVistaEx();
        Patient patient = vistaExResource.retrievePatient(patientId.getValue());
//        vistaExResource.logOutOfVistaEx();
        logger.debug("Retrieved Patient");
        returnVals.add(patient);
        return returnVals;
    }

}
