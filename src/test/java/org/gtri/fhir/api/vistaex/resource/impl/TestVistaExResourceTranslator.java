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

import ca.uhn.fhir.model.dstu2.composite.HumanNameDt;
import ca.uhn.fhir.model.dstu2.resource.*;
import junit.framework.Assert;
import org.gtri.fhir.api.vistaex.resource.api.VistaExResourceTranslator;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class TestVistaExResourceTranslator extends AbstractTest{

    private VistaExResourceTranslator translator;

    @Before
    public void createImpl(){
        translator = new VistaExResourceTranslatorImpl();
    }

    @Test
    public void testTranslatePerson() throws Exception{
        String jsonFileText = getFileTextContent("src/test/resources/json/patient-fhirish-sample.json");
        Patient patient = translator.translatePatient( jsonFileText );
        List<HumanNameDt> names = patient.getName();
        Assert.assertEquals( names.size(), 1);
        Assert.assertTrue( names.get(0).getText().equals("EIGHT,PATIENT"));
    }

    @Test
    public void testTranslateConditionBundle() throws Exception{
        String jsonFileText = getFileTextContent("src/test/resources/json/condition-fhirish-sample.json");
        Bundle conditionBundle = translator.translateConditionBundleForPatient(jsonFileText);
        validateBundle(conditionBundle, 12, "Condition");
    }

    @Test
    public void testTranslateObservation() throws Exception{
        String jsonFileText = getFileTextContent("src/test/resources/json/observation-fhrish-sample.json");
        Bundle observationBundle = translator.translateObservationForPatient(jsonFileText);
        validateBundle(observationBundle, 1748, "Observation");
    }

    @Test
    public void testTranslateMedicationPrescription() throws Exception{
        String jsonFileText = getFileTextContent("src/test/resources/json/medication-prescription-fhirish-sample-new.json");
        List<MedicationOrder> medicationPrescriptionBundle = translator.translateMedicationOrderForPatient(jsonFileText);
        Assert.assertEquals(medicationPrescriptionBundle.size(), 48);
//        validateBundle(medicationPrescriptionBundle, 48, "MedicationOrder");
    }

    @Test
    public void testTranslateProcedure() throws Exception{
        String jsonFileText = getFileTextContent("src/test/resources/json/procedure-fhirish-sample.json");
        Bundle procedureBundle = translator.translateProcedureForPatient(jsonFileText);
        validateBundle(procedureBundle, 2, "Procedure");
    }

    @Test
    public void testTranslateAllergyIntolerance() throws Exception{
        String jsonFileText = getFileTextContent("src/test/resources/json/allergy-intolerance-fhirish-sample.json");
        Bundle allergyBundle = translator.translateAllergyIntoleranceForPatient(jsonFileText);
        validateBundle(allergyBundle, 8, "AllergyIntolerance");
    }

    @Test
    public void testTranslateVisit() throws Exception {
        String jsonFileText = getFileTextContent("src/test/resources/json/visit-sample.json");
        List<Encounter> encounters = translator.translateEncounterforPatient(jsonFileText);
        Assert.assertEquals(encounters.size(), 254);
    }

    @Test
    public void testTranslateMedicationAdministration() throws Exception{
        String jsonFileText = getFileTextContent("src/test/resources/json/medication-administration-fhirish-sample.json");
        List<MedicationAdministration> medicationAdminBundle = translator.translateMedicationAdministrationForPatient(jsonFileText);
        Assert.assertEquals(medicationAdminBundle.size(), 8);
    }
}