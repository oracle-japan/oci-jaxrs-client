package com.oracle.jp.se.mw.jaxrs.client;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Configuration;

import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;

public class JaxRsClientHelper {

    public static Client getClient() throws KeyManagementException, NoSuchAlgorithmException {
        return JaxRsClientHelperBase.getClient(false, null);
    }
    public static Client getLooseClient() throws KeyManagementException, NoSuchAlgorithmException {
        return JaxRsClientHelperBase.getClient(true, null);
    }
    public static Client getClient(String proxyUri) throws KeyManagementException, NoSuchAlgorithmException {
        return getClient(false, proxyUri);
    }
    public static Client getLooseClient(String proxyUri) throws KeyManagementException, NoSuchAlgorithmException {
        return getClient(true, proxyUri);
    }
    
    public static Client getClient(boolean isLoose, String proxyUri) throws KeyManagementException, NoSuchAlgorithmException {

    	List<Configuration> list = new ArrayList<Configuration>();
    	
        // providerをproxy対応にする
        if(null != proxyUri && proxyUri.length() > 0){
        	ClientConfig config = new ClientConfig();
            config.connectorProvider(new ApacheConnectorProvider());
            config.property(ClientProperties.PROXY_URI, proxyUri);
            list.add(config);
        }
        
        return JaxRsClientHelperBase.getClient(isLoose, list);
    }

}