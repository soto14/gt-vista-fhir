package org.gtri.fhir.api.vistaex.rest.service.provider;

import org.gtri.fhir.api.vistaex.resource.api.VistaExResource;
import org.gtri.fhir.api.vistaex.rest.service.util.VistaUtil;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;

/**
 * Created by es130 on 8/31/2016.
 */
public class GtVistaResourceProvider {

    public VistaExResource getVistaExResource() {
        return VistaUtil.getVistaExResource();
    }

}
