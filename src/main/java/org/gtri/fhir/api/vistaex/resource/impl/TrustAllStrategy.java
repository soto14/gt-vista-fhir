package org.gtri.fhir.api.vistaex.resource.impl;

import org.apache.http.conn.ssl.TrustStrategy;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * Created by es130 on 8/30/2016.
 * Implemented per http://stackoverflow.com/questions/34655031/javax-net-ssl-sslpeerunverifiedexception-host-name-does-not-match-the-certifica
 * to allow client to accept cert for the VistA EX API.
 */
public class TrustAllStrategy implements TrustStrategy {
    @Override
    public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        return true;
    }
}
