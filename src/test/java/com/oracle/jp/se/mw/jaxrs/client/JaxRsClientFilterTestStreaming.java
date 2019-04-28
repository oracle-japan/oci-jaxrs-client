package com.oracle.jp.se.mw.jaxrs.client;

import java.io.FileInputStream;
import java.util.Properties;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.annotation.JsonInclude;


/**
 * It's not a JUnit test
 * 
 * */
public class JaxRsClientFilterTestStreaming {

    public static void main(String[] args) throws Exception{

        //System.setProperty("javax.net.debug", "all");
    	
        // expecting client.properties in the root folder
        Properties config = new Properties();
        try(FileInputStream in = new FileInputStream("client.properties")){
            config.load(in);
        }
        String proxy = config.getProperty("proxy");

        String streaming_url = config.getProperty("streaming_host_url");
        System.out.println("Host: " + streaming_url);
        String streamId = config.getProperty("stream_id");
        System.out.println("Stream ID: " + streamId);

        Client client = OciJaxRsClient.getClient(proxy);

        //POST /20180418/streams/{streamId}/cursors
        
        CreateCursorRequest cursorRequest = new CreateCursorRequest();
        cursorRequest.partition = "0";
        cursorRequest.type = "TRIM_HORIZON";
        //cursorRequest.type = "AT_OFFSET";
        //cursorRequest.offset = 0;
        
        WebTarget target = client.target(streaming_url).path("/20180418/streams/{streamId}/cursors").resolveTemplate("streamId", streamId);
        System.out.println("Target URL: " + target.getUri());
                
        Builder builder = target.request();
        Response response = builder.post(Entity.entity(cursorRequest, MediaType.APPLICATION_JSON_TYPE));
        System.out.println("----------------------------------------");
        int status = response.getStatus();
        System.out.println(status + " " + response.getStatusInfo());
        if(200 == status) {
        	CreateCursorResponse cursorResponse = response.readEntity(CreateCursorResponse.class);
        	System.out.println(cursorResponse.value);
        }else {
            System.out.println(response.readEntity(String.class));
        }
        client.close();
        
    }
        
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class CreateCursorRequest{
        public String partition;
        public String type;
        public int offset;
        public String time;
    }
    
    public static class CreateCursorResponse{
        public String value;
    }

    public static class ErrorMessage{
        public String code;
        public String message;
        public String toString() {
        	return String.format("Error: %s - %s", code, message); 
        }
    }


}
