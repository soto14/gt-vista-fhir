package org.gtri.fhir.api.vistaex.rest.service.conformance;

import ca.uhn.fhir.model.api.ExtensionDt;
import ca.uhn.fhir.model.dstu2.resource.Conformance;
import ca.uhn.fhir.model.dstu2.valueset.RestfulSecurityServiceEnum;
import ca.uhn.fhir.model.primitive.UriDt;
import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.fhir.rest.server.provider.dstu2.ServerConformanceProvider;
import org.gtri.fhir.api.vistaex.rest.service.util.VistaUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Properties;

/**
 * Created by es130 on 9/16/2016.
 */
public class GtVistaExConformanceProvider extends ServerConformanceProvider {

    private static final String OAUTH_URL_BASE = "oauthUrlBase";
    private static final String SMART_ON_FHIR_URL= "smartOnFhirUrl";
    private static final String TOKEN = "token";
    private static final String AUTHORIZE = "authorize";
    private static final String REGISTER = "register";

    private Properties properties;
    private String oauthUrlBase;
    private String smartOnFhirUrl;
    private String authorizeURI;
    private String tokenURI;
    private String registerURI;

    private final Logger logger = LoggerFactory.getLogger(GtVistaExConformanceProvider.class);

    public GtVistaExConformanceProvider(RestfulServer theRestfulServer) {
        super(theRestfulServer);
        properties = VistaUtil.getProperties();
        oauthUrlBase = properties.getProperty(OAUTH_URL_BASE);
        smartOnFhirUrl = properties.getProperty(SMART_ON_FHIR_URL);

        authorizeURI = oauthUrlBase + AUTHORIZE;
        tokenURI = oauthUrlBase + TOKEN;
        registerURI = oauthUrlBase + REGISTER;
        setCache(false);
    }

    @Override
    public Conformance getServerConformance(HttpServletRequest theRequest) {
        logger.debug("Generating Conformance Statement");
        //start with the default generated conformance statement
        Conformance conformanceStmt = super.getServerConformance(theRequest);

        Conformance.RestSecurity restSec = new Conformance.RestSecurity();

        //Set security.service
        restSec.setService(RestfulSecurityServiceEnum.SMART_ON_FHIR);

        logger.debug("SmartOnFhir URL {}", smartOnFhirUrl);
        logger.debug("Authorize URL {}", authorizeURI);
        logger.debug("Token URL {}", tokenURI);
        logger.debug("Register URL {}", registerURI);

        //now add in the security information
        ExtensionDt authorizeExtension = new ExtensionDt(false, AUTHORIZE, new UriDt(authorizeURI));
        ExtensionDt tokenExtension = new ExtensionDt(false, TOKEN, new UriDt(tokenURI));
        ExtensionDt registerExtension = new ExtensionDt(false, REGISTER, new UriDt(registerURI));

        ExtensionDt securityExtension = new ExtensionDt();
        securityExtension.setUrl(oauthUrlBase);
        securityExtension.addUndeclaredExtension(authorizeExtension);
        securityExtension.addUndeclaredExtension(tokenExtension);
        securityExtension.addUndeclaredExtension(registerExtension);

        restSec.addUndeclaredExtension(securityExtension);

        List<Conformance.Rest> rests = conformanceStmt.getRest();

        if (rests == null || rests.size() <= 0) {
            Conformance.Rest rest = new Conformance.Rest();
            rest.setSecurity(restSec);
            conformanceStmt.addRest(rest);
        } else {
            Conformance.Rest rest = rests.get(0);
            rest.setSecurity(restSec);
        }
        logger.debug("Finished Generating Conformance Statement");
        return conformanceStmt;
    }
}
