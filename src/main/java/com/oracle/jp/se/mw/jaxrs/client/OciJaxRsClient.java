package com.oracle.jp.se.mw.jaxrs.client;

import javax.ws.rs.client.Client;

public class OciJaxRsClient{

    public static Client getClient(){
        return JaxRsClient.getClient(false, null).register(OciJaxRsClientFilter.class);
    }
    
    public static Client getLooseClient(){
        return JaxRsClient.getClient(true, null).register(OciJaxRsClientFilter.class);
    }
    
    public static Client getClient(String proxyUri){
        return JaxRsClient.getClient(false, proxyUri).register(OciJaxRsClientFilter.class);
    }
    
    public static Client getLooseClient(String proxyUri){
        return JaxRsClient.getClient(true, proxyUri).register(OciJaxRsClientFilter.class);
    }
    
}