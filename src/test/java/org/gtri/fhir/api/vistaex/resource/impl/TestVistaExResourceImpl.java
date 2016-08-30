package org.gtri.fhir.api.vistaex.resource.impl;

import ca.uhn.fhir.model.dstu2.composite.HumanNameDt;
import ca.uhn.fhir.model.dstu2.resource.Bundle;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import junit.framework.Assert;
import org.gtri.fhir.api.vistaex.resource.api.VistaExResource;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

/**
 * Created by es130 on 8/30/2016.
 */
public class TestVistaExResourceImpl {
    private static final String PATIENT_ID = "9E7A%3B3"; //maps to 9E7A;3
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

        //query for condition bundle
        Bundle conditionBundle = vistaExResource.retrieveConditionForPatient(PATIENT_ID);
        Assert.assertNotNull(conditionBundle);
        Assert.assertEquals(conditionBundle.getTotal(), Integer.valueOf(12) );
        entryList = conditionBundle.getEntry();
        for(Bundle.Entry entry : entryList){
            Assert.assertEquals(entry.getResource().getResourceName(), "Condition" );
        }

        //query for observation bundle
        Bundle observationBundle = vistaExResource.retrieveObservationForPatient(PATIENT_ID);
        Assert.assertNotNull(observationBundle);
        Assert.assertFalse(observationBundle.isEmpty());
        entryList = observationBundle.getEntry();
        Assert.assertTrue(entryList.size()> 0);
        for(Bundle.Entry entry : entryList){
            Assert.assertEquals(entry.getResource().getResourceName(), "Observation");
        }

        //query for medication order bundle
        Bundle medicationOrderBundle = vistaExResource.retrieveMedicationOrderForPatient(PATIENT_ID);
        Assert.assertNotNull(medicationOrderBundle);
        Assert.assertFalse(medicationOrderBundle.isEmpty());
        entryList = medicationOrderBundle.getEntry();
        Assert.assertEquals(entryList.size(), 48);
        for(Bundle.Entry entry : entryList){
            Assert.assertNotNull(entry.getResource());
            Assert.assertEquals(entry.getResource().getResourceName(), "MedicationOrder");
        }

        //log out
        success = vistaExResource.logOutOfVistaEx();
        Assert.assertTrue(success);
    }

}
