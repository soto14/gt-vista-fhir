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

package org.gtri.fhir.api.vistaex.resource.api;

import ca.uhn.fhir.model.dstu2.resource.*;

import java.util.List;

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
