package org.gtri.fhir.api.vistaex.resource.impl;

import ca.uhn.fhir.model.dstu2.resource.*;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.gtri.fhir.api.vistaex.resource.api.VistaExResource;
import org.gtri.fhir.api.vistaex.resource.api.VistaExResourceTranslator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.net.ssl.SSLContext;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Properties;

/**
 * Created by es130 on 8/29/2016.
 */
@Service
public class VistaExResourceImpl implements VistaExResource{
    /*========================================================================*/
    /* CONSTANTS */
    /*========================================================================*/
    private static final String ACCESS_CODE = "accessCode";
    private static final String VERIFY_CODE = "verifyCode";
    private static final String SITE_CODE = "site";
    private static final String RESPONSE_HEADER_COOKIE = "Set-Cookie";
    private static final String REQUEST_HEADER_COOKIE = "Cookie";

    /*========================================================================*/
    /* PRIVATE VARIABLES */
    /*========================================================================*/
    private final Logger logger = LoggerFactory.getLogger(VistaExResourceImpl.class);
    private Properties properties;
    private String site;
    private String serverURL = "";
    private VistaExResourceTranslator vistaExResourceTranslator;
    //TODO: this needs to change for production. For the demo it is ok because we are only using
    //one user. In reality this needs to be stored in the user session.
    private String cookie;
    private SSLConnectionSocketFactory sslsf = null;
    private CloseableHttpClient httpClient = null;
    private HttpClientContext httpClientContext = null;

    /*========================================================================*/
    /* CONSTRUCTORS*/
    /*========================================================================*/
    public VistaExResourceImpl(){
        vistaExResourceTranslator = new VistaExResourceTranslatorImpl();

        try {
            File propertiesFile = getFileInClassPath("gtvistaex.properties");
            FileInputStream fis = new FileInputStream(propertiesFile);
            properties = new Properties();
            properties.load(fis);
            serverURL = properties.getProperty("serverUrl");
            site = properties.getProperty("site");
        }
        catch(FileNotFoundException fnfe){
            fnfe.printStackTrace();
        }
        catch(IOException ioe){
            ioe.printStackTrace();
        }
    }

    /*========================================================================*/
    /* GETTERS */
    /*========================================================================*/
    public String getCookie() {
        return cookie;
    }

    public SSLConnectionSocketFactory getSslsf() {
        if( sslsf == null ){
            sslsf = createSSLConnectionSocketFactory();
        }
        return sslsf;
    }

    public CloseableHttpClient getHttpClient(){
        if( httpClient == null ){
            httpClient = createHttpClient();
        }
        return httpClient;
    }

    public HttpClientContext getHttpClientContext() {
        if( httpClientContext == null ){
            httpClientContext = HttpClientContext.create();
        }
        return httpClientContext;
    }

    /*========================================================================*/
    /* SETTERS */
    /*========================================================================*/
    public void setCookie(String cookie) {
        this.cookie = cookie;
    }

    /*========================================================================*/
    /* PRIVATE METHODS */
    /*========================================================================*/
    private File getFileInClassPath(String fileName) {
        //how to find resource in Servlet
        //http://stackoverflow.com/questions/2161054/where-to-place-and-how-to-read-configuration-resource-files-in-servlet-based-app/2161583#2161583
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        File propertiesFile = new File(classLoader.getResource(fileName).getFile());
        return propertiesFile;
    }

