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

import junit.framework.Assert;
import org.gtri.fhir.api.vistaex.rest.service.util.VistaUtil;
import org.junit.Test;

import java.util.Properties;

public class TestVistaUtil extends AbstractTest {

    private static String TEST_JSON = "src/test/resources/json/allergy-intolerance-with-bad-html-element-sample.json";

    @Test
    public void testGetProperties() throws Exception{
        Properties properties = VistaUtil.getProperties();
        Assert.assertNotNull(properties);
    }

    @Test
    public void testHtmlClean() throws Exception{
        String testCase1 = "foo bar <element> baz";
        String testCase2 = "foo bar <element/> baz";
        String testCase3 = "foo bar <element> baz </element>";

        String result1 = VistaUtil.closeOpenElements(testCase1);
        String result2 = VistaUtil.closeOpenElements(testCase2);
        String result3 = VistaUtil.closeOpenElements(testCase3);

        Assert.assertEquals( result1, "foo bar <element/> baz");
        Assert.assertEquals( result2, result2);
        Assert.assertEquals( result3, result3);
    }

    @Test
    public void testJsonClean() throws Exception{
        String jsonFileText = getFileTextContent(TEST_JSON);
        String newJson = VistaUtil.fixDivHtmlElements(jsonFileText);
        Assert.assertTrue( newJson.contains("<div>MILK <MILKS/></div>") );
    }

}
