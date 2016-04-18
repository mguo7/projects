import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class ServerStatus {

	private int id;
	private String port;
	private int version;
	private String ip;
	private final MultiReaderLock lock;
	public String discoveryIp;
	public String discoveryPort;
	public HashMap<String,String>dataserver;
	//public HashMap<Integer, String> logInfo;
	public HashMap<Integer, HashMap<Integer, String>> logmap;
	public HashMap<Integer, Integer> timestamp;
	public Twitter twitter;

	public ServerStatus(int id, String port, String ip, int version,
			Twitter twitter) {

		this.id = id;
		this.port = port;
		this.version = version;
		this.ip = ip;
		//this.logInfo = new HashMap<Integer, String>();
		this.twitter = twitter;
		this.lock = new MultiReaderLock();
		this.timestamp = new HashMap<Integer, Integer>();
		this.timestamp.put(id, 0);
		this.logmap = new HashMap<Integer, HashMap<Integer, String>>();
		this.dataserver = new HashMap<String,String>();

		// this.isavaliable = true;
	}

	public int getId() {

		return this.id;
	}

	public String getIP() {

		return this.ip;
	}

	public String getPort() {

		return this.port;
	}

	public int getVersion() {

		return this.version;
	}

	public void updateVersion() {

		this.version += 1;
	}

	public void setVersion(int version) {

		this.version = version;

	}

	public void saveLog(String request, int sid, int version) {
		HashMap<Integer, String> Info = new HashMap<Integer, String>();
		this.lock.lockWrite();
		if(this.logmap.containsKey(sid)){
			
			Info = this.logmap.get(sid);
			Info.put(version, request);
			this.logmap.put(sid, Info);
		}else{
			Info.put(version, request);
			this.logmap.put(sid, Info);
		}
		this.lock.unlockWrite();
		 
	}

	 

	public void updateAll(int id, int version) {

		 

		HashMap<Integer, ArrayList<String>> servers = new HashMap<Integer, ArrayList<String>>();
		servers = getAllServers();
		 
		 
		for (Integer serverid : servers.keySet()) {
			if (serverid != this.id) {
				String ip = servers.get(serverid).get(0);
				String port = servers.get(serverid).get(1);
				 
				String response = getVersion(ip, port, id);
				 
				 
				if (!response.isEmpty()) {

					int timestampVer = Integer.parseInt(response);
					 
					if (version > timestampVer) {

						int diff = version - timestampVer;
						sendUpdate(ip, port, diff, id);

					}

				}
			}
		}

	}

	@SuppressWarnings("unchecked")
	private String getVersion(String ip, String port, int id) {

		Socket socket = null;
		BufferedWriter wr = null;
		BufferedReader reader = null;
		String response = "";
		 
		try {

			JSONArray request = new JSONArray();
			request.add(id);

			socket = new Socket(ip, Integer.parseInt(port));
			String path = "/timestamp";
			wr = new BufferedWriter(new OutputStreamWriter(
					socket.getOutputStream(), "UTF8"));
			wr.write("GET " + path + " HTTP/1.0\r\n");
			wr.write("Content-Length: " + request.toString().length() + "\r\n");
			wr.write("Content-Type: application/x-www-form-urlencoded\r\n");
			wr.write("\r\n");
			wr.write(request.toString());
			wr.write("\n");
			wr.flush();
			reader = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));

			String line = "";
			if ((line = reader.readLine()) != null) {

				if (line.equals("HTTP/1.1 200 OK")) {
					 
					while (line != null) {

						if (line.startsWith("[")) {
							JSONParser parse = new JSONParser();
							JSONArray arrayobj = (JSONArray) parse.parse(line);
							response = arrayobj.get(0).toString();
						}
						line = reader.readLine();
					}
				}
			}
			reader.close();
			wr.close();
			socket.close();
		} catch (UnknownHostException e) {
			System.err.println("UnknownHost Error");
		} catch (IOException e) {
			System.err.println("Unexpected IO Error");
		} catch (ParseException e) {
			System.err.println("Parse Error");

		}
		 
		return response;
	}

	@SuppressWarnings("unchecked")
	private void sendUpdate(String ip, String port, int diff, int sid) {

		Socket socket = null;
		BufferedWriter wr = null;
		BufferedReader reader = null;
		//System.out.println(this.logmap+" : "+this.id);
		try {
			// mc05.cs.usfca.edu

			JSONArray request = new JSONArray();
			JSONObject jsonobj = new JSONObject();
			for (int i = 0; i < diff; i++) {

				jsonobj.put(this.logmap.get(sid).keySet().size() - i, this.logmap.get(sid)
						.get(this.logmap.get(sid).keySet().size() - i));

			}
			request.add(sid);
			request.add(this.timestamp.get(sid));
			request.add(jsonobj);
			System.out.println("Update"+request.toString());
			socket = new Socket(ip, Integer.parseInt(port));
			String path = "/updatetweets";
			wr = new BufferedWriter(new OutputStreamWriter(
					socket.getOutputStream(), "UTF8"));
			wr.write("POST " + path + " HTTP/1.0\r\n");
			wr.write("Content-Length: " + request.toString().length() + "\r\n");
			wr.write("Content-Type: application/x-www-form-urlencoded\r\n");
			wr.write("\r\n");
			//System.out.println(request.toString() + "SEND A REQUEST!");
			wr.write(request.toString());
			wr.write("\n");
			wr.flush();

			reader = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));

			reader.close();
			wr.close();
			socket.close();

		} catch (UnknownHostException e) {
			System.err.println("UnknownHost Error");
		} catch (IOException e) {
			System.out.println("Unexpected IO Error");
		}

	}

	@SuppressWarnings("unchecked")
	private void sendAll(Map<Integer, ArrayList<String>> servermap) {

		Socket socket = null;
		BufferedWriter wr = null;
		BufferedReader reader = null;

		 

		try {
			// mc05.cs.usfca.edu

			for (Integer sid : servermap.keySet()) {

				String ip = servermap.get(sid).get(0);
				String port = servermap.get(sid).get(1);
				Integer version = Integer.parseInt(servermap.get(sid).get(2)
						.toString());
				int diff = this.version - version;

				JSONObject request = new JSONObject();
				for (int i = 0; i < diff; i++) {

					request.put(this.version - i,
							this.logmap.get(sid).get(this.version - i));

				}

				socket = new Socket(ip, Integer.parseInt(port));
				String path = "/updatetweets";
				wr = new BufferedWriter(new OutputStreamWriter(
						socket.getOutputStream(), "UTF8"));
				wr.write("POST " + path + " HTTP/1.0\r\n");
				wr.write("Content-Length: " + request.toString().length()
						+ "\r\n");
				wr.write("Content-Type: application/x-www-form-urlencoded\r\n");
				wr.write("\r\n");
				System.out.println(request.toString() + "SEND A REQUEST!");
				wr.write(request.toString());
				wr.write("\n");
				wr.flush();

				reader = new BufferedReader(new InputStreamReader(
						socket.getInputStream()));

				reader.close();
				wr.close();
				socket.close();

			}
			//this.logInfo.clear();
		} catch (UnknownHostException e) {
			System.err.println("UnknownHost Error");
		} catch (IOException e) {
			System.out.println("Unexpected IO Error");
		}

	}

	@SuppressWarnings({ "unchecked" })
	public String addServer() {

		JSONObject obj = new JSONObject();
		obj.put("ip", this.ip);
		obj.put("id", this.id);
		obj.put("port", this.port);
		obj.put("version", this.version);

		String requestbody = obj.toJSONString();

		Socket socket = null;
		BufferedWriter wr = null;
		BufferedReader reader = null;
		String response = "";
		try {
			// mc05.cs.usfca.edu
			socket = new Socket(this.discoveryIp,
					Integer.parseInt(this.discoveryPort));
			String path = "/addme";
			wr = new BufferedWriter(new OutputStreamWriter(
					socket.getOutputStream(), "UTF8"));
			wr.write("GET " + path + " HTTP/1.0\r\n");
			wr.write("Content-Length: " + requestbody.length() + "\r\n");
			wr.write("Content-Type: application/x-www-form-urlencoded\r\n");
			wr.write("\r\n");
			wr.write(requestbody);
			wr.write("\n");
			System.out.println(requestbody);
			wr.flush();
			reader = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));

			response = getResponse(reader);
			System.out.println(response);
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
				// for the post's response
			} else if (line.equals("HTTP/1.1 400 Bad")) {

				response = "fail";

			} else if (line.equals("HTTP/1.1 404 Not Found")) {

				response = "not found";
			}

			reader.close();

		} catch (IOException e) {
			System.out.println("Unexpected IO Error");
		}

		return response;
	}

	 

	public void getTweets() {

		HashMap<Integer, ArrayList<String>> servers = new HashMap<Integer, ArrayList<String>>();
		servers = getAllServers();
		 

		try {
			if (servers.size() > 0) {
				for (Integer serverid : servers.keySet()) {

					String ip = servers.get(serverid).get(0);
					String port = servers.get(serverid).get(1);
					this.timestamp.put(serverid, 0);
					String response = getLogmap(ip, port);

					if (!response.isEmpty()) {

						JSONParser parser = new JSONParser();
						JSONObject logmap = (JSONObject) parser.parse(response);

						if (logmap.size() > 0) {
							System.out.println(logmap.toString());
							for (Object idobj : logmap.keySet()) {

								JSONObject logInfo = new JSONObject();
								logInfo = (JSONObject) logmap.get(idobj);
								int sid = Integer.parseInt(idobj.toString());
								if (logInfo.size() > 0) {
									for (Object logversion : logInfo.keySet()) {

										String request = logInfo
												.get(logversion).toString();
										this.twitter.addTweet(request);
										int version = Integer
												.parseInt(logversion.toString());
										saveLog(request, sid, version);

									}

								}
								//this.timestamp.put(sid, logInfo.keySet().size());
								updateAll(sid, logInfo.keySet().size());
							}
						}  

					}

				}
				
				for(Integer serverid: this.logmap.keySet()){
					
					this.timestamp.put(serverid, this.logmap.get(serverid).size());
					 
				}

			}

		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("Twitter: "+ this.twitter.twitter.toString());
		System.out.println("TimeStamp: "+ this.timestamp);

	}

	@SuppressWarnings("unchecked")
	private String getLogmap(String ip, String port) {

		Socket socket = null;
		BufferedWriter wr = null;
		BufferedReader reader = null;
		String response = "";

		try {
			// mc05.cs.usfca.edu

			JSONArray request = new JSONArray();
			request.add(this.id);
			 
			socket = new Socket(ip, Integer.parseInt(port));
			String path = "/allLog";
			wr = new BufferedWriter(new OutputStreamWriter(
					socket.getOutputStream(), "UTF8"));
			wr.write("GET " + path + " HTTP/1.0\r\n");
			wr.write("Content-Length: " + request.toString().length() + "\r\n");
			wr.write("Content-Type: application/x-www-form-urlencoded\r\n");
			wr.write("\r\n");
			wr.write(request.toString());
			wr.write("\n");
			wr.flush();
			 
			reader = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));

			String line = "";
			line = reader.readLine();

			if (line.equals("HTTP/1.1 200 OK")) {

				while (line != null) {
					 
				 
					if (line.startsWith("{")) {
						response = line;
						 
					}
					line = reader.readLine();
				}
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

	private HashMap<Integer, ArrayList<String>> getAllServers() {

		Socket socket = null;
		BufferedWriter wr = null;
		BufferedReader reader = null;
		HashMap<Integer, ArrayList<String>> servers = new HashMap<Integer, ArrayList<String>>();
		try {

			socket = new Socket(this.discoveryIp,
					Integer.parseInt(this.discoveryPort));
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
						 
						JSONParser parse = new JSONParser();
						JSONObject serverobj = (JSONObject) parse.parse(response);
						this.lock.lockWrite();
						if (serverobj.size() > 0) {
							for (Object idobj : serverobj.keySet()) {

								Integer id = Integer.parseInt(idobj.toString());
								if (id != this.getId()) {
									JSONArray ipport = (JSONArray) serverobj.get(idobj);
									String ip = ipport.get(0).toString();
									String port = ipport.get(1).toString();

									ArrayList<String> address = new ArrayList<String>();
									address.add(ip);
									address.add(port);
									servers.put(id, address);
								}
							}
						}
						this.lock.unlockWrite();
					}
					line = reader.readLine();
				}
			}

			
			
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

		return servers;

	}

}
