package org.gtri.fhir.api.vistaex.resource.api;

import ca.uhn.fhir.model.dstu2.resource.*;

import java.util.List;

/**
 * Created by es130 on 8/29/2016.
 */
public interface VistaExResource {

    public Boolean loginToVistaEx();
    public Boolean refreshSessionOnVistaEx();
    public Boolean logOutOfVistaEx();

    /**
     * Retrieves a DTSU2 Patient Object, from the VistaEx API, for a patient
     * @param patientId the patient ID to use for the search
     * @return {@link Patient} resource for the patient.
     */
    public Patient retrievePatient(String patientId);

    /**
     * Retrieves a DTSU2 Bundle of MedicationOrder Objects, from the VistaEx API, for a patient
     * @param patientId the patient ID to use for the search
     * @return {@link Bundle} resource for the patient.
     */
    public Bundle retrieveMedicationOrderForPatient(String patientId);

    /**
     * Retrieves a DTSU2 Bundle of Condition Objects, from the VistaEx API, for a patient
     * @param patientId the patient ID to use for the search
     * @return {@link Bundle} resource for the patient.
     */
    public Bundle retrieveConditionForPatient(String patientId);

    /**
     * Retrieves a DTSU2 Bundle of Observation Objects, from the VistaEx API, for a patient
     * @param patientId the patient ID to use for the search
     * @return {@link Bundle} resource for the patient.
     */
    public Bundle retrieveObservationForPatient(String patientId);

    /**
     * Retrieves a DTSU2 Bundle of Procedure Objects, from the VistaEx API, for a patient
     * @param patientId the patient ID to use for the search
     * @return {@link Bundle} resource for the patient.
     */
    public Bundle retrieveProcedureForPatient(String patientId);

    /**
     * Retrieves a DTSU2 Bundle of MedicationAdministration Object, from the VistaEx API, for a patient
     * @param patientId the patient ID to use for the search
     * @return {@link Bundle} resource for the patient.
     */
    public Bundle retrieveMedicationAdministrationForPatient(String patientId);

    /**
     * Retrieves a DTSU2 Bundle of AllergyIntollerance Objects, from the VistaEx API, for a patient
     * @param patientId the patient ID to use for the search
     * @return {@link Bundle} resource for the patient.
     */
    public Bundle retrieveAllergyIntoleranceForPatient(String patientId);

    /**
     * Retrieves Vista Ex Visit objects and converts it to a DTSU2 Encounter to the best of its ability.
     * @param patientId the patient ID to use for the search
     * @return List of {@link Encounter} resource for the patient.
     */
    public List<Encounter> retrieveEncountersForPatient(String patientId);
}
