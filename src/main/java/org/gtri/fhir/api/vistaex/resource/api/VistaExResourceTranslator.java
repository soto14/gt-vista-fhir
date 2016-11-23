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
import ca.uhn.fhir.model.dstu2.resource.AllergyIntolerance;
import ca.uhn.fhir.model.dstu2.resource.Condition;
import ca.uhn.fhir.model.dstu2.resource.MedicationAdministration;
import ca.uhn.fhir.model.dstu2.resource.Observation;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.model.dstu2.resource.Procedure;

import java.util.List;

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
    public List<MedicationAdministration> translateMedicationAdministrationForPatient(String medicationAdministrationJson);

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
