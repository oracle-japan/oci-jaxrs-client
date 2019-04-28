package com.oracle.jp.se.mw.jaxrs.client;


import java.io.FileInputStream;
import java.util.Properties;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;


/**
 * It's not a JUnit test
 * */
public class JaxRsClientFilterTestLooseSSL {

    public static void main(String[] args) throws Exception{
        
        // expecting client.properties in the root folder
        Properties config = new Properties();
        try(FileInputStream in = new FileInputStream("client.properties")){
            config.load(in);
        }

        Client client = JaxRsClient.getLooseClient();
        WebTarget target = client.target("https://localhost:7002/console");
        Builder builder = target.request();
        Response response = builder.get();
        System.out.println("----------------------------------------");
        System.out.println(response.getStatus() + " " + response.getStatusInfo());
        System.out.println(response.readEntity(String.class));
        client.close();

    }
        

}
