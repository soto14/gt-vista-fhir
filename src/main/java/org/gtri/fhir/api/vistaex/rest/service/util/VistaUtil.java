package org.gtri.fhir.api.vistaex.rest.service.util;

import org.gtri.fhir.api.vistaex.resource.api.VistaExResource;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by es130 on 9/1/2016.
 */
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
    public static File getFileInClassPath(String fileName) {
        //how to find resource in Servlet
        //http://stackoverflow.com/questions/2161054/where-to-place-and-how-to-read-configuration-resource-files-in-servlet-based-app/2161583#2161583
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        File propertiesFile = new File(classLoader.getResource(fileName).getFile());
        return propertiesFile;
    }

    public static Properties getProperties(){
        try {
            File propertiesFile = getFileInClassPath(PROPERTIES_FILE);
            FileInputStream fis = new FileInputStream(propertiesFile);
            Properties properties = new Properties();
            properties.load(fis);
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return getProperties();
    }
}
