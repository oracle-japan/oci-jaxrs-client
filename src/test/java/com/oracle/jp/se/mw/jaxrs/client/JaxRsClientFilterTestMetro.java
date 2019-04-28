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
public class JaxRsClientFilterTestMetro {

    public static void main(String[] args) throws Exception{
        
        // expecting client.properties in the root folder
        Properties config = new Properties();
        try(FileInputStream in = new FileInputStream("client.properties")){
            config.load(in);
        }
        String proxy = config.getProperty("proxy");

        // search geo location of Tokyo Station
        String metroToken = config.getProperty("metroToken");
        String metroUrl = "https://api.tokyometroapp.jp/api/v2/datapoints"
                + "?rdf:type=odpt:Station&dc:title=%E6%9D%B1%E4%BA%AC&acl:consumerKey="
                + metroToken; 

        Client client = JaxRsClient.getClient(proxy);
        WebTarget target = client.target(metroUrl);
        Builder builder = target.request();
        Response response = builder.get();
        System.out.println("----------------------------------------");
        System.out.println(response.getStatus() + " " + response.getStatusInfo());
        System.out.println(response.readEntity(String.class));
        client.close();
    }
        

}
