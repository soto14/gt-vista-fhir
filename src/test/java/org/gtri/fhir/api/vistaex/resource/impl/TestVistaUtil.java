package org.gtri.fhir.api.vistaex.resource.impl;

import junit.framework.Assert;
import org.gtri.fhir.api.vistaex.resource.api.VistaExResource;
import org.gtri.fhir.api.vistaex.rest.service.util.VistaUtil;
import org.junit.Test;

import java.io.InputStream;
import java.util.Properties;

/**
 * Created by es130 on 10/14/2016.
 */
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
