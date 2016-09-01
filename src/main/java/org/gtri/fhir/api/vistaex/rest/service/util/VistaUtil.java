package org.gtri.fhir.api.vistaex.rest.service.util;

import org.gtri.fhir.api.vistaex.resource.api.VistaExResource;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;

/**
 * Created by es130 on 9/1/2016.
 */
public class VistaUtil {

    /**
     * Returns an implementation of the VistaExResource interface.
     * @return
     */
    public static VistaExResource getVistaExResource(){
        WebApplicationContext parentAppCtx = ContextLoaderListener.getCurrentWebApplicationContext();
        return parentAppCtx.getBean(VistaExResource.class);
    }

}
