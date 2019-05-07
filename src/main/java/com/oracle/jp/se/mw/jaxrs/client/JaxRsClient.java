package com.oracle.jp.se.mw.jaxrs.client;

import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

public class JaxRsClient{
	
    public static Client getClient(){
        return getBuilder(false, null).build();
    }
    
    public static Client getLooseClient(){
        return getBuilder(true, null).build();
    }
    
    public static Client getClient(String proxyUri){
        return getBuilder(false, proxyUri).build();
    }
    
    public static Client getLooseClient(String proxyUri){
        return getBuilder(true, proxyUri).build();
    }
    
    public static ClientBuilder getBuilder(boolean isLoose, String proxyUri){

        ClientBuilder builder = ClientBuilder.newBuilder();

        try {

            // enable proxy
            if(null != proxyUri && proxyUri.length() > 0){
                // It cannot coexist with chunk setting (probably...)
                //config.connectorProvider(new ApacheConnectorProvider());
                //config.property(ClientProperties.PROXY_URI, proxyUri);
                URL url = new URL(proxyUri);
                
                System.setProperty("http.proxyHost", url.getHost());
                System.setProperty("http.proxyPort", new Integer(url.getPort()).toString());
                System.setProperty("https.proxyHost", url.getHost());
                System.setProperty("https.proxyPort", new Integer(url.getPort()).toString());
            }else {
            	// these can be set via -D even when proxyUri is null
                //System.clearProperty("http.proxyHost");
                //System.clearProperty("http.proxyPort");
                //System.clearProperty("https.proxyHost");
                //System.clearProperty("https.proxyPort");
            }

            // allow loose SSL connection
            if(isLoose) {
                builder.sslContext(getSSLContext()).hostnameVerifier(new TrustAllHostNameVerifier());
            }
            
            return builder;

        }catch(Exception e) {
            throw new RuntimeException("Cannot create client: " + e.getMessage(), e);
        }
    }

    private static SSLContext getSSLContext() throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext ctx = SSLContext.getInstance(/* "SSL" */"TLS");
        ctx.init(null, certs, new SecureRandom());
        return ctx;
    }

    private static TrustManager[] certs = new TrustManager[] { new X509TrustManager() {
        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }
    } };

    private static class TrustAllHostNameVerifier implements HostnameVerifier {

        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }
    
}