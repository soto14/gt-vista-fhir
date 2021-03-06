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

package org.gtri.fhir.api.vistaex.resource.impl;

import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu2.resource.*;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
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
import org.gtri.fhir.api.vistaex.rest.service.util.VistaUtil;
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
import java.util.function.Function;

public class VistaExResourceImpl implements VistaExResource{
    /*========================================================================*/
    /* CONSTANTS */
    /*========================================================================*/
    private static final String ACCESS_CODE = "accessCode";
    private static final String VERIFY_CODE = "verifyCode";
    private static final String SITE_CODE = "site";
    private static final String RESPONSE_HEADER_COOKIE = "Set-Cookie";
    private static final String REQUEST_HEADER_COOKIE = "Cookie";
    private static final String AUTH_URL_PROPERTY = "authUrl";
    private static final String REFRESH_URL_PROPERTY = "refreshUrl";
    private static final String DNS_URL_PROPERTY = "serverDns";
    private static final String FHIR_URL_PROPERTY = "serverFhir";
    private static final String VISIT_URL_PROPERTY = "visitUrl";
    private static final String SITE_PID_DELIMETER = "%3B";

    /*========================================================================*/
    /* PRIVATE VARIABLES */
    /*========================================================================*/
    private final Logger logger = LoggerFactory.getLogger(VistaExResourceImpl.class);
    private Properties properties;
    private String fhirUrl;
    private String authUrl;
    private String refreshUrl;
    private String visitUrl;
    private String siteCode;

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