    private SSLConnectionSocketFactory createSSLConnectionSocketFactory(){
        File trustStore = getFileInClassPath("gtVistaExTrustStore");
        SSLContext sslcontext = null;
        SSLConnectionSocketFactory sslsf = null;
        try {
            //TODO: At some point configure to use trust store
            sslcontext = SSLContexts.custom()
                    //.loadTrustMaterial(trustStore, "gtvistaex".toCharArray(),new TrustSelfSignedStrategy())
                    .loadTrustMaterial(new TrustAllStrategy())
                    .build();
            //TODO: Fix to use non-deprecated code
            // Allow TLSv1 protocol only
            sslsf = new SSLConnectionSocketFactory(
                    sslcontext,
                    new String[] { "TLSv1" },
                    null,
                    SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return sslsf;
    }

    private CloseableHttpClient createHttpClient(){
        //TODO: Fix to use non-deprecated code
        RequestConfig globalConfig = RequestConfig.custom()
                .setCookieSpec(CookieSpecs.STANDARD)
                .build();
        CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(globalConfig)
                .setSSLSocketFactory(getSslsf())
                .setHostnameVerifier(SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER)
                .build();
        return httpClient;
    }

    private StringEntity createLoginEntity(){
        StringBuilder loginBuilder = new StringBuilder();
        loginBuilder.append("{")
                .append("\"").append(ACCESS_CODE).append("\":\"").append(properties.getProperty("accessCode")).append("\",")
                .append("\"").append(VERIFY_CODE).append("\":\"").append(properties.getProperty("verifyCode")).append("\",")
                .append("\"").append(SITE_CODE).append("\":\"").append(properties.getProperty("site")).append("\"")
                .append("}");
        logger.debug("Using JSON " + loginBuilder.toString());
        StringEntity loginEntity = new StringEntity(loginBuilder.toString(), ContentType.APPLICATION_JSON);
        return loginEntity;
    }

    private String getJsonResponse(CloseableHttpResponse response) throws IOException{
        InputStream contentInputStream = response.getEntity().getContent();
        String jsonStr = IOUtils.toString( contentInputStream, Charset.forName("UTF-8"));
        logger.debug("Recieved JSON");
        logger.debug("------------------------------");
        logger.debug(jsonStr);
        return jsonStr;
    }

    /*========================================================================*/
    /* PUBLIC METHODS */
    /*========================================================================*/
    @Override
    public Boolean loginToVistaEx() {
        Boolean success = false;
        logger.debug("Logging into VistaEx");

        //TODO: Fix to use non-deprecated code
        HttpPost httpPost = new HttpPost(properties.getProperty("authUrl"));

        //build login JSON and to post
        StringEntity loginEntity = createLoginEntity();
        httpPost.setEntity(loginEntity);

        try {
            CloseableHttpResponse loginResponse = getHttpClient().execute(httpPost, getHttpClientContext());
            try {
                int statusCode = loginResponse.getStatusLine().getStatusCode();
                logger.debug("Response Code " + statusCode);
                success = (statusCode == 200);
                //All we need is the cookie, which is managed as part of the HTTPContext
                //for now ignore the content of the response. At a later date process
                //the JSON. It contains permission and user metadata.
//                HttpEntity responseEntity = loginResponse.getEntity();
            } finally {
                loginResponse.close();
            }
        }
        catch (IOException ioe){
            ioe.printStackTrace();
        }
        logger.debug("Completed login to VistaEx");
        return success;
    }

    @Override
    public Boolean logOutOfVistaEx(){
        logger.debug("Logging out of VistaEx");
        Boolean success = false;
        HttpDelete httpDelete = new HttpDelete(properties.getProperty("authUrl"));
        try {
            CloseableHttpResponse logoutResponse = getHttpClient().execute(httpDelete, getHttpClientContext());
            try {
                int statusCode = logoutResponse.getStatusLine().getStatusCode();
                success = (statusCode == 200);
            }
            finally {
                logoutResponse.close();
            }
        }
        catch(IOException ioe){
            ioe.printStackTrace();
        }
        logger.debug("Finished logging out of VistaEx");
        return success;
    }

    @Override
    /**
     * @param patientID, the id of the patient to search, must be of the format
     *                   <site_id>%3B<patient_id>, %3B is the URL escaped ; char.
     */
    public Patient retrievePatient(String patientId) {
        logger.debug("Getting Patient " + patientId);
        Patient patient = null;
        //assuming patient ID is of format <site_id>%3B<patient_id>, for example C877%3B3
        String patientRecordURL = serverURL + "patient/" + patientId + "?fields=";
        logger.debug("Using URL " + patientRecordURL);
        HttpGet httpGet = new HttpGet(patientRecordURL);
        try {
            CloseableHttpResponse response = getHttpClient().execute(httpGet, getHttpClientContext());
            try {
                String jsonStr = getJsonResponse(response);
                logger.debug("Converting to DSTU2 Patient");
                //now convert to Patient
                patient = vistaExResourceTranslator.translatePatient(jsonStr);
                //Explicitly set the id
                patient.setId(patientId);
            }
            finally{
                response.close();
            }
        }
        catch(IOException ioe){
            ioe.printStackTrace();
        }
        return patient;
    }

    @Override
    public Bundle retrieveMedicationOrderForPatient(String patientId) {
        logger.debug("Getting MedicationOrder for Patient");
        Bundle medicationOrderBundle = null;

        String medicationPrescriptionUrl = serverURL + "patient/" + patientId + "/medicationprescription?limit=&fields=";
        //https://54.173.144.121/resource/fhir/patient/9E7A%3B3/medicationprescription?limit=&fields=
        logger.debug("Using URL " + medicationPrescriptionUrl);
        HttpGet httpGet = new HttpGet(medicationPrescriptionUrl);

        try{
            CloseableHttpResponse response = getHttpClient().execute(httpGet, getHttpClientContext());
            try{
                String jsonStr = getJsonResponse(response);
                medicationOrderBundle = vistaExResourceTranslator.translateMedicationOrderForPatient(jsonStr);
            }
            finally{
                response.close();
            }
        }
        catch(IOException ioe){
            ioe.printStackTrace();
        }

        logger.debug("Finished Getting MedicationOrder");
        return medicationOrderBundle;
    }

    @Override
    public Bundle retrieveConditionForPatient(String patientId) {
        logger.debug("Getting Conditions for Patient {}", patientId);
        Bundle conditionBundle = null;

        String conditionRecordUrl = serverURL + "patient/" + patientId + "/condition?limit=&date-asserted=&onset=&fields=";
        //https://54.173.144.121/resource/fhir/patient/9E7A%3B3/condition?limit=&date-asserted=&onset=&fields=
        logger.debug("Using URL " + conditionRecordUrl);
        HttpGet httpGet = new HttpGet(conditionRecordUrl);

        try{
            CloseableHttpResponse response = getHttpClient().execute(httpGet, getHttpClientContext());
            try{
                String jsonStr = getJsonResponse(response);
                conditionBundle = vistaExResourceTranslator.translateConditionBundleForPatient(jsonStr);
            }
            finally{
                response.close();
            }
        }
        catch(IOException ioe){
            ioe.printStackTrace();
        }

        logger.debug("Finished Getting Conditions");
        return conditionBundle;
    }

    @Override
    public Bundle retrieveObservationForPatient(String patientId) {
        logger.debug("Getting Observation Bundle for Patient {}", patientId);
        Bundle observationBundle = null;
        //https://54.173.144.121/resource/fhir/patient/9E7A%3B3/observation?limit=&date-asserted=&onset=&fields=
        String observationRecordUrl = serverURL + "patient/" + patientId + "/observation?limit=&date-asserted=&onset=&fields=";
        logger.debug("Using URL " + observationRecordUrl);
        HttpGet httpGet = new HttpGet(observationRecordUrl);

        try{
            CloseableHttpResponse response = getHttpClient().execute(httpGet, getHttpClientContext());
            try{
                String jsonStr = getJsonResponse(response);
                observationBundle = vistaExResourceTranslator.translateObservationForPatient(jsonStr);
            }
            finally{
                response.close();
            }
        }
        catch(IOException ioe){
            ioe.printStackTrace();
        }
        logger.debug("Finshed Getting Conditions");
        return observationBundle;
    }

    @Override
    public Procedure retrieveProcedureForPatient(String patientId) {
        return null;
    }

    @Override
    public MedicationAdministration retrieveMedicationAdministrationForPatient(String patientId) {
        return null;
    }

    @Override
    public AllergyIntolerance retrieveAllergyIntoleranceForPatient(String patientId) {
        return null;
    }
}
