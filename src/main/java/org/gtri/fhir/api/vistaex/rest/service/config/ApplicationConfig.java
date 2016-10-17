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
