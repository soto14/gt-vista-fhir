package org.gtri.fhir.api.vistaex.rest.service.security;

import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import org.apache.commons.codec.binary.Base64;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.types.ParameterStyle;
import org.apache.oltu.oauth2.rs.request.OAuthAccessResourceRequest;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

//import ca.uhn.fhir.model.dstu.valueset.RestfulOperationTypeEnum;
import ca.uhn.fhir.rest.api.RestOperationTypeEnum;
import ca.uhn.fhir.rest.method.RequestDetails;

/**
 * Based off of Authorization object developed by Myung Choi for the GT-FHIR project, https://github.com/i3l/GT-FHIR
 */
public class OauthAuthorization {

    /*========================================================================*/
    /* CONSTANTS */
    /*========================================================================*/
    private static final String OAUTH_REQEST_SUBJECT = "sub";
    private static final String OAUTH_REQEST_CLIENT_ID= "client_id";
    private static final String OAUTH_REQEST_TOKEN_TYPE= "token_type";
    private static final String OAUTH_REQEST_SCOPE = "scope";
    private static final String OAUTH_REQEST_TOKEN_EXPIRATION = "exp";
    private static final String ACTIVE= "active";
    private static final String BEARER_TOKEN_NAME = "Bearer";
    private static int TIME_SCEW_ALLOWANCE = 300;

    /*========================================================================*/
    /* PRIVATE VARIABLES */
    /*========================================================================*/
    private final Logger logger = LoggerFactory.getLogger(OauthAuthorization.class);
    private String url;
    private String clientId;
    private String clientSecret;
    private String userId;
    private String token_type;
    private boolean is_admin = false;
    private Set<String> scopeSet;
    private DateFormat df;

    /*========================================================================*/
    /* CONSTRUCTORS */
    /*========================================================================*/
    public OauthAuthorization(String url) {
        this.url = url;
        this.clientId = "client";
        this.clientSecret = "secret";
    }

    public OauthAuthorization(String url, String clientId, String clientSecret) {
        this.url = url;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    /*========================================================================*/
    /* GETTERS */
    /*========================================================================*/
    public String getClientId() {
        return clientId;
    }

    public String getUserId() {
        return userId;
    }
    public DateFormat getDateFormat(){
        if(df==null){
            df = df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        }
        return df;
    }

    /*========================================================================*/
    /* PUBLIC METHODS */
    /*========================================================================*/
    public String introspectToken(HttpServletRequest request) {
        OAuthAccessResourceRequest oauthRequest;
        try {
            oauthRequest = new OAuthAccessResourceRequest(request, ParameterStyle.HEADER);

            // Get the access token
            String accessToken = oauthRequest.getAccessToken();
            if (introspectToken(accessToken) == false) {
                return "Invalid Access Token";
            }

        } catch (OAuthSystemException | OAuthProblemException e) {
            e.printStackTrace();
            return "Invalid Auth Request";
        }

        return "";
    }

    public boolean introspectToken(String token) {
        // Sanity Check.
        if (token == null || token.isEmpty()) {
            return false;
        }

        // Introspect the token
        ResponseEntity<String> response = performInterospect(token);
        //check the response
        HttpStatus statusCode = response.getStatusCode();
        if (statusCode.is2xxSuccessful() == false) {
            return false;
        }
        String responseContent = response.getBody();
        logger.debug("IntrospectToken: " + responseContent);

        // First check the token status. Turn the body into JSON.
        JSONObject jsonObject = new JSONObject(responseContent);
        if (jsonObject.getBoolean(ACTIVE) != true) {
            // This is not active token.
            return false;
        }

        // Get the expiration time.
        boolean expired = checkExpirationDate(jsonObject);
        if( expired ){
            return false;
        }

        // Store the received information such as scope, user_id, client_id, etc...
        userId = jsonObject.getString(OAUTH_REQEST_SUBJECT);
        clientId = jsonObject.getString(OAUTH_REQEST_CLIENT_ID);
        token_type = jsonObject.getString(OAUTH_REQEST_TOKEN_TYPE);

        String[] scopeValues = jsonObject.getString(OAUTH_REQEST_SCOPE)
                .trim().replaceAll("\\+", " ")
                .split(" ");
        scopeSet = new HashSet<String>(Arrays.asList(scopeValues));
        if (scopeSet.isEmpty()) return false;

        if (scopeSet.contains("user/*.*")) {
            is_admin = true;
        }

        return true;
    }

    public boolean checkBearer() {
        if (token_type != null && token_type.equalsIgnoreCase(BEARER_TOKEN_NAME)) {
            return true;
        } else {
            return false;
        }
    }

    public boolean allowRequest(RequestDetails theRequestDetails) {
        if (checkBearer() == false) {
            return false;
        }

        if (is_admin) {
            return true;
        }

        return hasResourceScope(theRequestDetails);
    }

    /*========================================================================*/
    /* PRIVATE METHODS */
    /*========================================================================*/
    private HttpHeaders createHeaders () {
        HttpHeaders httpHeaders = new HttpHeaders();
        String auth = clientId+":"+clientSecret;
        byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(Charset.forName("US-ASCII")));
        String authHeader = "Basic " + new String(encodedAuth);
        httpHeaders.set("Authorization", authHeader);
        httpHeaders.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

        return httpHeaders;
    }

