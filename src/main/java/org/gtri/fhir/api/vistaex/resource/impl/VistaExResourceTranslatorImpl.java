/*
 * Copyright 2016 Georgia Tech Research Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.gtri.fhir.api.vistaex.resource.impl;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.dstu2.composite.*;
import ca.uhn.fhir.model.dstu2.resource.*;
import ca.uhn.fhir.model.dstu2.valueset.EncounterClassEnum;
import ca.uhn.fhir.model.dstu2.valueset.EncounterStateEnum;
import ca.uhn.fhir.model.dstu2.valueset.MedicationAdministrationStatusEnum;
import ca.uhn.fhir.model.dstu2.valueset.ParticipantTypeEnum;
import ca.uhn.fhir.model.primitive.DateTimeDt;
import ca.uhn.fhir.model.primitive.DecimalDt;
import ca.uhn.fhir.parser.IParser;
import org.gtri.fhir.api.vistaex.resource.api.VistaExResourceTranslator;
import org.gtri.fhir.api.vistaex.rest.service.util.VistaUtil;
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
import java.util.stream.Collectors;

public class VistaExResourceTranslatorImpl implements VistaExResourceTranslator {

    private static final String MEDICATION_PRESCRIPTION = "MedicationPrescription";
    private static final String ARRIVAL_DATE_TIME_KEY = "arrivalDateTime";
    private static final String CATEGORY_CODE_KEY = "categoryCode";
    private static final String CATEGORY_NAME_KEY = "categoryName";
    private static final String CONTAINED_FIELD = "contained";
    private static final String DATA_KEY = "data";
    private static final String DATE_KEY = "dateTime";
    private static final String DATE_WRITTEN_FIELD = "dateWritten";
    private static final String DISCHARGE_DATE_TIME_KEY = "dischargeDateTime";
    private static final String DISPENSE_REQUEST_FIELD = "dispenseRequest";
    private static final String ICD9_URL = "http://hl7.org/fhir/sid/icd-9-cm";
    private static final String ID_FIELD = "id";
    private static final String ITEMS_KEY = "items";
    private static final String LAST_UPDATE_TIME_KEY= "lastUpdateTime";
    private static final String LOCATION_UID_KEY = "locationUid";
    private static final String LOCATION_DISPLAY_NAME_KEY = "locationDisplayName";
    private static final String MEDICATION_ADMIN_DOSAGE_FIELD = "dosage";
    private static final String MEDICATION_ADMIN_DOSAGE_QUANTITY_FIELD = "quantity";
    private static final String MEDICATION_ADMIN_DOSAGE_QUANTITY_VALUE_FIELD = "value";
    private static final String MEDICATION_ADMIN_DOSAGE_QUANTITY_UNIT_FIELD = "unit";
    private static final String MEDICATION_ADMIN_DOSAGE_QUANTITY_SYSTEM_FIELD = "system";
    private static final String MEDICATION_ADMIN_EFFECTIVE_TIME_FIELD = "effectiveTimePeriod";
    private static final String MEDICATION_ADMIN_EFFECTIVE_TIME_START_FIELD = "start";
    private static final String MEDICATION_ADMIN_EFFECTIVE_TIME_END_FIELD = "end";
    private static final String MEDICATION_ADMIN_PATIENT_FIELD = "patient";
    private static final String MEDICATION_ADMIN_STATUS_FIELD = "status";
    private static final String MEDICATION_CODE_FIELD = "code";
    private static final String MEDICATION_CODING_FIELD = "coding";
    private static final String MEDICATION_CODE_SYSTEM_FIELD = "system";
    private static final String MEDICATION_CODE_CODE_FIELD = "code";
    private static final String MEDICATION_CODE_DISPLAY_FIELD = "display";
    private static final String NUM_REPEATS_ALLOWED_FIELD = "numberOfRepeatsAllowed";
    private static final String PATIENT_CLASS_NAME_KEY = "patientClassName";
    private static final String PATIENT_PID_KEY = "pid";
    private static final String PRIMARY_PROVIDER_KEY = "primaryProvider";
    private static final String PROVIDER_UID_KEY = "providerUid";
    private static final String PROVIDERS_KEY = "providers";
    private static final String PROVIDER_DISPLAY_NAME_KEY = "providerDisplayName";
    private static final String QUANTITY_FIELD = "quantity";
    private static final String REFERENCE_FIELD = "reference";
    private static final String RESOURCE_FIELD = "resource";
    private static final String RESOURCE_TYPE_FIELD = "resourceType";
    private static final String RESOURCE_MEDICATION = "Medication";
    private static final String RESOURCE_MEDICATION_ORDER = "MedicationOrder";
    private static final String RXNORM_FHIR_URN = "http://www.nlm.nih.gov/research/umls/rxnorm";
    private static final String RXNORM_VISTAEX_ID = "urn:oid:2.16.840.1.113883.6.88";
    private static final String SNOMED_CT_FHIR_URN = "http://snomed.info/sct";
    private static final String SNOMED_CT_VISTAEX_ID = "SNOMED-CT";
    private static final String SNOMED_CT_VISTAEX_CODE_PREFIX = "urn:sct:";
    private static final String STAY_KEY = "stay";
    private static final String UID_KEY = "uid";
    private static final String UNITS_OF_MEASURE_ID = "urn:oid:2.16.840.1.113883.6.8";
    private static final String UNITS_OF_MEASURE_URN = "http://unitsofmeasure.org";
    private static final String VALIDITY_PERIOD_FIELD = "validityPeriod";
    private static final String VALUE_FIELD = "value";

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
    private DateFormat errorDateFormat;

    /*========================================================================*/
    /* CONSTRUCTORS */
    /*========================================================================*/
    public VistaExResourceTranslatorImpl(){
        //create a dstu1Context
        dstu1Context = FhirContext.forDstu1();
        dstu2Context = FhirContext.forDstu2();
        dateFormat = new SimpleDateFormat("yyyyMMddHHmm");
        errorDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    }

    /*========================================================================*/
    /* GETTERS */
    /*========================================================================*/

    public DateFormat getDateFormat() {
        return dateFormat;
    }

    public DateFormat getErrorDateFormat() {
        return errorDateFormat;
    }

    /*========================================================================*/
    /* PUBLIC METHODS */
    /*========================================================================*/

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
        translatedJson = VistaUtil.fixDivHtmlElements(translatedJson);
        Bundle allergyBundle = parser.parseResource(Bundle.class, translatedJson);
        logger.debug("Finished Translating Allergy Intolerance");
        return allergyBundle;
    }

    @Override
    public Bundle translateConditionBundleForPatient(String conditionBundleJson) {
        logger.debug("Translating ConditionBundle");
        //perform common translations
        String translatedJson = performCommonTranslations(conditionBundleJson);
        //manipulate the incoming JSON to convert from DSTU1 to DSTU2
        translatedJson = translatedJson .replaceAll("\"dateAsserted\"", "\"dateRecorded\"");
        //Translate the Patient reference
        translatedJson = translatedJson.replaceAll("(\"patient\":\\s*\\{\\s*\"reference\":\\s*\")\\w+;(\\w+)(\")", "$1Patient/$2$3");
        //translate the ICD9 codes
        translatedJson = translatedJson.replaceAll("(\"coding\":\\s*\\[\\s*\\{\\s*\"system\":\\s*\")[\\w\\d:\\.]+(\",\\s*\"code\":\\s*\")urn:icd:([\\w\\.]+\")", "$1" + ICD9_URL + "$2$3");
        //look for other ICD9 OIDs for ths system
        translatedJson = translatedJson.replaceAll("(\"system\":\\s*\")urn:oid:2.16.840.1.113883.6.42(\")", "$1" + ICD9_URL + "$2");
        IParser parser = dstu2Context.newJsonParser();
        Bundle dstuConditionBundle = parser.parseResource(Bundle.class, translatedJson);
        removeDuplicateConditionCodes(dstuConditionBundle);
        logger.debug("Finished translating ConditionBundle");
        return dstuConditionBundle;
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
                String patientRefStr = patientReference.getReference().getValue().replaceAll("(\\w+;)(\\d+)", "Patient/$2");
                patientReference.getReference().setValue(patientRefStr);
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
                PeriodDt periodDt = createEncounterStayPeriod(currItemObject);
                encounter.setPeriod(periodDt);

                //add the encounter to the list
                encounters.add(encounter);
            }
        }
        logger.debug("Finished Translating Visit to Encounter");
        return encounters;
    }

    @Override
    public List<MedicationAdministration> translateMedicationAdministrationForPatient(String medicationAdministrationJson) {
        logger.debug("Translating Medication Administration");
        IParser parser = dstu2Context.newJsonParser();
        //perform common translations
        String translatedJson = performCommonTranslations(medicationAdministrationJson);
        translatedJson = translateMedicationPrescriptionToMedicationOrder(translatedJson);

        List<MedicationAdministration> medicationAdministrations = buildMedicationAdministrationList(translatedJson);
        logger.debug("Finished Translating Medication Administration");
        return medicationAdministrations;
    }

    @Override
    public List<MedicationOrder> translateMedicationOrderForPatient(String medicationOrderBundleJson) {
        logger.debug("Translating Medication Order");

        //perform common translations
        String translatedJson = performCommonTranslations(medicationOrderBundleJson);

        //JSON coming back from server does not have the "resource" element for each resource in the entry portion of the bundle, add it
        //Fun with regex. Find the beginning of the MedicationPrescription definition and add a "resource: " in front of it.
        //The incoming JSON does not have it.
        translatedJson = translatedJson.replaceAll("(\\{\\s*\"resourceType\":\\s*\"MedicationPrescription\",)", "\"resource\": $1");

        //now wrap the resources in {}
        translatedJson = translatedJson.replaceAll("(\"entry\":\\s*\\[)", "$1{");
        translatedJson = translatedJson.replaceAll("(],\\s*\"total\":)", "}$1");
        translatedJson = translatedJson.replaceAll("(,\\s*)(\"resource\":)", "}$1{$2");

        //translate the medication prescription to a medication order
        translatedJson = translateMedicationPrescriptionToMedicationOrder(translatedJson);
        List<MedicationOrder> medicationOrderList = buildMedicationOrderList(translatedJson);

        logger.debug("Finsihed Translating Medication Order");
        return medicationOrderList;
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
        //add # to the front of the Performer reference ID to reference a contained object
        translatedJson = translatedJson.replaceAll("(\"performer\":\\s*\\[\\s*\\{\\s*\"reference\":\\s*\")([\\w-]+\")", "$1#$2");
        //generate the bundle from the translated JSON
        Bundle dstuObservationBundle = parser.parseResource(Bundle.class, translatedJson);
        //Create the components for systolic and diastolic pressures
        createSystolicAndDiastolicComponents(dstuObservationBundle);
        logger.debug("Finished Translating ObservationBundle");
        return dstuObservationBundle;
    }

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

    /*========================================================================*/
    /* PRIVATE METHODS */
    /*========================================================================*/

    /**
     * Takes a String representing a translated MedicationOrder Bundle from VistaEx and makes a list of
     * HAPI FHIR DSTU2 MedicationOrder Objects.
     * @param jsonBundle the bundle to process
     * @return a {@link List} of MedicationAdministration objects contained in the passed in bundle.
     */
    private List<MedicationOrder> buildMedicationOrderList(String jsonBundle){
        List<MedicationOrder> medicationOrderList = new ArrayList<MedicationOrder>();
        JSONObject bundleJson = new JSONObject(jsonBundle);
        JSONArray entryArray = bundleJson.getJSONArray("entry");
        //now traverse the Array of "resource" elements
        //for each "resource" element
        JSONObject currEntry;
        JSONObject currMedicationOrder;
        CodeableConceptDt medCodeableConcept;
        String currDateWritten;
        for( int i=0; i<entryArray.length(); i++){
            //get the current medication order
            currEntry = entryArray.optJSONObject(i);
            currMedicationOrder = currEntry.optJSONObject(RESOURCE_FIELD);
            //create a new MedicationOrder
            MedicationOrder medicationOrder = new MedicationOrder();

            //id
            medicationOrder.setId(currMedicationOrder.getString(ID_FIELD));

            //date written
            currDateWritten = currMedicationOrder.optString(DATE_WRITTEN_FIELD);
            DateTimeDt writtenDateTimeDt = new DateTimeDt();
            writtenDateTimeDt.setValueAsString(currDateWritten);
            medicationOrder.setDateWritten(writtenDateTimeDt);

            //patient
            //TODO not sure how we will get this information

            //medication codeable concept
            medCodeableConcept = getMedicationCodeableConceptFromMedicationOrder(currMedicationOrder);
            medicationOrder.setMedication(medCodeableConcept);

            //dispense request
            MedicationOrder.DispenseRequest dispenseRequest = getDispenseRequestFromMedicationOrder(currMedicationOrder);
            //set the medication codeable concept
            dispenseRequest.setMedication(medCodeableConcept);
            medicationOrder.setDispenseRequest(dispenseRequest);
            medicationOrderList.add(medicationOrder);
        }
        return medicationOrderList;
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
     * Takes a String representing a translated MedicationAdministration Bundle from VistaEx and makes a list of
     * HAPI FHIR DSTU2 MedicationAdministration Objects.
     * @param jsonBundle the bundle to process
     * @return a {@link List} of MedicationAdministration objects contained in the passed in bundle.
     */
    private List<MedicationAdministration> buildMedicationAdministrationList(String jsonBundle){
        List<MedicationAdministration> medicationAdministrationList = new ArrayList<MedicationAdministration>();
        JSONObject bundleJson = new JSONObject(jsonBundle);
        JSONArray entryArray = bundleJson.getJSONArray("entry");
        //now traverse the Array of "resource" elements
        //for each "resource" element
        JSONObject currEntry;
        JSONObject currMedicationAdministration;
        CodeableConceptDt medCodeableConcept;
        for( int i=0; i<entryArray.length(); i++){
            //get the resource it should have resourceType "MedicationAdministration"
            currEntry = entryArray.optJSONObject(i);
            currMedicationAdministration = currEntry.optJSONObject(RESOURCE_FIELD);
            MedicationAdministration medicationAdministration = new MedicationAdministration();

            //things to set for MedicationAdministration
            //id
            medicationAdministration.setId(currMedicationAdministration.getString(ID_FIELD));

            //status
            medicationAdministration.setStatus(MedicationAdministrationStatusEnum.forCode(currMedicationAdministration.getString(MEDICATION_ADMIN_STATUS_FIELD)));

            //patient
            ResourceReferenceDt patientReference = getMedicationAdminPatientReference(currMedicationAdministration);
            medicationAdministration.setPatient(patientReference);

            //effectiveTimePeriod
            PeriodDt effectiveTimePeriod = getMedicationAdminEffectiveTimePeriod(currMedicationAdministration);
            medicationAdministration.setEffectiveTime(effectiveTimePeriod);

            //dosage
            MedicationAdministration.Dosage dosage = getMedicationAdminDosage(currMedicationAdministration);
            medicationAdministration.setDosage(dosage);

            //medicationCodeableConcept
            medCodeableConcept = getMedicationAdminContainedMedication(currMedicationAdministration);
            medicationAdministration.setMedication(medCodeableConcept);

            medicationAdministrationList.add(medicationAdministration);
        }
        return medicationAdministrationList;
    }

    /**
     * Creates a date time from a date string.
     * @param dateTimeString the string representation of the date time.
     * @return
     */
    private DateTimeDt createDateTime(String dateTimeString){
        DateTimeDt dateTimeDt = new DateTimeDt();
        dateTimeDt.setValueAsString(dateTimeString);
        return dateTimeDt;
    }

    /**
     * Creates a {@link PeriodDt}
     * @param start the start time
     * @param end the end time
     * @return
     */
    private PeriodDt createTimePeriod(DateTimeDt start, DateTimeDt end){
        PeriodDt periodDt = new PeriodDt();
        periodDt.setStart(start);
        periodDt.setEnd(end);
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
     * Takes in a JSONObject that represents a VistA Ex visit and generates a FHIR DSTU2 Encounter
     * stay period.
     * @param visit
     * @return
     */
    private PeriodDt createEncounterStayPeriod(JSONObject visit){
        Date startDate = getDateForString(visit.optString(DATE_KEY), getDateFormat());
        Date endDate = getDateForString(visit.optString(LAST_UPDATE_TIME_KEY), getDateFormat());
        return createValidityPeriod(startDate, endDate);
    }

    /**
     * Takes a {@link Bundle} and updates the Observation resources for systolic and diastolic blood pressure readings
     * to pull the contained systolic and diastolic resources into a Component element of the Observation resource.
     * @param observationBundle
     */
    private void createSystolicAndDiastolicComponents(Bundle observationBundle){
        //look through all of the Observation
        for( Bundle.Entry entry : observationBundle.getEntry() ){
            //if observation is for blood pressure
            if(entry.getResource().getText().getDivAsString().toLowerCase().contains("blood pressure systolic and diastolic")){
                Observation currObservation = (Observation)entry.getResource();
                //we found a blood pressure
                //get the contained observations, create components for them, and add them to the main Observation
                List<IResource> observationsToRemove = new ArrayList<IResource>();
                currObservation.getContained().getContainedResources().forEach( containedResource -> {
                    if (containedResource instanceof Observation) {
                        //create a component
                        Observation.Component component = new Observation.Component();
                        component.setCode(((Observation) containedResource).getCode());
                        component.setValue(((Observation) containedResource).getValue());
                        if( component.getValue() instanceof QuantityDt){
                            QuantityDt valueQuantity = ((QuantityDt)component.getValue());
                            valueQuantity.setSystem(UNITS_OF_MEASURE_URN);
                            valueQuantity.setCode(valueQuantity.getUnit());
                        }
                        currObservation.addComponent(component);
                        observationsToRemove.add(containedResource);
                    }
                });
                //Find the resources in the contained element that are not Observations
                List<IResource> filteredContained = currObservation.getContained().getContainedResources()
                                                    .stream()
                                                    .filter(resource -> !(resource.getResourceName().trim().equalsIgnoreCase("Observation")))
                                                    .collect(Collectors.toList());
                //create a new contained element
                ContainedDt newContained = new ContainedDt();
                //set its content to be the filtered list
                newContained.setContainedResources(filteredContained);
                //set the new contained elements
                currObservation.setContained(newContained);
                //clear out the relations, since they are only for
                currObservation.setRelated(null);
            }
        }
    }

    /**
     * Takes in a JSONObject that represents a VistaEx MedicationOrder Validity Period and generates a
     * FHIR DSTU2 period.
     * @param validityPeriod
     * @return
     */
    private PeriodDt createValidityPeriod(JSONObject validityPeriod){
        String startDateString = validityPeriod.getString("start");
        String endDateString = validityPeriod.getString("end");
        SimpleDateFormat validitiyDateformater = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        Date startDate = getDateForString(startDateString, validitiyDateformater);
        Date endDate = getDateForString(endDateString, validitiyDateformater);
        return createValidityPeriod(startDate, endDate);
    }

    /**
     * Takes a start and end date and returns a PeriodDt
     *
     * @param startDate
     * @param endDate
     * @return
     */
    private PeriodDt createValidityPeriod(Date startDate, Date endDate){
        PeriodDt periodDt = new PeriodDt();
        if(startDate != null && endDate != null){
            periodDt.setStartWithSecondsPrecision(startDate);
            periodDt.setEndWithSecondsPrecision(endDate);
        }
        else if(startDate != null && endDate == null){
            periodDt.setStartWithSecondsPrecision(startDate);
        }
        else if(startDate == null && endDate != null){
            periodDt.setStartWithSecondsPrecision(endDate);
        }
        return periodDt;
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
                try{
                    date = getErrorDateFormat().parse(dateStr);
                }
                catch(ParseException pe2){
                    logger.warn("Could not parse date ", dateStr);
                }
            }
        }
        return date;
    }

    /**
     * Takes in a JSON representation of a VistaEx MedicationPrescription translated to a FHIR DSTU2 MedicationOrder and
     * returns a HAPI DispenseRequest object for the dispense request it contains.
     * @param medicationOrder
     * @return
     */
    private MedicationOrder.DispenseRequest getDispenseRequestFromMedicationOrder(JSONObject medicationOrder){
        MedicationOrder.DispenseRequest dispenseRequest = new MedicationOrder.DispenseRequest();
        //get the dispense JSONObject
        JSONObject dispense = medicationOrder.getJSONObject(DISPENSE_REQUEST_FIELD);
        //set the validity period
        JSONObject validityPeriod = dispense.getJSONObject(VALIDITY_PERIOD_FIELD);
        PeriodDt validityPeriodDt = createValidityPeriod(validityPeriod);
        dispenseRequest.setValidityPeriod(validityPeriodDt);

        //set the number of repeats allowed
        if(dispense.has(NUM_REPEATS_ALLOWED_FIELD)) {
            int repeats = dispense.optInt(NUM_REPEATS_ALLOWED_FIELD);
            dispenseRequest.setNumberOfRepeatsAllowed(repeats);
        }

        //set the quantity
        JSONObject quantity = dispense.getJSONObject(QUANTITY_FIELD);
        int quantityValue = quantity.getInt(VALUE_FIELD);
        SimpleQuantityDt simpleQuantityDt = new SimpleQuantityDt();
        DecimalDt decimalValue = new DecimalDt();
        decimalValue.setValueAsInteger(quantityValue);
        simpleQuantityDt.setValue(decimalValue);
        dispenseRequest.setQuantity(simpleQuantityDt);
        return dispenseRequest;
    }

    /**
     * Retrives the MedicationAdministration dosage information from the passed in {@link JSONObject}
     * @param medicationAdmin the {@link JSONObject} representing the MedicationAdministration to process.
     * @return the {@link ca.uhn.fhir.model.dstu2.resource.MedicationAdministration.Dosage} with the
     * dosage information found in the passed in object.
     */
    private MedicationAdministration.Dosage getMedicationAdminDosage(JSONObject medicationAdmin){
        JSONObject dosageObj = medicationAdmin.optJSONObject(MEDICATION_ADMIN_DOSAGE_FIELD);
        JSONObject dosageQuantityObj = dosageObj.optJSONObject(MEDICATION_ADMIN_DOSAGE_QUANTITY_FIELD);
        MedicationAdministration.Dosage dosage = new MedicationAdministration.Dosage();
        SimpleQuantityDt quantityDt = new SimpleQuantityDt();
        quantityDt.setValue(dosageQuantityObj.getDouble(MEDICATION_ADMIN_DOSAGE_QUANTITY_VALUE_FIELD));
        quantityDt.setUnit(dosageQuantityObj.getString(MEDICATION_ADMIN_DOSAGE_QUANTITY_UNIT_FIELD).toLowerCase());
        String dosageSystem = dosageQuantityObj.getString(MEDICATION_ADMIN_DOSAGE_QUANTITY_SYSTEM_FIELD);
        if(isUnitsOfMeasureDotOrg(dosageSystem)){
            quantityDt.setSystem(UNITS_OF_MEASURE_URN);
        }
        else{
            quantityDt.setSystem(dosageSystem);
        }
        dosage.setQuantity(quantityDt);
        return dosage;
    }

    /**
     * Takes a {@link JSONObject} representation of a VistaEx MedicationAdministration and returns the
     * EffeciveTimePeriod it contains.
     * @param medicationAdmin
     * @return
     */
    private PeriodDt getMedicationAdminEffectiveTimePeriod(JSONObject medicationAdmin){
        JSONObject effectiveTimeObj = medicationAdmin.getJSONObject(MEDICATION_ADMIN_EFFECTIVE_TIME_FIELD);
        String startTime = effectiveTimeObj.getString(MEDICATION_ADMIN_EFFECTIVE_TIME_START_FIELD);
        String endTime = effectiveTimeObj.getString(MEDICATION_ADMIN_EFFECTIVE_TIME_END_FIELD);
        return createTimePeriod(createDateTime(startTime), createDateTime(endTime));
    }

    /**
     * Takes a {@link JSONObject} representation of a VistaEx MedicationAdministration and returns the
     * Patient Refernce it contains.
     * @param medicationAdmin
     * @return
     */
    private ResourceReferenceDt getMedicationAdminPatientReference(JSONObject medicationAdmin){
        JSONObject patientJSONObj = medicationAdmin.optJSONObject(MEDICATION_ADMIN_PATIENT_FIELD);
        String patientId = patientJSONObj.getString(REFERENCE_FIELD);
        return new ResourceReferenceDt(patientId);
    }

    /**
     * Takes a {@link JSONObject} representation of a VistaEx MedicationAdministration and returns the
     * Medication in the contained array
     * @param currMedicationAdministration
     * @return
     */
    private CodeableConceptDt getMedicationAdminContainedMedication(JSONObject currMedicationAdministration){
        CodeableConceptDt medCodeableConcept = null;
        JSONObject currContained;
        //get the "contained" Array in "MedicationAdministration
        JSONArray medAdminContainedArray = currMedicationAdministration.getJSONArray(CONTAINED_FIELD);
        for( int j=0; j<medAdminContainedArray.length(); j++) {
            currContained = medAdminContainedArray.optJSONObject(j);
            medCodeableConcept = getMedicationCodeableConceptFromMedicationOrder(currContained);
            if( medCodeableConcept != null ){
                break;
            }
        }
        return medCodeableConcept;
    }

    /**
     * Searches a VistaEx MedicationOrder for a contained Medication and returns a HAPI DSTU2 Medication
     * CodeableConceptDt with the data for the medication.
     * @param medicationOrder
     * @return
     */
    private CodeableConceptDt getMedicationCodeableConceptFromMedicationOrder(JSONObject medicationOrder){
        CodeableConceptDt medCodeableConcept = null;
        if(medicationOrder.getString(RESOURCE_TYPE_FIELD).equals(RESOURCE_MEDICATION_ORDER)){
            //get the contained in the Medication Order
            JSONArray medOrderContainedArray = medicationOrder.getJSONArray(CONTAINED_FIELD);
            JSONObject currMedOrderContained;
            JSONObject currCode;
            JSONArray currCodingArray;
            for( int k=0; k< medOrderContainedArray.length(); k++) {
                currMedOrderContained = medOrderContainedArray.optJSONObject(k);
                if (currMedOrderContained.getString(RESOURCE_TYPE_FIELD).equals(RESOURCE_MEDICATION)) {
                    //from the "Medication" element get the "code" element
                    currCode = currMedOrderContained.getJSONObject(MEDICATION_CODE_FIELD);
                    //from the "code" element get the "coding" Array
                    currCodingArray = currCode.getJSONArray(MEDICATION_CODING_FIELD);
                    //search the elements in the "coding" array by looking at their "system", "code", "display" elements
                    medCodeableConcept = getMedicationCode(currCodingArray);
                    //found our Medication, so break out of the search in contained
                    break;
                }
            }
        }
        return medCodeableConcept;
    }

    /**
     * Looks at a {@link JSONArray} containing a Medication codes and extracts the correct code.
     * @param medicationCodeArray the Medication JSONArray to process.
     * @return a {@link CodeableConceptDt} for the code in the passed in JSONArray.
     */
    private CodeableConceptDt getMedicationCode(JSONArray medicationCodeArray){
        JSONObject currMedicationCode;
        CodeableConceptDt medCodeableConcept = null;
        String currSystem;
        String currCodeStr;
        String currDisplay;
        //There will only be one code in the Medication Code Array so get it by index
        if(medicationCodeArray.length() > 0) {
            currMedicationCode = medicationCodeArray.optJSONObject(0);
            //You only need one code, so first look for RXNORM, "urn:oid:2.16.840.1.113883.6.88" in system
            currSystem = currMedicationCode.getString(MEDICATION_CODE_SYSTEM_FIELD);
            currCodeStr = currMedicationCode.getString(MEDICATION_CODE_CODE_FIELD);
            currDisplay = currMedicationCode.getString(MEDICATION_CODE_DISPLAY_FIELD);
            //set the medication codeable concept
            medCodeableConcept = processMedicationCode(currSystem, currCodeStr, currDisplay);
        }
        return medCodeableConcept;
    }

    /**
     * Looks at a system value from a VistaEx Medication code and returns
     * true if it is an RXNorm code and false otherwise.
     * @param systemId the system ID to look at
     * @return
     */
    private boolean isRXNorm(String systemId){
        return systemId.equals(RXNORM_VISTAEX_ID);
    }

    /**
     * Looks at a system value from a VistaEx Medication code and returns
     * true if it is an unitsofmeasure.org code and false otherwise.
     * @param systemId the system ID to look at
     * @return
     */
    private boolean isUnitsOfMeasureDotOrg(String systemId){
        return systemId.equals(UNITS_OF_MEASURE_ID);
    }

    /**
     * Looks at a system value from a VistaEx Medication code and returns
     * true if it is a SNOMED-CT code and false otherwise.
     * @param systemId the system ID to look at
     * @return
     */
    private boolean isSNOMED(String systemId){
        return systemId.equals(SNOMED_CT_VISTAEX_ID);
    }

    /**
     * Performs common translations on passed in JSON from VistaEx
     * @param bundleJson
     * @return
     */
    private String performCommonTranslations(String bundleJson){
//        logger.debug("Translating {}", bundleJson);
        // some messages come in with the DSTU1 style bundles, update them
        String translatedJson = bundleJson.replaceAll("(\"link\":\\s*\\[\\s*\\{\\s*\")rel(\":\\s*\"\\w+\",\\s*\")href(\":\\s*\"[\\w:/\\.?=%&;-]+\"\\s*\\})", "$1relation$2url$3");
        //update units to unit to make Quantities valid from DSTU1 to DSTU2
        translatedJson = translatedJson.replaceAll("\"units\":", "\"unit\":");
        return translatedJson;
    }

    /**
     * Takes in the system, code, and display values used in a Medication code in VistaEx, and creates a {@link CodeableConceptDt}
     * out of them.
     * @param systemId the system valee in the VistaEx Medication code
     * @param code the code value in the VistaEx Medication code
     * @param display the display value in teh VistaEx Medication code
     * @return a {@link CodeableConceptDt} with the information of the passed in code.
     */
    private CodeableConceptDt processMedicationCode(String systemId, String code, String display){
        CodeableConceptDt medCodeableConcept;
        if( isRXNorm(systemId)){
            //return rx norm
            medCodeableConcept = new CodeableConceptDt(RXNORM_FHIR_URN, code);
        }
        else if(isSNOMED(systemId)){
            //return snomed
            medCodeableConcept = new CodeableConceptDt(SNOMED_CT_FHIR_URN, processVistaExSnomedCode(code));
        }
        else if( isUnitsOfMeasureDotOrg(systemId)){
            medCodeableConcept = new CodeableConceptDt(UNITS_OF_MEASURE_URN, code);
        }
        else{
            //do default code
            medCodeableConcept = new CodeableConceptDt(systemId, code);
        }
        //set the display of the coding in the codable concept. There will only be one.
        medCodeableConcept.getCodingFirstRep().setDisplay(display);
        return medCodeableConcept;
    }

    /**
     * Takes a VistaEx SNOMED-CT code value from a VistaEx Medication code and returns
     * the value minus the "urn:sct". For example, a VistaEx Medication code may contain the following
     * code value, "urn:sct:410942007", this method would return "410942007". If the passed in code does not
     * contain the "urn:sct:" substring then the original string is returned.
     * @param code the code to process
     * @return
     */
    private String processVistaExSnomedCode(String code){
        return code.replace(SNOMED_CT_VISTAEX_CODE_PREFIX, "");
    }

    /**
     * Takes a HAPI Bundle Object that represents a translated VistaEx Condition, and removes duplicate codes from
     * each condition in the Bundle.
     * @param dstuConditionBundle
     */
    private void removeDuplicateConditionCodes(Bundle dstuConditionBundle){
        //filter codes, so code sets only contain unique codes
        for( Bundle.Entry entry : dstuConditionBundle.getEntry() ){
            for( CodeableConceptDt codeableConceptDt : entry.getResource().getAllPopulatedChildElementsOfType(CodeableConceptDt.class) ){
                List<CodingDt> uniqueCodes = new ArrayList<CodingDt>();
                for( CodingDt code : codeableConceptDt.getCoding() ){
                    //have we seen the code
                    Boolean isCodeUnique = true;
                    for( CodingDt uniqueCode : uniqueCodes ){
                        if( uniqueCode.getCode().equals(code.getCode()) &&
                                uniqueCode.getSystem().equals(code.getSystem()) &&
                                uniqueCode.getDisplay().equals(code.getDisplay())
                                ){
                            //code is not unique
                            isCodeUnique = false;
                            break;
                        }
                    }
                    if(isCodeUnique){
                        uniqueCodes.add(code);
                    }
                }
                codeableConceptDt.setCoding( uniqueCodes );
            }
        }
    }

    /**
     * Takes a JSON string representing a MedicationPrescription from VistaEx and translates it to a
     * String containing a DSTU2 MedicationOrder.
     * @param jsonString the String containing the MedicationPrescription from VistaEx.
     * @return a String containing a MedicationOrder.
     */
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
        //change the structure of the ID used to reference the patient, it does not need the system ID
        translatedJson = translatedJson.replaceAll("(\"patient\":\\s*\\{\\s*\"reference\":\\s*\"[Pp]atient/)(\\w+);(\\d+\"\\s*\\})", "$1$3");
        return translatedJson;
    }
}
