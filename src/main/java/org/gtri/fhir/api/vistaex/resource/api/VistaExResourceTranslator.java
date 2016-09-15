package org.gtri.fhir.api.vistaex.resource.api;

//import ca.uhn.fhir.model.dstu.resource.*;
import ca.uhn.fhir.model.dstu2.resource.*;
import ca.uhn.fhir.model.dstu2.resource.AllergyIntolerance;
import ca.uhn.fhir.model.dstu2.resource.Condition;
import ca.uhn.fhir.model.dstu2.resource.MedicationAdministration;
import ca.uhn.fhir.model.dstu2.resource.Observation;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.model.dstu2.resource.Procedure;

import java.util.List;

/**
 * Created by es130 on 8/29/2016.
 */
public interface VistaExResourceTranslator {

    /**
     * Translates JSON for a Patient Object, from the VistaEx API, to a DTSU2 Patient
     * @param patientJSON the {@link String} JSON representation to translate to a DTSU2 Patient.
     * @return {@link Patient}.
     */
    public Patient translatePatient(String patientJSON);

    /**
     * Translates JSON for a Bundle of MedicationOrder Objects, from the VistaEx API, to a DTSU2 MedicationOrder
     * @param medicationOrderBundleJson the {@link String} JSON representation to translate to a DTSU2 MedicationOrder Bundle
     * @return {@link Bundle}.
     */
    public Bundle translateMedicationOrderForPatient(String medicationOrderBundleJson);

    /**
     * Translates JSON for a Bundle of Condition Objects, from the VistaEx API, to a DTSU2 Bundle of Conditions
     * @param conditionBundleJson the {@link String} JSON representation to translate to a DTSU2 Condition Bundle
     * @return {@link Bundle}.
     */
    public Bundle translateConditionBundleForPatient(String conditionBundleJson);

    /**
     * Translates JSON for a Bundle of Observation Objects, from the VistaEx API, to a DTSU2 Bundle of Observation
     * @param observationBundleJson the {@link String} JSON representation to translate to a DTSU2 Observation Bundle
     * @return {@link Bundle}.
     */
    public Bundle translateObservationForPatient(String observationBundleJson);

    /**
     * Translates JSON for a Bundle of Procedure Objects, from the VistaEx API, to a DTSU2 Procedure
     * @param procedureJson the {@link String} JSON representation to translate to a DTSU2
     * @return {@link Bundle}.
     */
    public Bundle translateProcedureForPatient(String procedureJson);

    /**
     * Translates JSON for a Bundle of MedicationAdministration Objects, from the VistaEx API, to a DTSU2 MedicationAdministration
     * @param medicationAdministrationJson the {@link String} JSON representation to translate to a DTSU2 MedicationAdministration
     * @return {@link Bundle}.
     */
    public Bundle translateMedicationAdministrationForPatient(String medicationAdministrationJson);

    /**
     * Translates JSON for a Bundle of AllergyIntollerance Objects, from the VistaEx API, to a DTSU2 AllergyIntolerance
     * @param allergyIntoleranceJson the {@link String} JSON representation to translate to a DTSU2 AllergyIntolerance
     * @return {@link Bundle}.
     */
    public Bundle translateAllergyIntoleranceForPatient(String allergyIntoleranceJson);

    /**
     * Translates JSON for a Visit Object, from the VistaEx API, to a DTSU2 Encounter
     * @param encounterJson
     * @return
     */
    public List<Encounter> translateEncounterforPatient(String encounterJson);
}
