package org.gtri.fhir.api.vistaex.resource.impl;

import ca.uhn.fhir.context.FhirContext;
//import ca.uhn.fhir.model.dstu.resource.*;
//import ca.uhn.fhir.model.dstu.resource.Medication;
import ca.uhn.fhir.model.dstu2.resource.AllergyIntolerance;
import ca.uhn.fhir.model.dstu2.resource.Bundle;
import ca.uhn.fhir.model.dstu2.resource.MedicationAdministration;
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

    private static final String MEDICATION_PRESCRIPTION = "MedicationPrescription";

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
        String translatedJson = performCommonTranslations(patientJSON);
        Patient dstuPatient = parser.parseResource(Patient.class, translatedJson);
        logger.debug("FINISHED Translating Patient");
        return dstuPatient;
    }

    @Override
    public Bundle translateMedicationOrderForPatient(String medicationOrderBundleJson) {
        logger.debug("Translating Medication Order");

        //perform common translations
        String translatedJson = performCommonTranslations(medicationOrderBundleJson);

        //JSON coming back from server does not have the "resource" element for each resource in the entry portion of the bundle, add it
        //Fun with regex. Find the begining fo the MedicationPrescription definition and add a "resource: " in frong of it.
        //The incoming JSON does not have it.
        translatedJson = translatedJson.replaceAll("(\\{\\s*\"resourceType\":\\s*\"MedicationPrescription\",)", "\"resource\": $1");

        //now wrap the resources in {}
        translatedJson = translatedJson.replaceAll("(\"entry\":\\s*\\[)", "$1{");
        translatedJson = translatedJson.replaceAll("(],\\s*\"total\":)", "}$1");
        translatedJson = translatedJson.replaceAll("(,\\s*)(\"resource\":)", "}$1{$2");

        //medication becomes medicationReference in DSTU2
        //this replace covers replacement of both medication.reference, and the dispense.medication.reference
        translatedJson = translatedJson.replaceAll("\"medication\"", "\"medicationReference\"");
        //scheduledTiming becomes timing in DSTU2
        translatedJson = translatedJson.replaceAll("\"scheduledTiming\"", "\"timing\"");
        //Change Type from MedicationPrescription to MedicationOrder
        translatedJson = translatedJson.replaceAll("\"resourceType\":\\s*\"MedicationPrescription\",", "\"resourceType\": \"MedicationOrder\",");
        //Change dispense to dispenseRequest
        translatedJson = translatedJson.replaceAll("\"dispense\":", "\"dispenseRequest\":");
        //need to update the Substance object to use "code" instead of "type" to describe the substance
        //fun with Regex here. Find the JSON snippet that indicates the beginning of a Substance, create two groups, the
        //portion of the Regex in the parens. Then use those groups to maintain the existing substance definition
        //but change "type" to "code".
        translatedJson = translatedJson.replaceAll("(\"resourceType\":\\s*\"Substance\",\\s*\"id\":\\s*\"[\\w-]+\",\\s*\")type(\":)", "$1code$2");
        //Medication does not contain the field name in DSTU2, remove it from the DSTU1 input
        translatedJson = translatedJson.replaceAll("(\"resourceType\":\\s*\"Medication\",\\s*\"id\":\\s*\"[\\w-]+\",\\s*)\"name\":\\s*\"[\\w\\s,]+\",", "$1");

        IParser parser = dstu2Context.newJsonParser();
        Bundle dstuMedicationOrderBundle = parser.parseResource(Bundle.class, translatedJson);
        logger.debug("Finsihed Translating Medication Order");
        return dstuMedicationOrderBundle;
    }

    @Override
    public Bundle translateConditionBundleForPatient(String conditionBundleJson) {
        logger.debug("Translating ConditionBundle");
        //perform common translations
        String translatedJson = performCommonTranslations(conditionBundleJson);
        //manipulate the incoming JSON to convert from DSTU1 to DSTU2
        translatedJson = translatedJson .replaceAll("\"dateAsserted\"", "\"dateRecorded\"");
        IParser parser = dstu2Context.newJsonParser();
        Bundle dstuConditionBundle = parser.parseResource(Bundle.class, translatedJson);
        logger.debug("Finished translating ConditionBundle");
        return dstuConditionBundle;
    }

    @Override
    public Bundle translateObservationForPatient(String observationBundleJson) {
        logger.debug("Translating ObservationBundle");
        IParser parser = dstu2Context.newJsonParser();
        //perform common translations
        String translatedJson = performCommonTranslations(observationBundleJson);
        //reliability was removed in DSTU2
        translatedJson = translatedJson.replaceAll("\"reliability\":\\s*\"\\w*\",", "");
        //appliesDateTime maps to effectiveDateTime in DSTU2
        translatedJson = translatedJson.replaceAll("\"appliesDateTime\"", "\"effectiveDateTime\"");
        //appliesPeriod maps to effectivePeriod in DSTU2
        translatedJson = translatedJson.replaceAll("\"appliesPeriod\"", "\"effectivePeriod\"");
        Bundle dstuObservationBundle = parser.parseResource(Bundle.class, translatedJson);
        logger.debug("Finished Translating ObservationBundle");
        return dstuObservationBundle;
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

    /*========================================================================*/
    /* PUBLIC METHODS */
    /*========================================================================*/
    private String performCommonTranslations(String bundleJson){
        //some messages come in with the DSTU1 style bundles, update them
        String translatedJson = bundleJson.replaceAll("(\"link\":\\s*\\[\\s*\\{\\s*\")rel(\":\\s*\"\\w+\",\\s*\")href(\":\\s*\"[\\w:/\\.?=&\";-]+\\s*\\})", "$1relation$2url$3");
        //update units to unit to make Quantities valid from DSTU1 to DSTU2
        translatedJson = translatedJson.replaceAll("\"units\":", "\"unit\":");
        return translatedJson;
    }
}
