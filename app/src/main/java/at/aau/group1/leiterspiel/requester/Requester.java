package at.aau.group1.leiterspiel.requester;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Requester {
	private final static String USER_AGENT = "Mozilla/5.0";
	private final static String bURL = "http://www.nope.systems/_se2/";
	
	/**
	 * sends a request to the server in order to create a new user
	 * @param user - Username (<= 16 characters)
 	 * @param nick - Nickname (<= 16 characters)
	 * @param pw   - Password (unhashed)
	 * @param mail - email address
	 * 
	 * @return responseCode - (-1->JAVA Error, 100->noError, 404->dataMissing, 800->MySQLError, 801->UserAlreadyExists)
	 */
	public static long addUser(String user, String nick, String pw, String mail) {
		String rURL = bURL+"account/add?user="+user+"&nick="+nick+"&password="+pw+"&mail="+mail;
		
		return getRequest(rURL);
	}
	
	/**
	 * sends a request to the server in order ot log a user in
	 * @param user - Username (<= 16 characters)
	 * @param pw   - Password (unhashed)
	 * 
	 * @return responseCode - (-1->JAVA Error, 100->noError, 404->dataMissing, 800->MySQLError,
	 */
	public static long loginAttempt(String user, String pw) {
		String rURL = bURL+"account/login?user="+user+"&password="+pw;
		
		return getRequest(rURL);
	}
	
	/**
	 * general function to send a request to the server
	 * @param url - URL String with GET parameters
	 * @return errorCode
	 */
	private static long getRequest(String url) {
		try {
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();

			// optional default is GET
			con.setRequestMethod("GET");

			//add request header
			con.setRequestProperty("User-Agent", USER_AGENT);

			int responseCode = con.getResponseCode();
			System.out.println("\nSending 'GET' request to URL : " + url);
			System.out.println("Response Code : " + responseCode);

			BufferedReader in = new BufferedReader(
			        new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

			//print result
			String r = response.toString();
			
			JSONParser parser = new JSONParser();
			JSONObject o = (JSONObject) parser.parse(r);
			
			if(o.containsKey("response"))
				return (long) o.get("response");
			else
				return -1;
		}
		catch(Exception e) {
			System.out.println(e.getMessage());
			
			return -1;
		}
	}
	
	/**
	 * Spielwiese zum ausprobieren
	 * @param args
	 */
	public static void main(String[] args) {
		
	}
}
