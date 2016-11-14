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
import ca.uhn.fhir.model.dstu2.resource.Bundle;
import ca.uhn.fhir.model.dstu2.resource.Encounter;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import junit.framework.Assert;
import org.gtri.fhir.api.vistaex.resource.api.VistaExResource;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class TestVistaExResourceImpl extends AbstractTest{
    //The SITE ID is appended by the impl methods in VistaExResourceImpl, there is no need to append it to the
    //PATIENT_ID anymore. However, if at some point that does change, teh previous PATIENT_ID value was left
    //here for an example.
    private static final String PATIENT_ID = "3";//"9E7A%3B3"; //maps to 9E7A;3
    private VistaExResource vistaExResource;

    @Before
    public void createInstance(){
        vistaExResource = new VistaExResourceImpl();
    }

    @Test
    public void testServiceInteraction() throws Exception{
        // list to store bundles
        List<Bundle.Entry> entryList;

        //log in
        boolean success = vistaExResource.loginToVistaEx();
        Assert.assertTrue( success );

        //query for a person
        Patient patient = vistaExResource.retrievePatient(PATIENT_ID);
        Assert.assertNotNull(patient);
        List<HumanNameDt> names = patient.getName();
        Assert.assertEquals( names.size(), 1);
        Assert.assertTrue( names.get(0).getText().equals("EIGHT,PATIENT"));

        //query for allergy-intolerance
        Bundle allergyBundle = vistaExResource.retrieveAllergyIntoleranceForPatient(PATIENT_ID);
        validateBundle(allergyBundle, 8, "AllergyIntolerance"); // was 9

        //query for condition bundle
        Bundle conditionBundle = vistaExResource.retrieveConditionForPatient(PATIENT_ID);
        validateBundle(conditionBundle, 12, "Condition"); //was 14

        List<Encounter> encounters = vistaExResource.retrieveEncountersForPatient(PATIENT_ID);
        Assert.assertEquals(encounters.size(), 254);

        Bundle medicationAdmin = vistaExResource.retrieveMedicationAdministrationForPatient(PATIENT_ID);
        validateBundle(medicationAdmin, 8, "MedicationAdministration");

        Bundle medicationOrder = vistaExResource.retrieveMedicationOrderForPatient(PATIENT_ID);
        validateBundle(medicationOrder, 48, "MedicationOrder");

        //query for observation bundle
        Bundle observationBundle = vistaExResource.retrieveObservationForPatient(PATIENT_ID);
        validateBundle( observationBundle, 1748, "Observation"); //was 1749

        //query for medication order bundle
        Bundle procedureBundle = vistaExResource.retrieveProcedureForPatient(PATIENT_ID);
        validateBundle(procedureBundle, 2, "Procedure");

        //log out
        success = vistaExResource.logOutOfVistaEx();
        Assert.assertTrue(success);
    }
}
