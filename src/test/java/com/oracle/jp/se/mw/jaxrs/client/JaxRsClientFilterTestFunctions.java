package com.oracle.jp.se.mw.jaxrs.client;

import java.io.FileInputStream;
import java.util.Properties;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * It's not a JUnit test
 * 
 */
public class JaxRsClientFilterTestFunctions {

    public static void main(String[] args) throws Exception{
        
        // expecting client.properties in the root folder
        Properties config = new Properties();
        try(FileInputStream in = new FileInputStream("client.properties")){
            config.load(in);
        }
        String proxy = config.getProperty("proxy");
        String url = config.getProperty("url");
        String body = config.getProperty("body");

        Client client = OciJaxRsClient.getClient(proxy);
        WebTarget target = client.target(url);
        Builder builder = target.request();
        Response response = builder.post(Entity.entity(body, MediaType.APPLICATION_JSON_TYPE));
        System.out.println("----------------------------------------");
        System.out.println(response.getStatus() + " " + response.getStatusInfo());
        System.out.println(response.readEntity(String.class));
        client.close();
    }
        

}
