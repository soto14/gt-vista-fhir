package ca.uhn.example.servlet;

import java.util.ArrayList;
import java.util.List;

import ca.uhn.example.provider.*;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.narrative.DefaultThymeleafNarrativeGenerator;
import ca.uhn.fhir.narrative.INarrativeGenerator;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.fhir.rest.server.interceptor.ResponseHighlighterInterceptor;
import org.gtri.fhir.api.vistaex.resource.api.VistaExResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;

/**
 * This servlet is the actual FHIR server itself
 */
public class ExampleRestfulServlet extends RestfulServer {

	private static final long serialVersionUID = 1L;

    private VistaExResource vistaExResource;

	/**
	 * Constructor
	 */
	public ExampleRestfulServlet() {
		super(FhirContext.forDstu2()); // Support DSTU2
        //Wire in the VistaExResource
        //I don't like doing this, but the Autowired annotation does not work, and this
        //method was the only way I could figure to get the VistaExResource Injected.
        WebApplicationContext parentAppCtx = ContextLoaderListener.getCurrentWebApplicationContext();
        vistaExResource = parentAppCtx.getBean(VistaExResource.class);
	}
	
	/**
	 * This method is called automatically when the
	 * servlet is initializing.
	 */
	@Override
	public void initialize() {
		/*
		 * Two resource providers are defined. Each one handles a specific
		 * type of resource.
		 */
		List<IResourceProvider> providers = new ArrayList<IResourceProvider>();
		providers.add(new GtVistaExApiPatientResourceProvider());
		providers.add(new GtVistaExApiObservationResourceProvider());
        providers.add(new GtVistaExApiConditionResourceProvider());
        providers.add(new GtVistaExApiMedicationOrderResourceProvider());
		setResourceProviders(providers);
		
		/*
		 * Use a narrative generator. This is a completely optional step, 
		 * but can be useful as it causes HAPI to generate narratives for
		 * resources which don't otherwise have one.
		 */
		INarrativeGenerator narrativeGen = new DefaultThymeleafNarrativeGenerator();
		getFhirContext().setNarrativeGenerator(narrativeGen);

		/*
		 * This server interceptor causes the server to return nicely
		 * formatter and coloured responses instead of plain JSON/XML if
		 * the request is coming from a browser window. It is optional,
		 * but can be nice for testing.
		 */
		registerInterceptor(new ResponseHighlighterInterceptor());
		
		/*
		 * Tells the server to return pretty-printed responses by default
		 */
		setDefaultPrettyPrint(true);

		//todo log in to Vista Ex API
        vistaExResource.loginToVistaEx();
	}

    @Override
    public void destroy() {
        super.destroy();
        //todo log out of Vista Ex API
        vistaExResource.logOutOfVistaEx();
    }
}
