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

package org.gtri.fhir.api.vistaex.rest.service.util;

import org.gtri.fhir.api.vistaex.resource.api.VistaExResource;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;

import java.io.*;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VistaUtil {

    private static final String PROPERTIES_FILE = "gtvistaex.properties";

    /**
     * Returns an implementation of the VistaExResource interface.
     * @return
     */
    public static VistaExResource getVistaExResource(){
        WebApplicationContext parentAppCtx = ContextLoaderListener.getCurrentWebApplicationContext();
        return parentAppCtx.getBean(VistaExResource.class);
    }

    /**
     * Finds a file on the classpath
     * @param fileName the name of the file to find
     * @return the file.
     */
    public static InputStream getFileInputStreamFromClassPath(String fileName) {
        //how to find resource in Servlet
        //http://stackoverflow.com/questions/2161054/where-to-place-and-how-to-read-configuration-resource-files-in-servlet-based-app/2161583#2161583
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(fileName);
        return inputStream;
    }

    public static Properties getProperties(){
        Properties properties = null;
        try {
            InputStream fis = getFileInputStreamFromClassPath(PROPERTIES_FILE);
            properties = new Properties();
            properties.load(fis);
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
            properties = null;
        } catch (IOException ioe) {
            ioe.printStackTrace();
            properties = null;
        }
        return properties;
    }

    /**
     * Takes a DSTU2 JSON String that contains a text -> status -> div and cleans up the div so that
     * any HTML element it contains is closed.
     * @param jsonString
     * @return the new JSON string with closed elements.
     */
    public static String fixDivHtmlElements(String jsonString){
        //regex to find the text -> status -> div element in a FHIR JSON Object.
//        String regex = "\"text\":\\s*\\{\\s*\"status\":\\s*\"\\w*\",\\s*\"div\":\\s*\"<div>(.*)</div>\"";
        String regex = "\"text\":\\s*\\{\\s*\"status\":\\s*\"\\w*\",\\s*\"div\":\\s*\"<div>([\\w\\s<>/]*)</div>\"";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(jsonString);
        String divText;
        String newText;
        String resultString = jsonString;
        while( m.find() ){
            //the first group contains the div text
            divText = m.group(1);
            newText = closeOpenElements(divText);
            resultString = resultString.replace(divText, newText);
        }
        return resultString;
    }

    /**
     * Takes an HTML string and closes any non closed elements.
     * @param htmlString the string to process.
     * @return the new string with closed elements.
     */
    public static String closeOpenElements(String htmlString){
        //regexp to check if string has HTML tag (<)(\w+)(>).*(?=</\2>)
        //use a regex with negative lookahead to find all HTML tags that are not closed
        //See the following table for how this regex works
        // Div content                 Matched Value (returned by call to find()
        //------------                ------------------------------------------
        // foo bar <element> baz      <element>
        // foo bar <element/> baz     no match, the element is closed
        // foo <element></element>    no match, the element is closed
        // foo <element></element2>   no match, cannot detect that close element is different from open element.

        //the regex below only matches the first example, as that is the one that is most common to occur.
        //The regex breaks down into the following groups
        // 0: the entire match
        // 1: the open arrow "<"
        // 2: the element name, value between "<" and ">"
        // 3: the close arrow ">"

        //What we want to do here is find "<element>" and turn it into "<element/>"
        return htmlString.replaceAll("(<)(\\w+)(>)(?!.*</\\2>|</\\w*>)", "$1$2/$3");
    }


}