        properties = VistaUtil.getProperties();
        if( properties != null ) {
            String urlDns = properties.getProperty(DNS_URL_PROPERTY);
            fhirUrl = urlDns + properties.getProperty(FHIR_URL_PROPERTY);
            authUrl = urlDns + properties.getProperty(AUTH_URL_PROPERTY);
            refreshUrl = urlDns + properties.getProperty(REFRESH_URL_PROPERTY);
            visitUrl = urlDns + properties.getProperty(VISIT_URL_PROPERTY);
            siteCode = properties.getProperty(SITE_CODE);
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

    public String getFhirUrl() {
        return fhirUrl;
    }

    public String getAuthUrl() {
        return authUrl;
    }

    public String getRefreshUrl() {
        return refreshUrl;
    }

    public String getVisitUrl() {
        return visitUrl;
    }

    public String getSiteCode() {
        return siteCode;
    }

    /*========================================================================*/
    /* SETTERS */
    /*========================================================================*/
    public void setCookie(String cookie) {
        this.cookie = cookie;
    }

    public void setFhirUrl(String fhirUrl) {
        this.fhirUrl = fhirUrl;
    }

    public void setAuthUrl(String authUrl) {
        this.authUrl = authUrl;
    }

    public void setRefreshUrl(String refreshUrl) {
        this.refreshUrl = refreshUrl;
    }

    public void setVisitUrl(String visitUrl) {
        this.visitUrl = visitUrl;
    }

    public void setSiteCode(String siteCode) {
        this.siteCode = siteCode;
    }

    /*========================================================================*/
    /* PUBLIC METHODS */
    /*========================================================================*/
    @Override
    public Boolean loginToVistaEx() {
        logger.debug("Logging into VistaEx");
        HttpPost httpPost = new HttpPost(getAuthUrl());
        //build login JSON and to post
        StringEntity loginEntity = createLoginEntity();
        httpPost.setEntity(loginEntity);
        Boolean success = processLogInOutRequest(httpPost);
        logger.debug("Completed login to VistaEx");
        return success;
    }

    @Override
    public Boolean refreshSessionOnVistaEx() {
        logger.debug("Refreshing session on VistaEx");
        HttpGet httpGet = new HttpGet(getRefreshUrl());
        Boolean success = processLogInOutRequest(httpGet);
        logger.debug("Finished refreshing session on VistaEx");
        return success;
    }

    @Override
    public Boolean logOutOfVistaEx(){
        logger.debug("Logging out of VistaEx");
        HttpDelete httpDelete = new HttpDelete(getAuthUrl());
        Boolean success = processLogInOutRequest(httpDelete);
        logger.debug("Finished logging out of VistaEx");
        return success;
    }

    @Override
    /**
     * Retrieves a DTSU2 Patient Object, from the VistaEx API, for a patient
     * @param patientId the patient ID to use for the search. The method handles appending the VistaEx Site ID to the
     *                  patient ID before making the call to VistaEx.
     * @return {@link Patient} resource for the patient.
     */
    public Patient retrievePatient(String patientId) {
        logger.debug("Getting Patient " + patientId);
        Patient patient = null;
        //assuming patient ID is of format <site_id>%3B<patient_id>, for example C877%3B3
        String patientRecordURL = getFhirUrl() + "patient/" + getSiteCode() + SITE_PID_DELIMETER + patientId + "?fields=";
        logger.debug("Using URL " + patientRecordURL);
        patient = requestDataFromVistaEx( patientRecordURL, vistaExResourceTranslator::translatePatient);
        //Explicitly set the id
        patient.setId(patientId);
        return patient;
    }

    @Override
    /**
     * Retrieves a DTSU2 Bundle of MedicationOrder Objects, from the VistaEx API, for a patient
     * @param patientId the patient ID to use for the search. The method handles appending the VistaEx Site ID to the
     *                  patient ID before making the call to VistaEx.
     * @return {@link List} of {@link MedicationOrder} resources for the patient.
     */
    public List<MedicationOrder> retrieveMedicationOrderForPatient(String patientId) {
        logger.debug("Getting MedicationOrder for Patient");
        List<MedicationOrder> medicationOrders = null;
        String medicationPrescriptionUrl = getFhirUrl() + "patient/" + getSiteCode() + SITE_PID_DELIMETER + patientId + "/medicationprescription?limit=&fields=";
        //https://54.173.144.121/resource/fhir/patient/9E7A%3B3/medicationprescription?limit=&fields=
        logger.debug("Using URL " + medicationPrescriptionUrl);
        medicationOrders = requestDataFromVistaEx( medicationPrescriptionUrl, vistaExResourceTranslator::translateMedicationOrderForPatient);
        addPatientToMedicationOrders(patientId, medicationOrders);
        logger.debug("Finished Getting MedicationOrder");
        return medicationOrders;
    }

    @Override
    /**
     * Retrieves a DTSU2 Bundle of Condition Objects, from the VistaEx API, for a patient
     * @param patientId the patient ID to use for the search. The method handles appending the VistaEx Site ID to the
     *                  patient ID before making the call to VistaEx.
     * @return {@link Bundle} resource for the patient.
     */
    public Bundle retrieveConditionForPatient(String patientId) {
        logger.debug("Getting Conditions for Patient {}", patientId);
        Bundle conditionBundle = null;
        String conditionRecordUrl = getFhirUrl() + "patient/" + getSiteCode() + SITE_PID_DELIMETER + patientId + "/condition?limit=&date-asserted=&onset=&fields=";
        //https://54.173.144.121/resource/fhir/patient/9E7A%3B3/condition?limit=&date-asserted=&onset=&fields=
        logger.debug("Using URL " + conditionRecordUrl);
        conditionBundle = requestDataFromVistaEx( conditionRecordUrl, vistaExResourceTranslator::translateConditionBundleForPatient);
        logger.debug("Finished Getting Conditions");
        return conditionBundle;
    }

    @Override
    /**
     * Retrieves a DTSU2 Bundle of Observation Objects, from the VistaEx API, for a patient
     * @param patientId the patient ID to use for the search. The method handles appending the VistaEx Site ID to the
     *                  patient ID before making the call to VistaEx.
     * @return {@link Bundle} resource for the patient.
     */
    public Bundle retrieveObservationForPatient(String patientId) {
        logger.debug("Getting Observation Bundle for Patient {}", patientId);
        Bundle observationBundle = null;
        //https://54.173.144.121/resource/fhir/patient/9E7A%3B3/observation?limit=&date-asserted=&onset=&fields=
        String observationRecordUrl = getFhirUrl() + "patient/" + getSiteCode() + SITE_PID_DELIMETER + patientId + "/observation?limit=&date-asserted=&onset=&fields=";
        logger.debug("Using URL " + observationRecordUrl);
        observationBundle = requestDataFromVistaEx( observationRecordUrl, vistaExResourceTranslator::translateObservationForPatient);
        logger.debug("Finished Getting Conditions");
        return observationBundle;
    }

    @Override
    /**
     * Retrieves a DTSU2 Bundle of Procedure Objects, from the VistaEx API, for a patient
     * @param patientId the patient ID to use for the search. The method handles appending the VistaEx Site ID to the
     *                  patient ID before making the call to VistaEx.
     * @return {@link Bundle} resource for the patient.
     */
    public Bundle retrieveProcedureForPatient(String patientId) {
        logger.debug("Getting Procedures");
        Bundle procedureBundle = null;
        //https://54.173.144.121/resource/fhir/patient/9E7A%3B3/procedure?limit=&date-asserted=&onset=&fields=
        String procedureUrl = getFhirUrl() + "patient/" + getSiteCode() + SITE_PID_DELIMETER + patientId + "/procedure?limit=&date-asserted=&onset=&fields=";
        procedureBundle = requestDataFromVistaEx( procedureUrl, vistaExResourceTranslator::translateProcedureForPatient);
        logger.debug("Finished Getting Procedures");
        return procedureBundle;
    }

    @Override
    /**
     * Retrieves a DTSU2 Bundle of MedicationAdministration Object, from the VistaEx API, for a patient
     * @param patientId the patient ID to use for the search. The method handles appending the VistaEx Site ID to the
     *                  patient ID before making the call to VistaEx.
     * @return {@link List} of {@link MedicationAdministration} resources for the patient.
     */
    public List<MedicationAdministration> retrieveMedicationAdministrationForPatient(String patientId) {
        logger.debug("Getting Medication Administration");
        //https://ehmp2.vaftl.us/resource/fhir/medicationadministration?subject.identifier=9E7A%3B3&limit=&fields=
        List<MedicationAdministration> medicationAdministrationList = null;
        String medAdminUrl = getFhirUrl() + "medicationadministration?subject.identifier=" + getSiteCode() + SITE_PID_DELIMETER + patientId + "&limit=&fields=";
        medicationAdministrationList = requestDataFromVistaEx( medAdminUrl, vistaExResourceTranslator::translateMedicationAdministrationForPatient);
        logger.debug("Finished Getting Medication Administration");
        return medicationAdministrationList;
    }

    @Override
    /**
     * Retrieves a DTSU2 Bundle of AllergyIntollerance Objects, from the VistaEx API, for a patient
     * @param patientId the patient ID to use for the search. The method handles appending the VistaEx Site ID to the
     *                  patient ID before making the call to VistaEx.
     * @return {@link Bundle} resource for the patient.
     */
    public Bundle retrieveAllergyIntoleranceForPatient(String patientId) {
        logger.debug("Getting Allergy Intolerances");
        Bundle allergyBundle = null;
        String allergyUrl = getFhirUrl() + "allergyintolerance?subject.identifier=" + getSiteCode() + SITE_PID_DELIMETER + patientId +"&uid=&start=&limit=&fields=";
        logger.debug("Using URL " + allergyUrl);
        allergyBundle = requestDataFromVistaEx( allergyUrl, vistaExResourceTranslator::translateAllergyIntoleranceForPatient);
        logger.debug("Finished Getting Allergy Intolerances");
        return allergyBundle;
    }

    @Override
    /**
     * Retrieves Vista Ex Visit objects and converts it to a DTSU2 Encounter to the best of its ability.
     * @param patientId the patient ID to use for the search. The method handles appending the VistaEx Site ID to the
     *                  patient ID before making the call to VistaEx.
     * @return List of {@link Encounter} resource for the patient.
     */
    public List<Encounter> retrieveEncountersForPatient(String patientId) {
        logger.debug("Getting Encounters for Patient {}", patientId);
        List<Encounter> encounters = null;
        //https://54.173.144.121/resource/patient/record/domain/visit?pid=9E7A%3B3&uid=&start=&limit=&filter=&order=&callType=&vler_uid=&fields=
        String pid = getSiteCode() + SITE_PID_DELIMETER + patientId;
        String visitUrlWithParam = getVisitUrl() + "?pid=" + pid + "&uid=&start=&limit=&filter=eq(%22pid%22%2C%22" + pid + "%22)&order=&callType=&vler_uid=&fields=";
        logger.debug("Using URL " + visitUrlWithParam);
        encounters = requestDataFromVistaEx( visitUrlWithParam, vistaExResourceTranslator::translateEncounterforPatient );
        logger.debug("Finished Getting Encounters");
        return encounters;
    }

    /*========================================================================*/
    /* PRIVATE METHODS */
    /*========================================================================*/

    /**
     * Goes through a List of MedicationOrder objects and adds a patient element to them.
     * @param patientId
     * @param medicationOrders
     */
    private void addPatientToMedicationOrders(String patientId, List<MedicationOrder> medicationOrders){
        //go through and add patient information to each MedicationOrder, this data is not returned in the query to
        //the VistaEx medicationprescription endpoint.
        for(MedicationOrder mo : medicationOrders){
            mo.setPatient(new ResourceReferenceDt("Patient/" + patientId));
        }
    }

    /**
     * Handles executing and processing the response of VistaEx requests dealing with
     * login, session refresh, and logout
     * @param request the request to process. It will either be an {@link HttpPost}, {@link HttpGet}, or {@link HttpDelete}
     * @return true if the request was successful, false otheriwse.
     */
    private Boolean processLogInOutRequest(HttpUriRequest request){
        Boolean success = false;
        try {
            CloseableHttpResponse logoutResponse = getHttpClient().execute(request, getHttpClientContext());
            success = checkLoginResponse(logoutResponse);
        }
        catch(IOException ioe){
            ioe.printStackTrace();
        }
        return success;
    }

    /**
     * Finds a file on the classpath
     * @param fileName the name of the file to find
     * @return the file.
     */
    private File getFileInClassPath(String fileName) {
        //how to find resource in Servlet
        //http://stackoverflow.com/questions/2161054/where-to-place-and-how-to-read-configuration-resource-files-in-servlet-based-app/2161583#2161583
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        File propertiesFile = new File(classLoader.getResource(fileName).getFile());
        return propertiesFile;
    }

    /**
     * Creates and configures a {@link SSLConnectionSocketFactory} to use in the application
     * @return
     */
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

    /**
     * Creates a {@link CloseableHttpClient} to use in the application
     * @return
     */
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

    /**
     * Creates a {@link StringEntity} containing the JSON used for logging in to the VistaEx system
     * @return
     */
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

    /**
     * Retrieves the JSON payload from a {@link CloseableHttpResponse}
     * @param response the response to process
     * @return the String representation of the JSON payload
     * @throws IOException
     */
    private String getJsonResponse(CloseableHttpResponse response) throws IOException{
        InputStream contentInputStream = response.getEntity().getContent();
        String jsonStr = IOUtils.toString( contentInputStream, Charset.forName("UTF-8"));
        return jsonStr;
    }

    /**
     * Checks the {@link CloseableHttpResponse} for a login/logout/session renew request
     * @param loginResponse the response to process.
     * @return true if the request was successful, false otherwise
     * @throws IOException
     */
    private Boolean checkLoginResponse(CloseableHttpResponse loginResponse) throws IOException {
        Boolean success = false;
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
        return success;
    }

    /**
     * Generic method used to call a VistaEx URL and translate the results using a specific translator function
     * @param vistaUrl the VistaEx URL to call
     * @param translatorFunction the translator function to use to translate the results. The passed in function
     *                           must take a {@link String} as an input parameter, and return a single value object
     *                           of any type.
     * @param <R> The return type from the translator function.
     * @return The object returned by the translatorFunction.
     */
    private <R> R requestDataFromVistaEx(String vistaUrl, Function<String,R> translatorFunction){
        R returnObject = null;
        HttpGet httpGet = new HttpGet(vistaUrl);
        try{
            CloseableHttpResponse response = getHttpClient().execute(httpGet, getHttpClientContext());
            try{
                String jsonStr = getJsonResponse(response);
                //call the passed in translator function with the json returned in the response.
                returnObject = translatorFunction.apply(jsonStr);
            }
            finally{
                response.close();
            }
        }
        catch(IOException ioe){
            ioe.printStackTrace();
        }
        return returnObject;
    }
}
