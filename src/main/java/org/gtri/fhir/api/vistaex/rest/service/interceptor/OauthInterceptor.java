package org.gtri.fhir.api.vistaex.rest.service.interceptor;

import ca.uhn.fhir.rest.api.RestOperationTypeEnum;
import ca.uhn.fhir.rest.method.RequestDetails;
import ca.uhn.fhir.rest.server.exceptions.AuthenticationException;
import ca.uhn.fhir.rest.server.interceptor.InterceptorAdapter;
import org.gtri.fhir.api.vistaex.rest.service.security.OauthAuthorization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by es130 on 9/19/2016.
 */
public class OauthInterceptor extends InterceptorAdapter{

    /*========================================================================*/
    /* CONSTANTS */
    /*========================================================================*/
    private static final String CLIENT_ID = "client_id";
    private static final String CLIENT_SECRET = "client_secret";

    /*========================================================================*/
    /* PRIVATE VARIABLES */
    /*========================================================================*/
    private final Logger logger = LoggerFactory.getLogger(OauthInterceptor.class);
    private String enableOAuth;
    private String introspectUrl;
    private String clientId;
    private String clientSecret;
    private String localByPass;
    private String readOnly;

    /*========================================================================*/
    /* GETTERS AND SETTERS */
    /*========================================================================*/
    public String getEnableOAuth() {
        return enableOAuth;
    }

    public void setEnableOAuth(String enableOAuth) {
        this.enableOAuth = enableOAuth;
    }

    public String getIntrospectUrl() {
        return introspectUrl;
    }

    public void setIntrospectUrl(String introspectUrl) {
        this.introspectUrl = introspectUrl;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getLocalByPass() {
        return localByPass;
    }

    public void setLocalByPass(String localByPass) {
        this.localByPass = localByPass;
    }

    public String getReadOnly() {
        return readOnly;
    }

    public void setReadOnly(String readOnly) {
        this.readOnly = readOnly;
    }

    /*========================================================================*/
    /* PUBLIC METHODS */
    /*========================================================================*/
    @Override
    /**
     * Validate the bearer token and scope of the incoming request
     * @return true if the request has access, false otherwise.
     */
    public boolean incomingRequestPostProcessed(RequestDetails theRequestDetails, HttpServletRequest theRequest, HttpServletResponse theResponse) throws AuthenticationException {

        logger.debug("[OAuth] Request from " + theRequest.getRemoteAddr());

        if (readOnly.equalsIgnoreCase("True")) {
            if (theRequest.getMethod().equalsIgnoreCase("GET"))
                return true;
            else
                return false;
        }

        if (enableOAuth.equalsIgnoreCase("False")) {
            System.out.println("[OAuth] OAuth is disabled. Request from " + theRequest.getRemoteAddr() + "is approved");
            return true;
        }

        if (theRequestDetails.getRestOperationType() == RestOperationTypeEnum.METADATA) {
            System.out.println("This is METADATA request.");
            return true;
        }

        // Quick Hack for request from localhost overlay site.
        if (localByPass.equalsIgnoreCase("True")) {
            if (theRequest.getRemoteAddr().equalsIgnoreCase("127.0.0.1")
                    || theRequest.getRemoteAddr().equalsIgnoreCase("0:0:0:0:0:0:0:1")) {
                return true;
            }

            if (theRequest.getLocalAddr().equalsIgnoreCase(theRequest.getRemoteAddr())) {
                return true;
            }
        }

        //TODO: Client ID and Secret passed in? If so, get the client_id and client_secret
        String clientId = theRequest.getHeader(CLIENT_ID);
        String clientSecret = theRequest.getHeader(CLIENT_SECRET);

        // checking Auth
        logger.debug("IntrospectURL:" + getIntrospectUrl() + " clientID:" + getClientId() + " clientSecret:"
                + getClientSecret());
        OauthAuthorization myAuth = new OauthAuthorization(getIntrospectUrl(), getClientId(), getClientSecret());

        String err_msg = myAuth.introspectToken(theRequest);
        if (err_msg.isEmpty() == false) {
            throw new AuthenticationException(err_msg);
        }

        // Now we have a valid access token. Now, check Token type
        if (myAuth.checkBearer() == false) {
            throw new AuthenticationException("Not Token Bearer");
        }

        // Check scope.
        return myAuth.allowRequest(theRequestDetails);
    }
}
