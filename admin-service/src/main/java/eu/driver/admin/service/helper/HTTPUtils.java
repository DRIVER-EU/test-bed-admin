package eu.driver.admin.service.helper;

import java.io.FileInputStream;
import java.net.URL;
import java.security.KeyStore;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

import eu.driver.adapter.excpetion.CommunicationException;
import eu.driver.adapter.properties.ClientProperties;

public class HTTPUtils {

	private Logger log = Logger.getLogger(this.getClass());
	private SSLContext sc = null;
	
	private ClientProperties clientProp = ClientProperties.getInstance();
	
	private String username = "admin";
	private String password = "admin";
	private String certFilePassword = "changeit";
	
	public HTTPUtils() {
		
		username = clientProp.getProperty("http.username", "admin");
		password = clientProp.getProperty("http.password", "admin");
		
		certFilePassword = clientProp.getProperty("cert.file.password", "changeit");
		
		try {
			KeyStore ks = KeyStore.getInstance("PKCS12");
			FileInputStream fis = new FileInputStream(clientProp.getProperty("cert.file.path", "config/cert/TB-AdminTool.p12"));
			ks.load(fis, certFilePassword.toCharArray());
			KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
			kmf.init(ks, certFilePassword.toCharArray());
			sc = SSLContext.getInstance("TLS");
			sc.init(kmf.getKeyManagers(), null, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public String postHTTPRequest(String url, String requestMethod, String contentType, String msgParam) throws CommunicationException {
		String response = null;
		log.info("url: " + url);
		log.info("data: " + msgParam);
		
		try {
			HttpsURLConnection connection = (HttpsURLConnection) (new URL(url)).openConnection();
			if (connection instanceof HttpsURLConnection) {
			    ((HttpsURLConnection)connection).setSSLSocketFactory(sc.getSocketFactory());
			}
	        connection.setRequestMethod(requestMethod);
	        connection.setDoOutput(true);
			connection.setRequestProperty("Content-Type", contentType);
			connection.setRequestProperty("Accept", contentType);
			connection.setRequestProperty( "Encoding", "UTF-8");
			
			String authorization = null;
			
			if (username != null && password != null) {
	            authorization = username + ":" + password;
	        }
			
			if (authorization != null) {
	            String base64Authorization = "BASIC " + new String(Base64.encodeBase64(authorization.getBytes()));
	            connection.setRequestProperty("Authorization", base64Authorization);
	        }
			
			if (msgParam != null) {
				String param = msgParam;
				byte[] postDataBytes = param.getBytes("UTF-8");
				connection.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
				connection.getOutputStream().write(postDataBytes);
				connection.getOutputStream().flush();
				connection.getOutputStream().close(); 
			}
			
			int code = connection.getResponseCode();
			log.info("ResponeCode: " + code + ", " + connection.getResponseMessage());
			if (code == 200 || code == 201 || code == 204) {
				log.info("The message was distributed successfully!");
				response = "The message was distributed successfully!";
			} else {
				log.error("Error distributing the Message to: " + url);
				throw new CommunicationException("Error distributing the message!");
			}
		} catch (CommunicationException ce) {
			ce.printStackTrace();
			throw ce;
		} catch (Exception e) {
			log.error("Error distributing the Message to: " + url, e);
			throw new CommunicationException("Error distributing the message!", e);
		}
		
		return response;
		
	}

}