    /**
     * Checks the expiration date of the JSONObject representing the response from the introspect call
     * @param jsonObject the JSON object to search
     * @return true if the token is expired, false otherwise
     */
    private boolean checkExpirationDate(JSONObject jsonObject){
        Date expDate;
        boolean isExpired = false;
        try {
            expDate = getDateFormat().parse(jsonObject.getString(OAUTH_REQEST_TOKEN_EXPIRATION));
        } catch (JSONException | ParseException e) {
            e.printStackTrace();
            expDate = null;
            isExpired = true;
        }

        Date minAllowableExpirationTime = new Date(System.currentTimeMillis()-(TIME_SCEW_ALLOWANCE * 1000L));
        if (expDate != null && expDate.before(minAllowableExpirationTime)) {
            isExpired = true;
        }
        return isExpired;
    }

    /**
     * Checks the scope of the incoming RequestDetails object to see if it has access
     * @param theRequestDetails the RequestDetails to check
     * @return true is it has access, false otherwise.
     */
    private boolean hasResourceScope(RequestDetails theRequestDetails){
        boolean scopeValid = false;
        // TODO: Check the request detail and compare with scope. If out of scope, then
        //       return false.
        // We need to have user or patient level permission checking. For now, user/ and patient/ scope has
        // all patients permission.  We need to revisit this.
        //
        String resourceName = theRequestDetails.getResourceName();
        RestOperationTypeEnum resourceOperationType = theRequestDetails.getRestOperationType();
        for (String scope : scopeSet) {
            String[] scopeDetail = scope.split("/");
            if (resourceOperationType ==  RestOperationTypeEnum.READ ||
                    resourceOperationType == RestOperationTypeEnum.VREAD ||
                    resourceOperationType == RestOperationTypeEnum.SEARCH_TYPE) {
                if ((scopeDetail[1].equalsIgnoreCase("*.read") || scopeDetail[1].equalsIgnoreCase("*.*"))) {
                    scopeValid = true;
                    break;
                } else {
                    String[] scopeResource = scopeDetail[1].split(".");
                    if (scopeResource[0].equalsIgnoreCase(resourceName) &&
                            (scopeResource[1].equalsIgnoreCase("read") || scopeResource[1].equalsIgnoreCase("*"))) {
                        scopeValid = true;
                        break;
                    }
                }
            }
            else {
                // This is CREATE, UPDATE, DELETE... write permission is required.
                if ((scopeDetail[1].equalsIgnoreCase("*.write") || scopeDetail[1].equalsIgnoreCase("*.*"))) {
                    scopeValid = true;
                    break;
                } else {
                    String[] scopeResource = scopeDetail[1].split(".");
                    if (scopeResource[0].equalsIgnoreCase(resourceName) &&
                            (scopeResource[1].equalsIgnoreCase("write") || scopeResource[1].equalsIgnoreCase("*"))) {
                        scopeValid = true;
                        break;
                    }
                }
            }
        }

        logger.debug(theRequestDetails.getCompartmentName()+" "+resourceOperationType.name()+" request has Authorization: " + scopeValid);
        return scopeValid;
    }

    /**
     * Performs the introspection call on the token
     * @param token the token to check
     * @return the response from the introspect call
     */
    private ResponseEntity<String> performInterospect(String token){
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<String> reqAuth = new HttpEntity<String>(createHeaders());
        ResponseEntity<String> response;

        String introspectTokenUrl = url+"?token="+token;
        response = restTemplate.exchange(introspectTokenUrl, HttpMethod.POST, reqAuth, String.class);
        return response;
    }
}
