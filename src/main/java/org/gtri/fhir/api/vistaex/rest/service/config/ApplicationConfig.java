package org.gtri.fhir.api.vistaex.rest.service.config;

import org.gtri.fhir.api.vistaex.resource.api.VistaExResource;
import org.gtri.fhir.api.vistaex.resource.impl.VistaExResourceImpl;
import org.gtri.fhir.api.vistaex.rest.service.interceptor.OauthInterceptor;
import org.gtri.fhir.api.vistaex.rest.service.util.VistaUtil;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

//import ca.uhn.fhir.context.FhirVersionEnum;
//import ca.uhn.fhir.to.FhirTesterMvcConfig;
//import ca.uhn.fhir.to.TesterConfig;


//@formatter:off
/**
 * This spring config file configures the web testing module. It serves two
 * purposes:
 * 1. It imports FhirTesterMvcConfig, which is the spring config for the
 *    tester itself
 * 2. It tells the tester which server(s) to talk to, via the testerConfig()
 *    method below
 */

@Configuration
@ComponentScan({"org.gtri.fhir.api.vistaex.resource.impl.*", "org.gtri.fhir.api.vistaex.rest.service.provider.*"})
public class ApplicationConfig {

    private static final String INTROSPECT_URL = "introspectUrl";
    private static final String INTROSPECT_CLIENT_ID = "introspectClientId";
    private static final String INTROSPECT_CLIENT_SECRET = "introspectClientSecret";
    private static final String INTROSPECT_ENABLE_OAUTH="introspectEnableOAuth";
    private static final String INTROSPECT_LOCAL_BY_PASS="introspectLocalByPass";
    private static final String INTROSPECT_READ_ONLY="introspectReadOnly";

	/**
	 * This bean tells the testing webpage which servers it should configure itself
	 * to communicate with. In this example we configure it to talk to the local
	 * server, as well as one public server. If you are creating a project to 
	 * deploy somewhere else, you might choose to only put your own server's 
	 * address here.
	 * 
	 * Note the use of the ${serverBase} variable below. This will be replaced with
	 * the base URL as reported by the server itself. Often for a simple Tomcat
	 * (or other container) installation, this will end up being something
	 * like "http://localhost:8080/hapi-fhir-jpaserver-example". If you are
	 * deploying your server to a place with a fully qualified domain name, 
	 * you might want to use that instead of using the variable.
	 */
//	@Bean
//	public TesterConfig testerConfig() {
//		TesterConfig retVal = new TesterConfig();
//		retVal
//			.addServer()
//				.withId("home")
//				.withFhirVersion(FhirVersionEnum.DSTU2)
//				.withBaseUrl("${serverBase}/fhir")
//				.withName("Local Tester")
//			.addServer()
//				.withId("hapi")
//				.withFhirVersion(FhirVersionEnum.DSTU2)
//				.withBaseUrl("http://fhirtest.uhn.ca/baseDstu2")
//				.withName("Public HAPI Test Server");
//
//		/*
//		 * Use the method below to supply a client "factory" which can be used
//		 * if your server requires authentication
//		 */
//		// retVal.setClientFactory(clientFactory);
//
//		return retVal;
//	}

	@Bean
	@Scope(value="singleton")
	public VistaExResource vistaExResource(){
		return new VistaExResourceImpl();
	}

	@Bean
	public OauthInterceptor oauthInterceptor(){
        Properties properties = VistaUtil.getProperties();
        OauthInterceptor interceptor = new OauthInterceptor();
        interceptor.setIntrospectUrl(properties.getProperty(INTROSPECT_URL));
        interceptor.setClientId(properties.getProperty(INTROSPECT_CLIENT_ID));
        interceptor.setClientSecret(properties.getProperty(INTROSPECT_CLIENT_SECRET));
        interceptor.setEnableOAuth(properties.getProperty(INTROSPECT_ENABLE_OAUTH));
        interceptor.setLocalByPass(properties.getProperty(INTROSPECT_LOCAL_BY_PASS));
        interceptor.setReadOnly(properties.getProperty(INTROSPECT_READ_ONLY));
        return interceptor;
    }
}
//@formatter:on
