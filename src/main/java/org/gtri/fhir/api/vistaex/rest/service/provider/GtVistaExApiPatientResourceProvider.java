package org.gtri.fhir.api.vistaex.rest.service.provider;

import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.rest.annotation.RequiredParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by es130 on 8/29/2016.
 */
public class GtVistaExApiPatientResourceProvider extends GtVistaResourceProvider implements IResourceProvider {

    private static final Logger logger = LoggerFactory.getLogger(GtVistaExApiPatientResourceProvider.class);

    public GtVistaExApiPatientResourceProvider(){
        super();
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
    public List<Patient> searchById(@RequiredParam(name=Patient.SP_IDENTIFIER) StringParam patientId){
//    public List<Patient> searchById(@RequiredParam(name=Patient.SP_IDENTIFIER) StringParam patientId){
        List<Patient> returnVals = new ArrayList<Patient>();
        logger.debug("Retrieving Patient {}", patientId.getValue());
        //make call to VistA Ex API
        Patient patient = getVistaExResource().retrievePatient(patientId.getValue());
        logger.debug("Retrieved Patient");
        returnVals.add(patient);
        return returnVals;
    }

}
