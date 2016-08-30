package org.gtri.fhir.api.vistaex.resource.impl;

import ca.uhn.fhir.model.dstu2.composite.HumanNameDt;
import ca.uhn.fhir.model.dstu2.resource.Bundle;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import junit.framework.Assert;
import org.gtri.fhir.api.vistaex.resource.api.VistaExResourceTranslator;
import org.junit.Before;
import org.junit.Test;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.nio.charset.Charset;
import java.util.List;

public class TestVistaExResourceTranslator {

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
        Assert.assertFalse(conditionBundle.isEmpty());
        Assert.assertEquals(conditionBundle.getTotal(), Integer.valueOf(12) );
        List<Bundle.Entry> entryList = conditionBundle.getEntry();
        for(Bundle.Entry entry : entryList){
            Assert.assertEquals(entry.getResource().getResourceName(), "Condition" );
        }
    }

    public String getFileTextContent(String filePath) throws Exception{
        File patientJsonFile = new File(filePath);
        return FileUtils.readFileToString(patientJsonFile, Charset.forName("UTF-8"));
    }
}