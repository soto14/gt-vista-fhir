package org.gtri.fhir.api.vistaex.resource.api;

import ca.uhn.fhir.model.dstu.resource.*;
import ca.uhn.fhir.model.dstu2.resource.*;
import ca.uhn.fhir.model.dstu2.resource.AllergyIntolerance;
import ca.uhn.fhir.model.dstu2.resource.Condition;
import ca.uhn.fhir.model.dstu2.resource.MedicationAdministration;
import ca.uhn.fhir.model.dstu2.resource.Observation;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.model.dstu2.resource.Procedure;

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
     * Translates JSON for a MedicationOrder Object, from the VistaEx API, to a DTSU2 MedicationOrder
     * @param medicationOrderJson the {@link String} JSON representation to translate to a DTSU2 MedicationOrder
     * @return {@link MedicationOrder}.
     */
    public MedicationOrder translateMedicationOrderForPatient(String medicationOrderJson);

    /**
     * Translates JSON for a Bundle of rCondition Objects, from the VistaEx API, to a DTSU2 Bundle of Conditions
     * @param conditionBundleJson the {@link String} JSON representation to translate to a DTSU2 Condition
     * @return {@link Bundle}.
     */
    public Bundle translateConditionBundleForPatient(String conditionBundleJson);

    /**
     * Translates JSON for a Observation Object, from the VistaEx API, to a DTSU2 Observation
     * @param observationJson the {@link String} JSON representation to translate to a DTSU2
     * @return {@link Observation}.
     */
    public Observation translateObservationForPatient(String observationJson);

    /**
     * Translates JSON for a Procedure Object, from the VistaEx API, to a DTSU2 Procedure
     * @param procedureJson the {@link String} JSON representation to translate to a DTSU2
     * @return {@link Procedure}.
     */
    public Procedure translateProcedureForPatient(String procedureJson);

    /**
     * Translates JSON for a MedicationAdministration Object, from the VistaEx API, to a DTSU2 MedicationAdministration
     * @param medicationAdministrationJson the {@link String} JSON representation to translate to a DTSU2 MedicationAdministration
     * @return {@link MedicationAdministration}.
     */
    public MedicationAdministration translateMedicationAdministrationForPatient(String medicationAdministrationJson);

    /**
     * Translates JSON for a AllergyIntollerance Object, from the VistaEx API, to a DTSU2 AllergyIntolerance
     * @param allergyIntoleranceJson the {@link String} JSON representation to translate to a DTSU2 AllergyIntolerance
     * @return {@link AllergyIntolerance}.
     */
    public AllergyIntolerance translateAllergyIntoleranceForPatient(String allergyIntoleranceJson);
}
