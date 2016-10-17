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

import ca.uhn.fhir.model.dstu2.resource.Bundle;
import junit.framework.Assert;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.nio.charset.Charset;
import java.util.List;

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
