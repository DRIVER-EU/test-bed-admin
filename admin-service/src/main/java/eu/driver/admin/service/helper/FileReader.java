package eu.driver.admin.service.helper;

import java.io.BufferedReader;

public class FileReader {
	
	public FileReader() {
		
	}
	
	public String readFile(String file) {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new java.io.FileReader(file));
			String         line = null;
			StringBuilder  stringBuilder = new StringBuilder();
			String         ls = System.getProperty("line.separator");
	    
	        while((line = reader.readLine()) != null) {
	            stringBuilder.append(line);
	            stringBuilder.append(ls);
	        }

	        return stringBuilder.toString();
	    } catch (Exception e) {
	    	return null;
	    } finally {
	        try {
	        	reader.close();
	        } catch (Exception e) {
	        	// ignore
	        }
	    }
	}

}
