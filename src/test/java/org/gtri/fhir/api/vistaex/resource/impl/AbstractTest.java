package org.gtri.fhir.api.vistaex.resource.impl;

import ca.uhn.fhir.model.dstu2.resource.Bundle;
import junit.framework.Assert;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.nio.charset.Charset;
import java.util.List;

/**
 * Created by es130 on 9/15/2016.
 */
public class AbstractTest {

    /**
     * Loads and file and returns its string content.
     * @param filePath the path to the file to process
     * @return the string content of the file
     * @throws Exception
     */
    public String getFileTextContent(String filePath) throws Exception{
        File patientJsonFile = new File(filePath);
        return FileUtils.readFileToString(patientJsonFile, Charset.forName("UTF-8"));
    }

    /**
     * Validates a bundle
     * @param bundle
     * @param numEntries
     * @param resourceName
     */
    public void validateBundle(Bundle bundle, int numEntries, String resourceName){
        Assert.assertNotNull(bundle);
        Assert.assertFalse(bundle.isEmpty());
        List<Bundle.Entry> entryList = bundle.getEntry();
        Assert.assertEquals(entryList.size(), numEntries);
        for(Bundle.Entry entry : entryList){
            Assert.assertNotNull(entry.getResource());
            Assert.assertEquals(entry.getResource().getResourceName(), resourceName);
        }
    }
}
