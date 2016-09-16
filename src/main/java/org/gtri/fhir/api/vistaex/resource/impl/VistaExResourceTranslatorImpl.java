package org.gtri.fhir.api.vistaex.resource.impl;

import ca.uhn.fhir.context.FhirContext;
//import ca.uhn.fhir.model.dstu.resource.*;
//import ca.uhn.fhir.model.dstu.resource.Medication;
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
    private static final String ARRIVAL_DATE_TIME_KEY = "arrivalDateTime";
    private static final String DISCHARGE_DATE_TIME_KEY = "dischargeDateTime";
    private static final String CATEGORY_CODE_KEY = "categoryCode";
    private static final String CATEGORY_NAME_KEY = "categoryName";
    private static final String DATA_KEY = "data";
    private static final String ITEMS_KEY = "items";
    private static final String LOCATION_UID_KEY = "locationUid";
    private static final String LOCATION_DISPLAY_NAME_KEY = "locationDisplayName";
    private static final String PATIENT_CLASS_NAME_KEY = "patientClassName";
    private static final String PATIENT_PID_KEY = "pid";
    private static final String PRIMARY_PROVIDER_KEY = "primaryProvider";
    private static final String PROVIDER_UID_KEY = "providerUid";
    private static final String PROVIDERS_KEY = "providers";
    private static final String PROVIDER_DISPLAY_NAME_KEY = "providerDisplayName";
    private static final String STAY_KEY = "stay";
    private static final String UID_KEY = "uid";


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
    private DateFormat dateFormat;

    /*========================================================================*/
    /* CONSTRUCTORS */
    /*========================================================================*/
    public VistaExResourceTranslatorImpl(){
        //create a dstu1Context
        dstu1Context = FhirContext.forDstu1();
        dstu2Context = FhirContext.forDstu2();
        dateFormat = new SimpleDateFormat("yyyymmddHHmm");
    }

    /*========================================================================*/
    /* GETTERS */
    /*========================================================================*/

    public DateFormat getDateFormat() {
        return dateFormat;
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

        //translate the medication prescription to a medication order
        translatedJson = translateMedicationPrescriptionToMedicationOrder(translatedJson);

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
        //Translate the Patient reference
        translatedJson = translatedJson.replaceAll("(\"patient\":\\s*\\{\\s*\"reference\":\\s*\")(\\w+;\\w+)(\")", "$1Patient/$2$3");
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
    public Bundle translateProcedureForPatient(String procedureJson) {
        logger.debug("Translating Procedure Bundle");
        IParser parser = dstu2Context.newJsonParser();
        //perform common translations
        String translatedJson = performCommonTranslations(procedureJson);
        //response from vista comes with patient element that maps to subject inDSTU2
        translatedJson = translatedJson.replaceAll("\"patient\"", "\"subject\"");
        //type maps to code
        translatedJson = translatedJson.replaceAll("\"type\"", "\"code\"");
        translatedJson = translatedJson.replaceAll(",\\s*\"primary\":\\s*\\w+", "");
        Bundle procedureBundle = parser.parseResource(Bundle.class, translatedJson);
        logger.debug("Finished Translating Procedure Bundle");
        return procedureBundle;
    }

    @Override
    public Bundle translateMedicationAdministrationForPatient(String medicationAdministrationJson) {
        logger.debug("Translating Medication Administration");
        IParser parser = dstu2Context.newJsonParser();
        //perform common translations
        String translatedJson = performCommonTranslations(medicationAdministrationJson);
        translatedJson = translateMedicationPrescriptionToMedicationOrder(translatedJson);
        Bundle medicationAdminBundle = parser.parseResource(Bundle.class, translatedJson);
        logger.debug("Finsihed Translating Medication Administration");
        return medicationAdminBundle;
    }

    @Override
    public Bundle translateAllergyIntoleranceForPatient(String allergyIntoleranceJson) {
        logger.debug("Translating Allergy Intolerance");
        IParser parser = dstu2Context.newJsonParser();
        //perform common translations
        String translatedJson = performCommonTranslations(allergyIntoleranceJson);
        //Vista Ex FHIR returns an element with an 'event" that should be 'reaction' in DSTU2
        translatedJson = translatedJson.replaceAll("\"event\"", "\"reaction\"");
        translatedJson = translatedJson.replaceAll("\"comment\":", "\"description\":");
        translatedJson = translatedJson.replaceAll("\"duration\":\\s*\\w+,", "");
        Bundle allergyBundle = parser.parseResource(Bundle.class, translatedJson);
        logger.debug("Finished Translating Allergy Intolerance");
        return allergyBundle;
    }

    @Override
    public List<Encounter> translateEncounterforPatient(String encounterJson) {
        logger.debug("Translating Visit to Encounter");
        List<Encounter> encounters = new ArrayList<Encounter>();
        JSONObject jsonObject = new JSONObject(encounterJson);
        //get data element
        JSONObject dataObject = jsonObject.optJSONObject(DATA_KEY);
        //get item aray
        JSONArray itemsArray = dataObject != null ? dataObject.optJSONArray(ITEMS_KEY) : null;
        String encounterId;
        String encounterClass;
        String categoryCodeStr;
        String categoryNameStr;


        if( itemsArray != null ) {
            JSONObject currItemObject;
            for (int i = 0; i < itemsArray.length(); i++) {
                //get an item object
                currItemObject = itemsArray.optJSONObject(i);
                //Create a new encounter object
                Encounter encounter = new Encounter();

                //set the ID for the encounter
                encounterId = currItemObject.optString(UID_KEY);
                encounter.setId(encounterId);

                //set class
                encounterClass = currItemObject.optString(PATIENT_CLASS_NAME_KEY);
                encounter.setClassElement(EncounterClassEnum.forCode(encounterClass.toLowerCase()));

                //set the Patient reference
                ResourceReferenceDt patientReference = createReference(currItemObject, PATIENT_PID_KEY, "");
                encounter.setPatient(patientReference);

                //create the participant list
                List<Encounter.Participant> encounterParticipants = createEncounterProviderList(currItemObject);
                encounter.setParticipant(encounterParticipants);

                //set the location
                List<Encounter.Location> locations = createEncounterLocation(currItemObject);
                encounter.setLocation(locations);

                //TODO: Figure out how to get real status from Vista Ex Visit, should not always be finished
                encounter.setStatus(EncounterStateEnum.FINISHED);

                //set the type
                categoryCodeStr = currItemObject.optString(CATEGORY_CODE_KEY);
                categoryNameStr = currItemObject.optString(CATEGORY_NAME_KEY);
                List<CodeableConceptDt> typeList = createEncounterType(categoryCodeStr, categoryNameStr);
                encounter.setType(typeList);

                //now set the class by looking at the item.categoryName
                EncounterClassEnum className = createEncounterClass(categoryNameStr);
                encounter.setClassElement(className);

                //get the start and end date
                JSONObject stay = currItemObject.optJSONObject(STAY_KEY);
                if( stay != null ){
                    PeriodDt periodDt = createEncounterStayPeriod(stay);
                    encounter.setPeriod(periodDt);
                }

                //add the encounter to the list
                encounters.add(encounter);
            }
        }
        logger.debug("Finished Translating Visit to Encounter");
        return encounters;
    }

    /*========================================================================*/
    /* PRIVATE METHODS */
    /*========================================================================*/

    private String translateMedicationPrescriptionToMedicationOrder(String jsonString){
        String translatedJson;

        //Change Type from MedicationPrescription to MedicationOrder
        translatedJson = jsonString.replaceAll("\"resourceType\":\\s*\"MedicationPrescription\",", "\"resourceType\": \"MedicationOrder\",");
        //medication becomes medicationReference in DSTU2
        //this replace covers replacement of both medication.reference, and the dispense.medication.reference
        translatedJson = translatedJson.replaceAll("\"medication\"", "\"medicationReference\"");
        //scheduledTiming becomes timing in DSTU2
        translatedJson = translatedJson.replaceAll("\"scheduledTiming\"", "\"timing\"");
        //Change dispense to dispenseRequest
        translatedJson = translatedJson.replaceAll("\"dispense\":", "\"dispenseRequest\":");
        //need to update the Substance object to use "code" instead of "type" to describe the substance
        //fun with Regex here. Find the JSON snippet that indicates the beginning of a Substance, create two groups, the
        //portion of the Regex in the parens. Then use those groups to maintain the existing substance definition
        //but change "type" to "code".
        translatedJson = translatedJson.replaceAll("(\"resourceType\":\\s*\"Substance\",\\s*\"id\":\\s*\"[\\w-]+\",\\s*\")type(\":)", "$1code$2");
        //Medication does not contain the field name in DSTU2, remove it from the DSTU1 input
        translatedJson = translatedJson.replaceAll("(\"resourceType\":\\s*\"Medication\",\\s*\"id\":\\s*\"[\\w-]+\",\\s*)\"name\":\\s*\"[\\w\\s,]+\",", "$1");

        return translatedJson;
    }

    /**
     * Takes in a JSONObject that represents a VistA Ex visit and generates a FHIR DSTU2 Encounter
     * stay period.
     * @param stay
     * @return
     */
    private PeriodDt createEncounterStayPeriod(JSONObject stay){
        Date startDate = null;
        Date endDate = null;
        PeriodDt periodDt = new PeriodDt();

        startDate = getDateForString(stay.optString(ARRIVAL_DATE_TIME_KEY), getDateFormat());
        endDate = getDateForString(stay.optString(DISCHARGE_DATE_TIME_KEY), getDateFormat());

        if( startDate != null ) {
            periodDt.setStartWithSecondsPrecision(startDate);
        }
        if( endDate != null ){
            periodDt.setEndWithSecondsPrecision(endDate);
        }
        return periodDt;
    }

    /**
     * Takes in a VistA Ex visit categoryName and generates the FHIR DSTU2 Encounter class for it.
     * @param categoryNameStr
     * @return
     */
    private EncounterClassEnum createEncounterClass(String categoryNameStr){
        //supported class codes
        //inpatient, outpatient, ambulatory, emergency, home, field, daytime, virtual, other
        EncounterClassEnum className = EncounterClassEnum.OTHER;
        String normalizedStr = categoryNameStr.toLowerCase();
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
        return className;
    }

    /**
     * Takes in a VistA Ex categoryCode and categoryName and generates a FHIR DSTU2 Encounter type for it.
     * @param categoryCodeStr
     * @param categoryNameStr
     * @return
     */
    private List<CodeableConceptDt> createEncounterType(String categoryCodeStr, String categoryNameStr){
        CodeableConceptDt typeConcept = new CodeableConceptDt();
        CodingDt codingDt = new CodingDt();
        codingDt.setCode(categoryCodeStr);
        codingDt.setDisplay(categoryNameStr);
        List<CodingDt> codingDtList = new ArrayList<CodingDt>();
        codingDtList.add(codingDt);
        typeConcept.setCoding(codingDtList);
        List<CodeableConceptDt> typeList = new ArrayList<CodeableConceptDt>();
        typeList.add(typeConcept);
        return typeList;
    }

    /**
     * Takes in a JSONObject that represents a VistA Ex visit and generates a FHIR DSTU2 Encounter Location Reference.
     * @param jsonObject
     * @return
     */
    private List<Encounter.Location> createEncounterLocation(JSONObject jsonObject){
        Encounter.Location eLocation = new Encounter.Location();
        ResourceReferenceDt referenceDt = createReference(jsonObject, LOCATION_UID_KEY, LOCATION_DISPLAY_NAME_KEY);
        eLocation.setLocation(referenceDt);
        List<Encounter.Location> locations = new ArrayList<Encounter.Location>();
        locations.add(eLocation);
        return locations;
    }

    /**
     * Takes in a Vista Ex visit object and generates a FHIR DSTU2 Encounter provider participant.
     * @param jsonObject
     * @return
     */
    private List<Encounter.Participant> createEncounterProviderList(JSONObject jsonObject){
        List<Encounter.Participant> encounterParticipants = new ArrayList<Encounter.Participant>();
        //get primary provider ref
        JSONObject primaryProvider = jsonObject.optJSONObject(PRIMARY_PROVIDER_KEY);
        if( primaryProvider != null ){
            addEncounterProviderToParticiantList(encounterParticipants, primaryProvider, true);
        }

        //get provider ref
        JSONArray providerArray = jsonObject.optJSONArray(PROVIDERS_KEY);
        if( providerArray != null ) {
            for (int j = 0; j < providerArray.length(); j++) {
                JSONObject provider = providerArray.getJSONObject(j);
                addEncounterProviderToParticiantList(encounterParticipants, provider, false);
            }
        }
        return encounterParticipants;
    }

    /**
     * Takes in a VistA Ex visit Provider JSON object and adds it to an Encounter.Participant list.
     * @param encounterParticipants - the list to modify
     * @param provider the provider to use
     * @param isPrimary true if the provider is the primary provider, false otherwise
     */
    private void addEncounterProviderToParticiantList(List<Encounter.Participant> encounterParticipants, JSONObject provider, Boolean isPrimary){
        Encounter.Participant primary = createEncounterProvider(provider, isPrimary);
        encounterParticipants.add(primary);
    }

    /**
     * Creates and configures an Encounter.Participant object from the JSONObject representing a VistA Ex Provider.
     * @param provider the provider to use
     * @param isPrimary true if primary, false otherwise
     * @return the generated Encounter.Participant.
     */
    private Encounter.Participant createEncounterProvider(JSONObject provider, Boolean isPrimary){
        ResourceReferenceDt primaryReference = createReference(provider, PROVIDER_UID_KEY, PROVIDER_DISPLAY_NAME_KEY);
        Encounter.Participant encounterProvider = new Encounter.Participant();
        if( isPrimary ){
            encounterProvider.setType(ParticipantTypeEnum.PPRF);
        }
        else{
            encounterProvider.setType(ParticipantTypeEnum.SPRF);
        }
        encounterProvider.setIndividual(primaryReference);
        return encounterProvider;
    }

    /**
     * Creates a FHIR DSTU2 ResourceReference to a VistA Ex element
     * @param jsonObject
     * @param referenceElement
     * @param displayNameElement
     * @return
     */
    private ResourceReferenceDt createReference(JSONObject jsonObject, String referenceElement, String displayNameElement){
        ResourceReferenceDt primaryReference = new ResourceReferenceDt();
        primaryReference.setReference(jsonObject.optString(referenceElement));
        primaryReference.setDisplay(jsonObject.optString(displayNameElement));
        return primaryReference;
    }

    /**
     * Creates a Date object from a string, following a specific format.
     * @param dateStr the String to convert to a date
     * @param df the date formatter to use.
     * @return Date
     */
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
//        String translatedJson = bundleJson.replaceAll("(\"link\":\\s*\\[\\s*\\{\\s*\")rel(\":\\s*\"\\w+\",\\s*\")href(\":\\s*\"[\\w:/\\.?=&\";-]+\\s*\\})", "$1relation$2url$3");
        String translatedJson = bundleJson.replaceAll("(\"link\":\\s*\\[\\s*\\{\\s*\")rel(\":\\s*\"\\w+\",\\s*\")href(\":\\s*\"[\\w:/\\.?=%&;-]+\"\\s*\\})", "$1relation$2url$3");
        //update units to unit to make Quantities valid from DSTU1 to DSTU2
        translatedJson = translatedJson.replaceAll("\"units\":", "\"unit\":");
        return translatedJson;
    }
}
