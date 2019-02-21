package eu.driver.admin.service.helper;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.KeyStore;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

import org.apache.log4j.Logger;

import eu.driver.adapter.excpetion.CommunicationException;

public class CertHTTPUtils {

	private Logger log = Logger.getLogger(this.getClass());
	private SSLContext sc = null;
	
	public CertHTTPUtils(String superUserPwd) {
		log.info("--> CertHTTPUtils");
		try {
			KeyStore ks = KeyStore.getInstance("PKCS12");
			FileInputStream fis = new FileInputStream("config/cert/superadmin.p12");
			ks.load(fis, superUserPwd.toCharArray());
			KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
			kmf.init(ks, superUserPwd.toCharArray());
			sc = SSLContext.getInstance("TLS");
			sc.init(kmf.getKeyManagers(), null, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		log.info("CertHTTPUtils -->");
	}
	
	@SuppressWarnings("static-access")
	public String postHTTPRequest(String url, String requestMethod, String contentType, String msgParam) throws CommunicationException {
		log.info("--> postHTTPRequest");
		String response = null;
		log.info("url: " + url);
		log.info("data: " + msgParam);
		
		try {
			HttpsURLConnection connection = (HttpsURLConnection) (new URL(url)).openConnection();
			if (connection instanceof HttpsURLConnection) {
			    ((HttpsURLConnection)connection).setSSLSocketFactory(sc.getSocketFactory());
			    ((HttpsURLConnection)connection).setDefaultHostnameVerifier(
			    		new javax.net.ssl.HostnameVerifier(){

			    		    public boolean verify(String hostname,
			    		            javax.net.ssl.SSLSession sslSession) {
			    		        return true;
			    		    }
			    		});
			}
	        connection.setRequestMethod(requestMethod);
	        connection.setDoOutput(true);
			connection.setRequestProperty("Content-Type", contentType);
			connection.setRequestProperty("Accept", contentType);
			connection.setRequestProperty( "Encoding", "UTF-8");
			
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
				BufferedReader br = new BufferedReader(new InputStreamReader((connection.getInputStream())));
				StringBuilder sb = new StringBuilder();
				String output;
				while ((output = br.readLine()) != null) {
				  sb.append(output);
				}
				response = sb.toString();
			} else {
				log.error("Error distributing the Message to: " + url);
				throw new CommunicationException("Error distributing the message!");
			}
		} catch (CommunicationException ce) {
			log.error("Error distributing the Message to: " + url, ce);
			throw ce;
		} catch (Exception e) {
			log.error("Error distributing the Message to: " + url, e);
			throw new CommunicationException("Error distributing the message!", e);
		}
		
		log.info("postHTTPRequest -->");
		return response;
		
	}

}
