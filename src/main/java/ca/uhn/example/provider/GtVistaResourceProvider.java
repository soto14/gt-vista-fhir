package ca.uhn.example.provider;

import org.gtri.fhir.api.vistaex.resource.api.VistaExResource;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;

/**
 * Created by es130 on 8/31/2016.
 */
public class GtVistaResourceProvider {
    //    @Autowired
    protected VistaExResource vistaExResource;

    public GtVistaResourceProvider(){
        initializeVistaExResource();
    }

    public VistaExResource getVistaExResource() {
        return vistaExResource;
    }

    private void initializeVistaExResource(){
        //I don't like doing this, but the Autowired annotation does not work, and this
        //method was the only way I could figure to get the VistaExResource Injected.
        WebApplicationContext parentAppCtx = ContextLoaderListener.getCurrentWebApplicationContext();
        vistaExResource = parentAppCtx.getBean(VistaExResource.class);
    }
}
