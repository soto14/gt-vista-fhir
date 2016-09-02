package org.gtri.fhir.api.vistaex.rest.service.provider;

import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.model.primitive.StringDt;
import ca.uhn.fhir.rest.annotation.*;
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

    @Read()
    public Patient getResourceById(@IdParam IdDt patientId){
        logger.debug("Searching patient {}", patientId.getValue());
        Patient patient = getVistaExResource().retrievePatient(patientId.getIdPart());
        logger.debug("Finished searching for patient");
        return patient;
    }

    @Search
    public List<Patient> searchById(@OptionalParam(name="id") StringParam patientId,
                                    @OptionalParam(name=Patient.SP_IDENTIFIER) StringParam patientIdentifier,
                                    @OptionalParam(name="_id") StringParam patientUnderScoreId){
        List<Patient> returnVals = new ArrayList<Patient>();
        //make call to VistA Ex API
        String idToUse = getPatientId(patientId, patientIdentifier, patientUnderScoreId);
        if( !idToUse.isEmpty() ) {
            logger.debug("Retrieving Patient {}", idToUse);
            Patient patient = getVistaExResource().retrievePatient(idToUse);
            logger.debug("Retrieved Patient");
            returnVals.add(patient);
        }
        return returnVals;
    }

    private String getPatientId( StringParam patientID, StringParam patientIdentifier, StringParam patientUnderScoreId){
        String idToUse = "";
        if( patientID != null ){
            idToUse = patientID.getValue();
        }
        else if( patientIdentifier != null ){
            idToUse = patientIdentifier.getValue();
        }
        else if( patientUnderScoreId != null ){
            idToUse = patientUnderScoreId.getValue();
        }
        return idToUse;
    }

}
