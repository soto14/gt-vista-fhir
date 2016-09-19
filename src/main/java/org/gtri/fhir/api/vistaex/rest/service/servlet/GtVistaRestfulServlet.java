package org.gtri.fhir.api.vistaex.rest.service.servlet;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.narrative.DefaultThymeleafNarrativeGenerator;
import ca.uhn.fhir.narrative.INarrativeGenerator;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.fhir.rest.server.interceptor.ResponseHighlighterInterceptor;
import org.gtri.fhir.api.vistaex.rest.service.conformance.GtVistaExConformanceProvider;
import org.gtri.fhir.api.vistaex.rest.service.interceptor.OauthInterceptor;
import org.gtri.fhir.api.vistaex.rest.service.provider.*;
import org.gtri.fhir.api.vistaex.rest.service.util.VistaUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;

/**
 * This servlet is the actual FHIR server itself
 */
public class GtVistaRestfulServlet extends RestfulServer {

    private static final long SESSION_REFRESH_INTERVAL_MILLI = 300000;
	private static final long serialVersionUID = 1L;
    private final static Logger logger = LoggerFactory.getLogger(GtVistaRestfulServlet.class);

    private Timer refreshTimer;
    private SessionRefreshTimer sessionRefreshTimerTask;

	/**
	 * Constructor
	 */
	public GtVistaRestfulServlet() {
		super(FhirContext.forDstu2()); // Support DSTU2
	}
	
	/**
	 * This method is called automatically when the
	 * servlet is initializing.
	 */
	@Override
	public void initialize() {

		//Get a handle on the Web Application Context
		WebApplicationContext applicationContext = ContextLoaderListener.getCurrentWebApplicationContext();

		/*
		 * Two resource providers are defined. Each one handles a specific
		 * type of resource.
		 */
		List<IResourceProvider> providers = new ArrayList<IResourceProvider>();
		providers.add(new GtVistaExApiPatientResourceProvider());
		providers.add(new GtVistaExApiObservationResourceProvider());
        providers.add(new GtVistaExApiConditionResourceProvider());
        providers.add(new GtVistaExApiMedicationOrderResourceProvider());
        providers.add(new GtVistaExApiEncounterResourceProvider());
		setResourceProviders(providers);

		/*
		 * Add custom conformance provider
		 *
		 */
        GtVistaExConformanceProvider conformanceProvider = new GtVistaExConformanceProvider(this);
        //stuff added by Myungs Conformance provider, Do we need it?
//        confProvider.setImplementationDescription("FHIR JPA Server");
//        confProvider.setPublisher("Georgia Tech - I3L");
//        confProvider.setAuthServerUrl(getServletConfig().getInitParameter("authServerUrl"));
        setServerConformanceProvider(conformanceProvider);

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

		//set OAuthInterceptor
		OauthInterceptor oauthInterceptor = applicationContext.getBean(OauthInterceptor.class);
		this.registerInterceptor(oauthInterceptor);

		/*
		 * Tells the server to return pretty-printed responses by default
		 */
		setDefaultPrettyPrint(true);

        //log in to Vista Ex API
        VistaUtil.getVistaExResource().loginToVistaEx();

        //start the refresh thread
        sessionRefreshTimerTask = new SessionRefreshTimer();
        refreshTimer = new Timer();
        refreshTimer.schedule(sessionRefreshTimerTask, SESSION_REFRESH_INTERVAL_MILLI, SESSION_REFRESH_INTERVAL_MILLI);

	}

    @Override
    public void destroy() {
        super.destroy();
        //log out of Vista Ex API
        VistaUtil.getVistaExResource().logOutOfVistaEx();
    }

}
