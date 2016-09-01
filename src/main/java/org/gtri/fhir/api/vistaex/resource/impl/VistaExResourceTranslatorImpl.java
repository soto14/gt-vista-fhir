package org.gtri.fhir.api.vistaex.resource.impl;

import ca.uhn.fhir.context.FhirContext;
//import ca.uhn.fhir.model.dstu.resource.*;
//import ca.uhn.fhir.model.dstu.resource.Medication;
import ca.uhn.fhir.model.dstu.valueset.EncounterTypeEnum;
import ca.uhn.fhir.model.dstu2.composite.CodeableConceptDt;
import ca.uhn.fhir.model.dstu2.composite.CodingDt;
import ca.uhn.fhir.model.dstu2.composite.PeriodDt;
import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu2.resource.*;
import ca.uhn.fhir.model.dstu2.valueset.EncounterClassEnum;
import ca.uhn.fhir.model.dstu2.valueset.EncounterStateEnum;
import ca.uhn.fhir.model.dstu2.valueset.ParticipantTypeEnum;
import ca.uhn.fhir.parser.IParser;
import org.gtri.fhir.api.vistaex.resource.api.VistaExResourceTranslator;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

    @Override
    public List<Encounter> translateEncounterforPatient(String encounterJson) {
        logger.debug("Translating Visit to Encounter");
        List<Encounter> encounters = new ArrayList<Encounter>();
        JSONObject jsonObject = new JSONObject(encounterJson);
        //get data element
        JSONObject dataObject = jsonObject.optJSONObject("data");
        //get item aray
        JSONArray itemsArray = dataObject != null ? dataObject.optJSONArray("items") : null;
        String encounterId;
        String encounterClass;
        String categoryCodeStr;
        String categoryNameStr;
        DateFormat df = new SimpleDateFormat("yyyymmddHHmm");

        if( itemsArray != null ) {
            JSONObject currItemObject;
            for (int i = 0; i < itemsArray.length(); i++) {
                //get an item object
                currItemObject = itemsArray.optJSONObject(i);
                //Create a new encounter object
                Encounter encounter = new Encounter();

                //set the ID for the encounter
                encounterId = currItemObject.optString("uid");
                encounter.setId(encounterId);

                //set class
                encounterClass = currItemObject.optString("patientClassName");
                encounter.setClassElement(EncounterClassEnum.forCode(encounterClass.toLowerCase()));

                //set the Patient reference
                ResourceReferenceDt patientReference = new ResourceReferenceDt();
                patientReference.setReference(currItemObject.optString("pid"));
                encounter.setPatient(patientReference);

                List<Encounter.Participant> encounterParticipants = new ArrayList<Encounter.Participant>();
                //get primary provider ref
                JSONObject primaryProvider = currItemObject.optJSONObject("primaryProvider");
                if( primaryProvider != null ){
                    ResourceReferenceDt primaryReference = new ResourceReferenceDt();
                    primaryReference.setReference(primaryProvider.optString("providerUid"));
                    Encounter.Participant primary = new Encounter.Participant();
                    primary.setType(ParticipantTypeEnum.PPRF);
                    primary.setIndividual(primaryReference);
                    encounterParticipants.add(primary);
                }

                //get provider ref
                JSONArray providerArray = currItemObject.optJSONArray("providers");
                if( providerArray != null ) {
                    for (int j = 0; j < providerArray.length(); j++) {
                        //TODO: refactor to use same code as primary
                        JSONObject provider = providerArray.getJSONObject(j);
                        ResourceReferenceDt providerReference = new ResourceReferenceDt();
                        providerReference.setReference(provider.optString("providerUid"));
                        Encounter.Participant providerPart = new Encounter.Participant();
                        providerPart.setType(ParticipantTypeEnum.SPRF);
                        providerPart.setIndividual(providerReference);
                        encounterParticipants.add(providerPart);
                    }
                }
                encounter.setParticipant(encounterParticipants);

                //set the location
                Location location = new Location();
                location.setDescription(currItemObject.optString("locationDisplayName"));
                location.setName(currItemObject.optString("locationName"));
                location.setId(currItemObject.optString("locationUid"));

                Encounter.Location eLocation = new Encounter.Location();
                ResourceReferenceDt referenceDt = new ResourceReferenceDt();
                referenceDt.setReference(currItemObject.optString("locationUid"));
                eLocation.setLocation(referenceDt);
                List<Encounter.Location> locations = new ArrayList<Encounter.Location>();
                encounter.setLocation(locations);

                //TODO: Figure out how to get real status from Vista Ex Visit, should not always be finished
                encounter.setStatus(EncounterStateEnum.FINISHED);

                //set the type
                categoryCodeStr = currItemObject.optString("categoryCode");
                categoryNameStr = currItemObject.optString("categoryName");
                String normalizedStr = categoryNameStr.toLowerCase();
                CodeableConceptDt typeConcept = new CodeableConceptDt();
                CodingDt codingDt = new CodingDt();
                codingDt.setCode(categoryCodeStr);
                codingDt.setDisplay(categoryNameStr);
                List<CodingDt> codingDtList = new ArrayList<CodingDt>();
                codingDtList.add(codingDt);
                typeConcept.setCoding(codingDtList);
                List<CodeableConceptDt> typeList = new ArrayList<CodeableConceptDt>();
                typeList.add(typeConcept);
                encounter.setType(typeList);

                //now set the class by looking at the item.categoryName
                //supported class codes
                //inpatient, outpatient, ambulatory, emergency, home, field, daytime, virtual, other
                EncounterClassEnum className = EncounterClassEnum.OTHER;

                if(normalizedStr.contains(EncounterClassEnum.AMBULATORY.getCode())){
                    className = EncounterClassEnum.AMBULATORY;
                }
                else if(normalizedStr.contains(EncounterClassEnum.DAYTIME.getCode())){
                    className = EncounterClassEnum.DAYTIME;
                }
                else if(normalizedStr.contains(EncounterClassEnum.EMERGENCY.getCode())){
                    className = EncounterClassEnum.EMERGENCY;
                }
                else if(normalizedStr.contains(EncounterClassEnum.FIELD.getCode())){
                    className = EncounterClassEnum.FIELD;
                }
                else if(normalizedStr.contains(EncounterClassEnum.HOME.getCode())){
                    className = EncounterClassEnum.HOME;
                }
                else if(normalizedStr.contains(EncounterClassEnum.INPATIENT.getCode())){
                    className = EncounterClassEnum.INPATIENT;
                }
                else if(normalizedStr.contains(EncounterClassEnum.OUTPATIENT.getCode())){
                    className = EncounterClassEnum.OUTPATIENT;
                }
                else if(normalizedStr.contains(EncounterClassEnum.VIRTUAL.getCode())){
                    className = EncounterClassEnum.VIRTUAL;
                }
                encounter.setClassElement(className);

                //get the start and end date
                JSONObject stay = currItemObject.optJSONObject("stay");
                if( stay != null ){
                    Date startDate = null;
                    Date endDate = null;
                    PeriodDt periodDt = new PeriodDt();
                    startDate = getDateForString(stay.optString("arrivalDateTime"), df);
                    endDate = getDateForString(stay.optString("dischargeDateTime"), df);

                    if( startDate != null ) {
                        periodDt.setStartWithSecondsPrecision(startDate);
                    }
                    if( endDate != null ){
                        periodDt.setEndWithSecondsPrecision(endDate);
                    }
                    encounter.setPeriod(periodDt);
                }

                encounters.add(encounter);
            }
        }
        logger.debug("Finished Translating Visit to Encounter");
        return encounters;
    }

    /*========================================================================*/
    /* PRIVATE METHODS */
    /*========================================================================*/
    private Date getDateForString( String dateStr, DateFormat df){
        Date date = null;
        if(!dateStr.isEmpty()) {
            try {
                date = df.parse(dateStr);
            }
            catch(ParseException pe){
                pe.printStackTrace();
            }
        }
        return date;
    }

    private String performCommonTranslations(String bundleJson){
        //some messages come in with the DSTU1 style bundles, update them
        String translatedJson = bundleJson.replaceAll("(\"link\":\\s*\\[\\s*\\{\\s*\")rel(\":\\s*\"\\w+\",\\s*\")href(\":\\s*\"[\\w:/\\.?=&\";-]+\\s*\\})", "$1relation$2url$3");
        //update units to unit to make Quantities valid from DSTU1 to DSTU2
        translatedJson = translatedJson.replaceAll("\"units\":", "\"unit\":");
        return translatedJson;
    }
}
