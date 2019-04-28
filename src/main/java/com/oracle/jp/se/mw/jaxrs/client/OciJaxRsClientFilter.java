package com.oracle.jp.se.mw.jaxrs.client;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Key;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

import org.tomitribe.auth.signatures.MissingRequiredHeaderException;
import org.tomitribe.auth.signatures.PEM;
import org.tomitribe.auth.signatures.Signature;
import org.tomitribe.auth.signatures.Signer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.hash.Hashing;

@Provider
//@Priority(Priorities.USER)
public class OciJaxRsClientFilter implements ClientRequestFilter {
	
	private ObjectMapper mapper = new ObjectMapper();

	private static final SimpleDateFormat DATE_FORMAT;
    private static final String SIGNATURE_ALGORITHM = "rsa-sha256";
    private static final Map<String, List<String>> REQUIRED_HEADERS;
    static {
        DATE_FORMAT = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));

        REQUIRED_HEADERS = ImmutableMap.<String, List<String>>builder()
                .put("get", ImmutableList.of("date", "(request-target)", "host"))
                .put("head", ImmutableList.of("date", "(request-target)", "host"))
                .put("delete", ImmutableList.of("date", "(request-target)", "host"))
                .put("put", ImmutableList.of("date", "(request-target)", "host", "content-length", "content-type", "x-content-sha256"))
                .put("post", ImmutableList.of("date", "(request-target)", "host", "content-length", "content-type", "x-content-sha256"))
        .build();
    }
    
    private Map<String, Signer> signers;
	private String apiKey;
	private PrivateKey privateKey;
	
	public OciJaxRsClientFilter() throws FileNotFoundException, IOException {
		try {
			init();
		}catch(Exception ignore) {}
	}

	public OciJaxRsClientFilter(String config) throws FileNotFoundException, IOException {
		super();
		init(config, null);
	}

	
	public OciJaxRsClientFilter(String config, String section) throws FileNotFoundException, IOException {
		init(config, section);
	}

	public OciJaxRsClientFilter(String tenancy, String user, String fingerprint, String keyFile) {
		init(tenancy, user, fingerprint, keyFile);
	}
	
	public void init() throws FileNotFoundException, IOException {
		String userHome = System.getProperty("user.home");
		Path config = Paths.get(userHome, ".oci/config");
		init(config.toString(), null);
	}
	
	public void init(String config, String section) throws FileNotFoundException, IOException {
		
		String sec = Optional.ofNullable(section).orElse("DEFAULT");
		log("[init] config: " + config);
		log("[init] section: " + sec);
		
		@SuppressWarnings("unused")
		String user = null, tenancy = null, region = null, keyFile = null, fingerprint = null;
		
		boolean inSection = false;
		try(FileInputStream fin = new FileInputStream(config)) {
			BufferedReader reader = new BufferedReader(new InputStreamReader(fin));
			Pattern pSec = Pattern.compile("\\s*\\[(.*)\\]\\s*");
			while(true) {
				String line = reader.readLine();
				if(null == line) break;
				Matcher m = pSec.matcher(line);
				log("matching: " + line);
				if(m.matches()) {
					inSection = m.group(1).equalsIgnoreCase(sec);
				}
				if(inSection){
					int pos = line.indexOf("=");
					if(-1 != pos) {
						String key = line.substring(0, pos).trim();
						String val = line.substring(pos +1).trim();
						log(String.format("key=%s, val=%s", key, val));
						if(key.equalsIgnoreCase("tenancy")) tenancy = val;
						if(key.equalsIgnoreCase("user")) user = val;
						if(key.equalsIgnoreCase("fingerprint")) fingerprint = val;
						if(key.equalsIgnoreCase("key_file")) keyFile = val;
						if(key.equalsIgnoreCase("region")) region = val;
					}
					
				}
			}
		}

		init(tenancy, user, fingerprint, keyFile);
	}

	public void init(String tenancy, String user, String fingerprint, String keyFile) {
		log("[init] tenancy:" + tenancy);
		log("[init] user:" + user);
		log("[init] fingerprint:" + fingerprint);
		log("[init] keyFile:" + keyFile);
		
		this.apiKey = (tenancy + "/" + user + "/" + fingerprint);
        this.privateKey = loadPrivateKey(keyFile);
		
        this.signers = REQUIRED_HEADERS
                .entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> entry.getKey(),
                        entry -> buildSigner(apiKey, privateKey, entry.getKey())));
	}


    private static PrivateKey loadPrivateKey(String privateKeyFilename) {
    	log("privateKeyFilename: " + privateKeyFilename);
        try (InputStream privateKeyStream = Files.newInputStream(Paths.get(privateKeyFilename))){
            return PEM.readPrivateKey(privateKeyStream);
        } catch (InvalidKeySpecException e) {
                throw new RuntimeException("Invalid format for private key");
        } catch (IOException e) {
            throw new RuntimeException("Failed to load private key");
        }
    }

    protected Signer buildSigner(String apiKey, Key privateKey, String method) {
        final Signature signature = new Signature(
                apiKey, SIGNATURE_ALGORITHM, null, REQUIRED_HEADERS.get(method.toLowerCase()));
        return new Signer(privateKey, signature);
    }
	
	@Override
	public void filter(ClientRequestContext context) throws IOException {
		//dumpContext(context);
		signRequest(context);
		//dumpContext(context);
	}

	List<Object> getHeaderValue(MultivaluedMap<String, Object> headers, String key){
		for(String k : headers.keySet()) {
			if(k.equalsIgnoreCase(key)) {
				return headers.get(k);
			}
		}
		return null;
	}

	public void signRequest(ClientRequestContext context) {
		log("[signRequest]");
		
    	final String method = context.getMethod().toLowerCase();
        // nothing to sign for options
        if (method.equals("options")) {
            return;
        }

        final String path = extractPath(context.getUri());
        log("path: " + path);

		MultivaluedMap<String, Object> headers = context.getHeaders();
        headers.remove("Transter-Encoding");

        // supply date if missing
        if (null == getHeaderValue(headers, "date")) {
        	String date = DATE_FORMAT.format(new Date());
            headers.putSingle("date", date);
        	log("date header is missing - " + date);
        }

        // supply host if mossing
        if (null == getHeaderValue(headers, "host")) {
        	String host = context.getUri().getHost();
        	headers.putSingle("host", context.getUri().getHost());
        	log("host header is missing - " + host);
        }

        // supply content-type, content-length, and x-content-sha256 if missing (PUT and POST only)
        if (method.equals("put") || method.equals("post")) {
            if (null == getHeaderValue(headers, "content-type")) {
            	headers.putSingle("content-type", "application/json");
            }
            if (null == getHeaderValue(headers, "content-length") || null == getHeaderValue(headers, "x-content-sha256")) {
                byte[] body = null;
                Object entity = context.getEntity();
                log("Entity class: " + entity.getClass().getName());
                if(entity instanceof byte[]) {
                	body = (byte[])entity;
                }else if(entity instanceof String) {
               		body = ((String)entity).getBytes(StandardCharsets.UTF_8);
                }else if(entity instanceof InputStream) {
                	body = getRequestBody((InputStream)entity);
                }else {
                	String subType = context.getMediaType().getSubtype();
                    log("Subtype: " + subType);
                	if(subType.equalsIgnoreCase("json")) {
                		try {
                			body = mapper.writeValueAsBytes(entity);
                			log("body: " + new String(body));
                		}catch (JsonProcessingException e) {
                        	throw new OciJaxRsClientFilterException(e.getMessage(), e);
						}
                	}else {
                    	throw new OciJaxRsClientFilterException("Unknown entity class: " + entity.getClass().getName());
                	}
                }
                context.setEntity(body); //入れなおす
    			log("body: " + new String(body));
            	headers.putSingle("content-length", Integer.toString(body.length));
            	headers.putSingle("x-content-sha256", calculateSHA256(body));
            }
        }

        final Map<String, String> sigheaders = extractHeadersToSign(context);
        sigheaders.forEach((k,v) -> {log(String.format("(sign header) %s: %s", k, v));});
        final String signature = this.calculateSignature(method, path, sigheaders);
        headers.putSingle("Authorization", signature);
    	headers.remove("content-length");
    }

    private static String extractPath(URI uri) {
        String path = uri.getRawPath();
        path = path.equals("") ? "/" : path;
        String query = uri.getRawQuery();
        if (query != null && !query.trim().isEmpty()) {
            path = path + "?" + query;
        }
        return path;
    }

    private Map<String, String> extractHeadersToSign(ClientRequestContext context) {
        List<String> headersToSign = REQUIRED_HEADERS.get(context.getMethod().toLowerCase());
        if (headersToSign == null) {
            throw new RuntimeException("Don't know how to sign method " + context.getMethod());
        }
		final MultivaluedMap<String, Object> headers = context.getHeaders();

        return headersToSign.stream()
                // (request-target) is a pseudo-header
                .filter(header -> !header.toLowerCase().equals("(request-target)"))
                .collect(Collectors.toMap(
                header -> header,
                header -> {
                    if (null == getHeaderValue(headers, header)) {
                        throw new MissingRequiredHeaderException(header);
                    }
                    List<Object> hList = getHeaderValue(headers, header);
                    if (hList.size() > 1) {
                        throw new RuntimeException(
                                String.format("Expected one value for header %s", header));
                    }
                    return hList.get(0).toString();
                }));
    }

    private String calculateSignature(String method, String path, Map<String, String> headers) {
        Signer signer = this.signers.get(method);
        if (signer == null) {
            throw new RuntimeException("Don't know how to sign method " + method);
        }
        try {
            return signer.sign(method, path, headers).toString();
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate signature", e);
        }
    }

    private String calculateSHA256(byte[] body) {
        byte[] hash = Hashing.sha256().hashBytes(body).asBytes();
        return Base64.getEncoder().encodeToString(hash);
    }

    private byte[] getRequestBody(InputStream in) {
        ByteArrayOutputStream content = new ByteArrayOutputStream();
        try(BufferedInputStream bin = new BufferedInputStream(in)){
            while(true) {
        		int c = bin.read();
            	if(-1 == c) break;
            	content.write(c);
            }
       	}catch(IOException e) {
        	throw new RuntimeException(e.getMessage());	
       	}
        byte[] body = content.toByteArray();
        return body;
    }
   
	@SuppressWarnings("unused")
	private void dumpContext(ClientRequestContext context) {
		System.out.println("*** ClientRequestContext ***");
		System.out.println("URL: " + context.getUri());
		System.out.println("Method: " + context.getMethod());
		System.out.println("Media Type: " + context.getMediaType());
		System.out.println("Entity Class: " + context.getEntityClass().getClass().getName());
		System.out.println("Entity Type: " + context.getEntityType());
		System.out.println("Headers >>");
		MultivaluedMap<String, Object> headers = context.getHeaders();
		headers = context.getHeaders();
		headers.forEach((k, v) -> {
			v.stream().forEach(i -> {
				System.out.println(String.format("%s: %s (%s)", k, i.toString(), i.getClass().getName()));
			});
		});
		System.out.println("*** ClientRequestContext ***");
	}

    public static class OciJaxRsClientFilterException extends RuntimeException{
		private static final long serialVersionUID = 1L;
		public OciJaxRsClientFilterException() {
    		super();
    	}
    	public OciJaxRsClientFilterException(String message) {
    		super(message);
    	}
    	public OciJaxRsClientFilterException(String message, Throwable e) {
    		super(message, e);
    	}
    }
    
	public static void log(Object o) {
		//System.out.println(o.toString());
	}

	
}
