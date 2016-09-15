package org.gtri.fhir.api.vistaex.resource.impl;

import ca.uhn.fhir.model.dstu2.composite.HumanNameDt;
import ca.uhn.fhir.model.dstu2.resource.Bundle;
import ca.uhn.fhir.model.dstu2.resource.Encounter;
import ca.uhn.fhir.model.dstu2.resource.Patient;
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
        Bundle medicationPrescriptionBundle = translator.translateMedicationOrderForPatient(jsonFileText);
        validateBundle(medicationPrescriptionBundle, 48, "MedicationOrder");
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
        Bundle medicationAdminBundle = translator.translateMedicationAdministrationForPatient(jsonFileText);
        validateBundle(medicationAdminBundle, 8, "MedicationAdministration");
    }
}