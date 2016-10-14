package org.gtri.fhir.api.vistaex.rest.service.config;

import org.gtri.fhir.api.vistaex.resource.api.VistaExResource;
import org.gtri.fhir.api.vistaex.resource.impl.VistaExResourceImpl;
import org.gtri.fhir.api.vistaex.rest.service.util.VistaUtil;
import org.springframework.context.annotation.*;

/**
 * This spring config file configures the web application
 */

@Configuration
@ComponentScan({"org.gtri.fhir.api.vistaex.resource.impl.*", "org.gtri.fhir.api.vistaex.rest.service.provider.*"})
public class ApplicationConfig {

	@Bean
	@Scope(value="singleton")
	public VistaExResource vistaExResource(){
		return new VistaExResourceImpl();
	}

}
//@formatter:on
