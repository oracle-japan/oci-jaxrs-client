package com.oracle.jp.se.mw.jaxrs.client;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Optional;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Configuration;

public class JaxRsClientHelperBase {

    public static Client getClient() throws KeyManagementException, NoSuchAlgorithmException {
        return getClient(false, null);
    }
    public static Client getLooseClient() throws KeyManagementException, NoSuchAlgorithmException {
        return getClient(true, null);
    }
    
    public static Client getClient(boolean isLoose, List<Configuration> configs) throws KeyManagementException, NoSuchAlgorithmException {
    	
        ClientBuilder builder = ClientBuilder.newBuilder();
        
        Optional.ofNullable(configs).ifPresent(l -> {
        	l.stream().forEach(c -> {
        		builder.withConfig(c);
        	});
        });
        
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