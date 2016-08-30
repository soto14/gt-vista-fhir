package org.gtri.fhir.api.vistaex.resource.impl;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.dstu.resource.*;
import ca.uhn.fhir.model.dstu2.resource.*;
import ca.uhn.fhir.model.dstu2.resource.AllergyIntolerance;
import ca.uhn.fhir.model.dstu2.resource.Condition;
import ca.uhn.fhir.model.dstu2.resource.MedicationAdministration;
import ca.uhn.fhir.model.dstu2.resource.Observation;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.model.dstu2.resource.Procedure;
import ca.uhn.fhir.parser.IParser;
import org.gtri.fhir.api.vistaex.resource.api.VistaExResourceTranslator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by es130 on 8/29/2016.
 */
public class VistaExResourceTranslatorImpl implements VistaExResourceTranslator {

    /*========================================================================*/
    /* PRIVATE VARIABLES */
    /*========================================================================*/
    private final Logger logger = LoggerFactory.getLogger(VistaExResourceTranslatorImpl.class);

    /** NOTE FROM HAPI FHIR DOCS
     * Performance tip: The FhirContext is an expensive object to create, so you should try to create
     * it once and keep it around during the life of your application. Parsers, on the other hand,
     * are very lightweight and do not need to be reused.
     */
    private FhirContext dstu1Context;
    private FhirContext dstu2Context;

    /*========================================================================*/
    /* CONSTRUCTORS */
    /*========================================================================*/
    public VistaExResourceTranslatorImpl(){
        //create a dstu1Context
        dstu1Context = FhirContext.forDstu1();
        dstu2Context = FhirContext.forDstu2();
    }

    /*========================================================================*/
    /* PUBLIC METHODS */
    /*========================================================================*/
    @Override
    public Patient translatePatient(String patientJSON) {
        logger.debug("Translating Patient");
        IParser parser = dstu2Context.newJsonParser();
        Patient dstuPatient = parser.parseResource(Patient.class, patientJSON);
        logger.debug("FINISHED Translating Patient");
        return dstuPatient;
    }

    @Override
    public MedicationOrder translateMedicationOrderForPatient(String medicationOrderJson) {
        return null;
    }

    @Override
    public Bundle translateConditionBundleForPatient(String conditionBundleJson) {
        logger.debug("Translating ConditionBundle");
        //manipulate the incoming JSON to convert from DSTU1 to DSTU2
        String translatedJson = conditionBundleJson.replaceAll("\"dateAsserted\"", "\"dateRecorded\"");
        IParser parser = dstu2Context.newJsonParser();
        Bundle dstuConditionBundle = parser.parseResource(Bundle.class, translatedJson);
        logger.debug("Finished translating ConditionBundle");
        return dstuConditionBundle;
    }

    @Override
    public Observation translateObservationForPatient(String observationJson) {
        return null;
    }

    @Override
    public Procedure translateProcedureForPatient(String procedureJson) {
        return null;
    }

    @Override
    public MedicationAdministration translateMedicationAdministrationForPatient(String medicationAdministrationJson) {
        return null;
    }

    @Override
    public AllergyIntolerance translateAllergyIntoleranceForPatient(String allergyIntoleranceJson) {
        return null;
    }
}
