package com.oracle.jp.se.mw.jaxrs.client;

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

import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;

public class JaxRsClientHelper {

    public static Client getClient() throws KeyManagementException, NoSuchAlgorithmException {
        return getClient(null);
    }
    public static Client getClient(String proxyUri) throws KeyManagementException, NoSuchAlgorithmException {
        return getLooseSslClient(false, proxyUri);
    }
    public static Client getLooseSslClient() throws KeyManagementException, NoSuchAlgorithmException {
        return getLooseSslClient(null);
    }
    public static Client getLooseSslClient(String proxyUri) throws KeyManagementException, NoSuchAlgorithmException {
        return getLooseSslClient(true, proxyUri);
    }
    
    private static Client getLooseSslClient(boolean isLoose, String proxyUri) throws KeyManagementException, NoSuchAlgorithmException {
        ClientConfig config = new ClientConfig();

        // providerをproxy対応にする
        if(null != proxyUri && proxyUri.length() > 0){
            config.connectorProvider(new ApacheConnectorProvider());
            config.property(ClientProperties.PROXY_URI, proxyUri);
            // config.property(ClientProperties.PROXY_USERNAME, "userName");
            // config.property(ClientProperties.PROXY_PASSWORD, "password");
        }
        
        ClientBuilder builder = ClientBuilder.newBuilder();
        builder.withConfig(config);
        if(isLoose) {
            builder.sslContext(getSSLContext()).hostnameVerifier(new TrustAllHostNameVerifier());
        }
        return builder.build();
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