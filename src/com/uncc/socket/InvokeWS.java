package com.uncc.socket;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

public class InvokeWS{

	
	private static String baseUrl = "http://" +SocketServer.getHostName() +":port/webService?q=";
	protected HttpURLConnection con;
	 BufferedReader in;
	URL obj;
	private final static String noAck = "No ACK";
	  /**
     * Send GET
     */
    
    String sendGet(String incomingMsg, long packetNumber) throws Exception {
    	String url = new String(baseUrl + incomingMsg);
    	
    	String response = new String();

        try{
        		System.out.println(":: Forming url: " + url);
        		obj = new URL(url);
	            con = (HttpURLConnection) obj.openConnection();
	            // optional default is GET
	            con.setRequestMethod("GET");

	            //add request header
	            //con.setRequestProperty("User-Agent", USER_AGENT);
//	            String encoded = URLEncoder.encode(String.valueOf(incomingMsg), "UTF-8");
	            
	            int responseCode = con.getResponseCode();
	            System.out.println(packetNumber+":: Sending 'GET' request to URL : " + url);
	            System.out.println(new Date() +">"+ packetNumber+"::Response Code : " + responseCode);

	           in = new BufferedReader(
	                    new InputStreamReader(con.getInputStream()));
	            String inputLine;

	            while ((inputLine = in.readLine()) != null) {
	                    response += inputLine;
	            }
	            
	            
	            //print result
	            System.out.println(packetNumber+"::"+response.toString());
        }catch(Exception ex){
        	System.out.println("---------------- Get call Exception ----------------");
            System.out.println(ex.getMessage());
        }finally{
        	in.close();
        }
        
      
        return response;

}
	
	
}
