package com.oracle.jp.se.mw.jaxrs.client;

import java.io.FileInputStream;
import java.net.URL;
import java.util.Properties;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.http.util.EntityUtils;


/**
 * It's not a JUnit test
 * */
public class JaxRsClientFilterTest {

    public static void main(String[] args) throws Exception{
        
        // please prepare client.properties in the root folder
        Properties config = new Properties();
        try(FileInputStream in = new FileInputStream("client.properties")){
            config.load(in);
        }
        String proxy = config.getProperty("proxy");
        String url = config.getProperty("url");

   		System.setProperty("http.proxyHost", "");
        System.setProperty("http.proxyPort", "");
   		System.setProperty("https.proxyHost", "");
        System.setProperty("https.proxyPort", "");

        // case 1
        Client client = JaxRsClientHelper.getClient(proxy);
        client.register(OciJaxRsClientFilter.class); //"read [DEFAULT] of ~/.oci/config"
        WebTarget target = client.target(url);
        Builder builder = target.request();
        Response response = builder.post(Entity.entity(new Message("こんにちは！"), MediaType.APPLICATION_JSON_TYPE));
        System.out.println("----------------------------------------");
        System.out.println(response.getStatus() + " " + response.getStatusInfo());
        System.out.println(response.readEntity(String.class));
        client.close();

        if(null != proxy) {
       		System.setProperty("https.proxyHost", new URL(proxy).getHost());
            System.setProperty("https.proxyPort", Integer.toString(new URL(proxy).getPort()));
        }

        // case 2
        String metroToken = config.getProperty("metroToken");
        String metroUrl = "https://api.tokyometroapp.jp/api/v2/datapoints"
                + "?rdf:type=odpt:Station&dc:title=%E6%9D%B1%E4%BA%AC&acl:consumerKey="
                + metroToken; // search geo location of Tokyo Station

        client = JaxRsClientHelperBase.getClient(); // = JaxRsClientHelper.getClient()
        client.register(new OciJaxRsClientFilter()); //"read [DEFAULT] of ~/.oci/config"
        target = client.target(metroUrl);
        builder = target.request();
        response = builder.get();
        System.out.println("----------------------------------------");
        System.out.println(response.getStatus() + " " + response.getStatusInfo());
        System.out.println(response.readEntity(String.class));
        client.close();
        
   		System.setProperty("https.proxyHost", "");
        System.setProperty("https.proxyPort", "");
        
        // case 3 - allow self-signed certificates
        client = JaxRsClientHelper.getLooseClient();
        target = client.target("https://localhost:7002/console");
        builder = target.request();
        response = builder.get();
        System.out.println("----------------------------------------");
        System.out.println(response.getStatus() + " " + response.getStatusInfo());
        System.out.println(response.readEntity(String.class));
        client.close();

    }
        
    public static class Message{
        public String text;
        public Message() {}
        public Message(String text) { this.text = text; }
    }
    

}
