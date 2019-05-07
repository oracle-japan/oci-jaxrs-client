package com.oracle.jp.se.mw.jaxrs.client;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;

public class OciJaxRsClient{

    public static Client getClient(){
        ClientBuilder builder = JaxRsClient.getBuilder(false, null);
        return setConfig(builder).build().register(OciJaxRsClientFilter.class);
    }
    
    public static Client getLooseClient(){
    	ClientBuilder builder = JaxRsClient.getBuilder(true, null);
        return setConfig(builder).build().register(OciJaxRsClientFilter.class);
    }
    
    public static Client getClient(String proxyUri){
    	ClientBuilder builder = JaxRsClient.getBuilder(false, proxyUri);
        return setConfig(builder).build().register(OciJaxRsClientFilter.class);
    }
    
    public static Client getLooseClient(String proxyUri){
    	ClientBuilder builder = JaxRsClient.getBuilder(true, proxyUri);
        return setConfig(builder).build().register(OciJaxRsClientFilter.class);
    }
    
    public static ClientBuilder setConfig(ClientBuilder builder) {
        // avoid chunked transfer when using jersey, because content-lengh is not set with chunked transfer
        ClientConfig config = new ClientConfig();
        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
        config.property(ClientProperties.CHUNKED_ENCODING_SIZE, Integer.MAX_VALUE);
        builder.withConfig(config);
        return builder;
    }
    
    
    
    
}