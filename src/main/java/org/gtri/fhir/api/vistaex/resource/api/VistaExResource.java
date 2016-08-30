package org.gtri.fhir.api.vistaex.resource.api;

import ca.uhn.fhir.model.dstu2.resource.*;

/**
 * Created by es130 on 8/29/2016.
 */
public interface VistaExResource {

    public Boolean loginToVistaEx();

    public Boolean logOutOfVistaEx();

    /**
     * Retrieves a DTSU2 Patient Object, from the VistaEx API, for a patient
     * @param patientId the patient ID to use for the search
     * @return {@link Patient} resource for the patient.
     */
    public Patient retrievePatient(String patientId);

    /**
     * Retrieves a DTSU2 MedicationOrder Object, from the VistaEx API, for a patient
     * @param patientId the patient ID to use for the search
     * @return {@link MedicationOrder} resource for the patient.
     */
    public MedicationOrder retrieveMedicationOrderForPatient(String patientId);

    /**
     * Retrieves a DTSU2 Condition Object, from the VistaEx API, for a patient
     * @param patientId the patient ID to use for the search
     * @return {@link Bundle} resource for the patient.
     */
    public Bundle retrieveConditionForPatient(String patientId);

    /**
     * Retrieves a DTSU2 Observation Object, from the VistaEx API, for a patient
     * @param patientId the patient ID to use for the search
     * @return {@link Observation} resource for the patient.
     */
    public Observation retrieveObservationForPatient(String patientId);

    /**
     * Retrieves a DTSU2 Procedure Object, from the VistaEx API, for a patient
     * @param patientId the patient ID to use for the search
     * @return {@link Procedure} resource for the patient.
     */
    public Procedure retrieveProcedureForPatient(String patientId);

    /**
     * Retrieves a DTSU2 MedicationAdministration Object, from the VistaEx API, for a patient
     * @param patientId the patient ID to use for the search
     * @return {@link MedicationAdministration} resource for the patient.
     */
    public MedicationAdministration retrieveMedicationAdministrationForPatient(String patientId);

    /**
     * Retrieves a DTSU2 AllergyIntollerance Object, from the VistaEx API, for a patient
     * @param patientId the patient ID to use for the search
     * @return {@link AllergyIntolerance} resource for the patient.
     */
    public AllergyIntolerance retrieveAllergyIntoleranceForPatient(String patientId);

}
