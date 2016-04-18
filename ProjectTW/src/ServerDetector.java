import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.TimerTask;
import java.util.TreeMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Simple demo that uses java.util.Timer to schedule a task to execute once 5
 * seconds have passed.
 */

public class ServerDetector extends TimerTask {

	public ServerStatus status;
	public TreeMap<Integer, ArrayList<String>> servers;
	private final MultiReaderLock lock;
	public ServerDetector(ServerStatus status) {

		this.status = status;
		this.servers = new TreeMap<Integer, ArrayList<String>>();
		this.lock = new MultiReaderLock();
	}

	@SuppressWarnings("unchecked")
	public void run() {
		// System.out.format("send request");
		getAllServers();
		for (Integer id : this.servers.keySet()) {

			if (this.status.getId() != id) {

				String ip = this.servers.get(id).get(0);
				String port = this.servers.get(id).get(1);
				JSONArray ipport = new JSONArray();
				ipport.add(ip);
				ipport.add(port);

				String response = sendMessage(ipport.toString(), "/hello");

				if (!response.equals("ok")) {

					System.out.println("One Server is down!");
					response = removeServer(ipport);
				}

				if (response.equals("down")) {

					System.out
							.println("The Server has been removed from discovery server.");
					this.servers.remove(id);
					break;

				}

			}
		}
		 }


	private String removeServer(JSONArray requestbody) {

		Socket socket = null;
		BufferedWriter wr = null;
		BufferedReader reader = null;
		String response = "";

		try {
			socket = new Socket(this.status.discoveryIp,Integer.parseInt(this.status.discoveryPort));
			String path = "/removeServer";

			wr = new BufferedWriter(new OutputStreamWriter(
					socket.getOutputStream(), "UTF8"));
			wr.write("GET " + path + " HTTP/1.0\r\n");
			wr.write("Content-Length: " + requestbody.toString().length()
					+ "\r\n");
			wr.write("Content-Type: application/x-www-form-urlencoded\r\n");
			wr.write("\r\n");
			wr.write(requestbody.toString());
			System.out.println(requestbody.toString());
			wr.write("\n");
			wr.flush();
			reader = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));

			String line = "";

			line = reader.readLine();
			System.out.println(line);
			if (line.equals("HTTP/1.1 200 OK")) {

				response = "down";

			}

			reader.close();
			wr.close();
			socket.close();
		} catch (UnknownHostException e) {
			System.err.println("UnknownHost Error");
		} catch (IOException e) {
			System.out.println("Unexpected IO Error");
		}

		return response;
	}

	private String sendMessage(String serverInfo, String path) {

		Socket socket = null;
		BufferedWriter wr = null;
		BufferedReader reader = null;
		String response = "";
		JSONParser parse = new JSONParser();
		JSONArray primaryObj = new JSONArray();

		try {

			primaryObj = (JSONArray) parse.parse(serverInfo);
			String ip = (String) primaryObj.get(0);
			String port = (String) primaryObj.get(1);

			System.out.println("Send to " + ip + ":" + port);

			socket = new Socket(ip, Integer.parseInt(port));
			wr = new BufferedWriter(new OutputStreamWriter(
					socket.getOutputStream(), "UTF8"));
			wr.write("GET " + path + " HTTP/1.0\r\n");
			wr.write("Content-Length: " + 0 + "\r\n");
			wr.write("Content-Type: application/x-www-form-urlencoded\r\n");
			wr.write("\r\n");
			wr.flush();

			reader = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));

			response = getResponse(reader);
			// System.out.println(response);

			reader.close();
			wr.close();
			socket.close();

		} catch (UnknownHostException e) {
			System.err.println("UnknownHost Error");
		} catch (IOException e) {
			System.out.println("Unexpected IO Error");
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return response;
	}

	/**
	 * Read lines can catch the response body
	 * 
	 * @param reader
	 * @return response
	 */
	private String getResponse(BufferedReader reader) {
		String response = "";
		String line = "";
		try {
			line = reader.readLine();
			 
			if (line.equals("HTTP/1.1 200 OK")) {

				response = "ok";

			}  
			
		} catch (IOException e) {
			System.out.println("Unexpected IO Error");
		} catch (NullPointerException e) {
			
			System.out.println("Connection ERROR!");
		}

		System.out.println(response);
		return response;
	}

	private void getAllServers() {

		Socket socket = null;
		BufferedWriter wr = null;
		BufferedReader reader = null;

		try {

			socket = new Socket(this.status.discoveryIp,Integer.parseInt(this.status.discoveryPort));
			String path = "/getAll";
			wr = new BufferedWriter(new OutputStreamWriter(
					socket.getOutputStream(), "UTF8"));
			wr.write("GET " + path + " HTTP/1.0\r\n");
			wr.write("Content-Length: " + 0 + "\r\n");
			wr.write("Content-Type: application/x-www-form-urlencoded\r\n");
			wr.write("\r\n");
			wr.flush();
			reader = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));

			String line = "";
			String response = "";
			line = reader.readLine();

			if (line.equals("HTTP/1.1 200 OK")) {

				while (line != null) {

					if (line.startsWith("{\"")) {
						response = line;
					}
					line = reader.readLine();
				}
			}

			JSONParser parse = new JSONParser();
			JSONObject serverobj = (JSONObject) parse.parse(response);
			this.lock.lockWrite();
			for (Object idobj : serverobj.keySet()) {

				Integer id = Integer.parseInt(idobj.toString());
				if(id != this.status.getId()){
				JSONArray ipport = (JSONArray) serverobj.get(idobj);
				String ip = ipport.get(0).toString();
				String port = ipport.get(1).toString();

				ArrayList<String> servers = new ArrayList<String>();
				servers.add(ip);
				servers.add(port);
				this.servers.put(id, servers);
				}
			}
			this.lock.unlockWrite();
			reader.close();
			wr.close();
			socket.close();
		} catch (UnknownHostException e) {
			System.err.println("UnknownHost Error");
		} catch (IOException e) {
			System.out.println("Unexpected IO Error");
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}