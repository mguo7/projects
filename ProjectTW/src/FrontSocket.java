import java.io.BufferedWriter;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * This class will create a socket which will receive Get requests from users.
 * 
 * @author miaoguo
 * 
 */
public class FrontSocket implements Runnable {

	private Socket socket;
	private CacheHandler cache;
	
	private HashMap<Integer, Integer> timestamp;
	private ServerStatus status;
	private final MultiReaderLock lock;

	// private static Logger logger = LogManager.getLogger(FrontSocket.class);

	public FrontSocket(Socket socket, CacheHandler cache,ServerStatus status) {
		this.socket = socket;
		this.cache = cache;
		
		this.timestamp = new HashMap<Integer, Integer>();
		this.status = status;
		this.lock = new MultiReaderLock();
		// logger.debug("A new Scoket has been created at FE");
	}

	/**
	 * Only Get request can be handled by the application. Otherwise, the server
	 * will send a 400 bad response.
	 */
	@SuppressWarnings({ "static-access", "unchecked" })
	public void run() {

		BufferedReader reader = null;
		OutputStream output = null;
		HTTPRequestLineParser httpparse = new HTTPRequestLineParser();

		try {

			reader = new BufferedReader(new InputStreamReader(
					this.socket.getInputStream()));

			output = this.socket.getOutputStream();

			String line = reader.readLine();
			HTTPRequestLine requestline = httpparse.parse(line);

			// do Post

			if (requestline.getMethod().equals("POST")) {

				if (requestline.getPath().equals("/tweets")) {

					String tweet = "";
					Integer charnum = 0;
					while (!line.isEmpty()) {

						line = reader.readLine();
						if (line.startsWith("Content-Length")) {

							String[] contentLen = line.split("\\s+");
							charnum = Integer.parseInt(contentLen[1]);
						}
					}

					char[] content = new char[charnum];
					reader.read(content);
					tweet = parseJson(new String(content));
					// System.out.println(tweet);

					ArrayList<String> hashtags = new ArrayList<String>();
					HashtagParser parse = new HashtagParser();
					hashtags = parse.getTags(tweet);

					if (hashtags.size() == 0) {

						String responsebody = "Bad request!\n";
						String responseheaders = "HTTP/1.1 400 Bad\n"
								+ "Content-Length: "
								+ responsebody.toString().getBytes().length
								+ "\n\n";

						output.write(responseheaders.getBytes());
						output.write(responsebody.getBytes());

					} else {

						// Post request
						// this.lock.lockWrite();
						String response = postTweet(tweet, hashtags);
						// this.lock.unlockWrite();

						if (response.equals("BAD")) {

							String responsebody = "BAD request!";
							String responseheaders = "HTTP/1.1 400 Bad\n"
									+ "Content-Length: "
									+ responsebody.getBytes().length + "\n\n";

							output.write(responseheaders.getBytes());
							output.write(responsebody.getBytes());

						} else if (response.equals("Valid")) {

							// Create 201 valid response
							String responsebody = "Valid request!";
							String responseheaders = "HTTP/1.1 201 Valid\n"
									+ "Content-Length: "
									+ responsebody.getBytes().length + "\n\n";

							output.write(responseheaders.getBytes());
							output.write(responsebody.getBytes());

						}

					}

				} else {

					String responsebody = "Not found!";
					String responseheaders = "HTTP/1.1 404 Not Found\n"
							+ "Content-Length: "
							+ responsebody.getBytes().length + "\n\n";

					output.write(responseheaders.getBytes());
					output.write(responsebody.getBytes());

				}

				// do Get

			} else if (requestline.getMethod().equals("GET")) {
				String query = "";

				if (requestline.getParameters().containsKey("q")) {
					query = requestline.getParameters().get("q");
				}

				if (!query.isEmpty() && !query.contains(" ")) {

					String response = "";

					if (this.cache.hasTag(query)) {
						Integer Version = this.cache.getVersion(query);
						this.lock.lockRead();
						response = searchTweet(query, Version);
						this.lock.unlockRead();
					} else {
						this.lock.lockRead();
						response = searchTweet(query, 0);
						this.lock.unlockRead();
					}

					if (response.isEmpty()) {

						JSONArray tweets = this.cache.getTweets(query);
						JSONObject responsebody = new JSONObject();

						this.lock.lockWrite();
						responsebody.put("q", query);
						responsebody.put("tweets", tweets);
						this.lock.unlockWrite();

						String responseheaders = "HTTP/1.1 200 OK\n"
								+ "Content-Length: "
								+ responsebody.toString().getBytes().length
								+ "\n\n";

						output.write(responseheaders.getBytes());
						output.write(responsebody.toString().getBytes());

						System.out.println(responsebody);
						// if response is not empty
					} else if (response.equals("BAD")) {

						String responsebody = "BAD request!";
						String responseheaders = "HTTP/1.1 400 Bad\n"
								+ "Content-Length: "
								+ responsebody.getBytes().length + "\n\n";

						output.write(responseheaders.getBytes());
						output.write(responsebody.getBytes());

					} else {

						JSONParser jsonParser = new JSONParser();
						JSONObject jsonTweet;
						try {
							jsonTweet = (JSONObject) jsonParser.parse(response);
							Integer version = Integer.parseInt(jsonTweet.get(
									"v").toString());
							JSONArray tweets = (JSONArray) jsonTweet
									.get("tweets");

							this.lock.lockWrite();
							JSONObject responsebody = new JSONObject();
							responsebody.put("q", query);
							responsebody.put("tweets", tweets);
							cache.updateVersion(query, version, tweets);
							this.lock.unlockWrite();

							String responseheaders = "HTTP/1.1 200 OK\n"
									+ "Content-Length: "
									+ responsebody.toString().getBytes().length
									+ "\n\n";

							output.write(responseheaders.getBytes());
							output.write((responsebody.toString() + "\n")
									.getBytes());

							System.out.println(responsebody);
						} catch (ParseException e) {
							System.err.println("Can not parse the query!");
						}

					}

					// if query is empty or contains space chars:
				} else if (query.isEmpty() || query.contains(" ")) {

					String responsebody = "Bad request!";
					String responseheaders = "HTTP/1.1 400 Bad\n"
							+ "Content-Length: "
							+ responsebody.getBytes().length + "\n\n";

					output.write(responseheaders.getBytes());
					output.write(responsebody.getBytes());

				}

			}
			reader.close();
			output.flush();
			output.close();
			this.socket.close();
		} catch (IOException e) {
			System.err.println("IO error");
		}

		// logger.debug("A Scoket has finished work at FE");
	}

	/**
	 * Get tweet from a request
	 * 
	 * @param text
	 *            of tweet from a request
	 * @return tweet
	 */
	private String parseJson(String text) {

		String tweet = "";

		JSONParser jsonParser = new JSONParser();

		JSONObject jsonObject;
		try {
			jsonObject = (JSONObject) jsonParser.parse(text);
			tweet = (String) jsonObject.get("text");
		} catch (ParseException e) {
			System.err.println("Can not parse the tweet!");
		}

		return tweet;

	}

	private String findServer() {

		Socket socket = null;
		BufferedWriter wr = null;
		BufferedReader reader = null;
		String response = "";
		try {
			socket = new Socket(this.status.discoveryIp,Integer.parseInt(this.status.discoveryPort));
			String path = "/getServer";
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

			line = reader.readLine();
			System.out.println(line);
			if (line.equals("HTTP/1.1 200 OK")) {

				while (line != null) {

					if (line.startsWith("[\"")) {
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

	/**
	 * Send a Post request to the BackEnd Server
	 * 
	 * @param tweet
	 * @param hashtags
	 *            of a list of hastags
	 */
	@SuppressWarnings({ "unchecked" })
	private String postTweet(String tweet, ArrayList<String> hashtags) {
		System.out.println("Current BackEnd is: "+this.status.dataserver );
		JSONObject obj = new JSONObject();
		obj.put("tweet", tweet);

		JSONArray list = new JSONArray();
		for (String tag : hashtags) {

			list.add(tag);
		}

		obj.put("hashtags", list);

		String requestbody = obj.toJSONString();

		Socket socket = null;
		BufferedWriter wr = null;
		BufferedReader reader = null;
		String response = "";
		JSONParser parse = new JSONParser();
		JSONArray serverObj = new JSONArray();

		try {
			// mc05.cs.usfca.edu

			String ip = "";
			String port = "";
			String hello = "";

			if (!this.status.dataserver.keySet().isEmpty()) {

				ip = this.status.dataserver.get("ip");
				port = this.status.dataserver.get("port");
				hello = sendMessage(ip, port);
				 
			}

			if (hello.isEmpty()) {
				this.status.dataserver.clear();
				String serverInfo = findServer();
				System.out.println("Current BackEnd is: "+serverInfo );
				int counter = 0;
				while (serverInfo.isEmpty() && counter < 10) {
					System.out.println("Trying to locate BackEnd Server...");
					serverInfo = findServer();
					counter++;
					Thread.sleep(500);
				}
				if (serverInfo.isEmpty()) {

					return "BAD";
				} else {
					System.out.println(serverInfo);
					serverObj = (JSONArray) parse.parse(serverInfo);
					ip = (String) serverObj.get(0);
					port = (String) serverObj.get(1);
					this.status.dataserver.put("ip", ip);
					this.status.dataserver.put("port", port);
				}
			}

			System.out.println("Send to " + ip + ":" + port);

			socket = new Socket(ip, Integer.parseInt(port));
			String path = "/tweets";
			wr = new BufferedWriter(new OutputStreamWriter(
					socket.getOutputStream(), "UTF8"));
			wr.write("POST " + path + " HTTP/1.0\r\n");
			wr.write("Content-Length: " + requestbody.length() + "\r\n");
			wr.write("Content-Type: application/x-www-form-urlencoded\r\n");
			wr.write("\r\n");
			wr.write(requestbody);
			wr.write("\n");
			wr.flush();
			System.out.println(requestbody);
			reader = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));

			response = getResponse(reader);

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
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return response;
	}

	/**
	 * Send a Get request to BackEnd Server
	 * 
	 * @param searchterm
	 * @param version
	 * @return response from BackEnd Server
	 */
	private String searchTweet(String searchterm, Integer version) {
		 
		Socket socket = null;
		BufferedWriter wr = null;
		BufferedReader reader = null;
		String response = "";
		JSONParser parse = new JSONParser();
		JSONArray serverObj = new JSONArray();
		System.out.println("Current BackEnd is: "+this.status.dataserver );
		try {

			String ip = "";
			String port = "";
			String hello = "";

			if (!this.status.dataserver.keySet().isEmpty()) {
				
				ip = this.status.dataserver.get("ip");
				port = this.status.dataserver.get("port");
				 
				hello = sendMessage(ip, port);
				 

			}
			System.out.println("I am hello"+hello);
			if (hello.isEmpty()) {
				this.status.dataserver.clear();
				String serverInfo = findServer();
				System.out.println(serverInfo+"Info!");
				
				int counter = 0;
				while (serverInfo.isEmpty() && counter < 10) {
					System.out.println("Trying to locate BackEnd Server...");
					serverInfo = findServer();
					counter++;
					Thread.sleep(500);
				}
				if (serverInfo.isEmpty()) {

					return "BAD";
				} else {

					serverObj = (JSONArray) parse.parse(serverInfo);
					ip = (String) serverObj.get(0);
					port = (String) serverObj.get(1);
					this.status.dataserver.put("ip", ip);
					this.status.dataserver.put("port", port);
				}
			}

			boolean get = false;
			int counter = 0;
			String timestamp = "";
			JSONObject stampObj = new JSONObject();
			while (!get && counter < 10) {
				timestamp = getTimestamp(ip, port);
				if (timestamp.isEmpty()) {
					 
					return "BAD";
				} else {
					 
					stampObj = (JSONObject) parse.parse(timestamp);
					for (Object idObj : stampObj.keySet()) {
						get = false;
						int id = Integer.parseInt(idObj.toString());
						int ver = Integer.parseInt(stampObj.get(idObj)
								.toString());

						if (this.timestamp.containsKey(id)) {

							if (this.timestamp.get(id) > ver) {

								counter++;
								break;

							}

						}
						get = true;
					}

					if (counter == 10) {

						for (Object idObj : stampObj.keySet()) {

							int id = Integer.parseInt(idObj.toString());
							int ver = Integer.parseInt(stampObj.get(idObj)
									.toString());

							this.timestamp.put(id, ver);
						}
					}

				}

				Thread.sleep(200);
			}
			
			
			for (Object idObj : stampObj.keySet()) {

				int id = Integer.parseInt(idObj.toString());
				int ver = Integer.parseInt(stampObj.get(idObj)
						.toString());

				this.timestamp.put(id, ver);
			}
			
			System.out.println("Current BackEnd is: "+this.status.dataserver );
			System.out.println(this.timestamp+" FrontEen timestamp");
			System.out.println("Send to " + ip + ":" + port);

			socket = new Socket(ip, Integer.parseInt(port));
			String path = "/tweets?q=" + searchterm + "&v=" + version;
			 
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
			reader.close();
			wr.close();
			socket.close();

		} catch (UnknownHostException e) {
			System.err.println("UnknownHost Error");
		} catch (IOException e) {
			System.out.println("Unexpected IO Error: " + e);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
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

				while (line != null) {

					if (line.startsWith("{\"")) {
						response = line;
					}
					line = reader.readLine();
				}

			} else if (line.equals("HTTP/1.1 400 Bad")) {

				response = "BAD";

			} else if (line.equals("HTTP/1.1 201 Valid")) {

				response = "Valid";

			}

			reader.close();
		} catch (IOException e) {
			System.out.println("Unexpected IO Error");
		}

		System.out.println(response);
		return response;
	}

	private String sendMessage(String ip, String port) {

		String response = "";

		Socket socket = null;
		BufferedWriter wr = null;
		BufferedReader reader = null;

		try {

			socket = new Socket(ip, Integer.parseInt(port));
			String path = "/hello";
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
			if ((line = reader.readLine()) != null) {

				if (line.equals("HTTP/1.1 200 OK")) {

					response = "I'm alive";
				}
			}
			reader.close();
			wr.close();
			socket.close();
		} catch (UnknownHostException e) {
			System.err.println("UnknownHost Error");
			return response;
		} catch (IOException e) {
			System.out.println("Unexpected IO Error");
			return response;
		}

		return response;
	}

	private String getTimestamp(String ip, String port) {

		String response = "";
		 
		Socket socket = null;
		BufferedWriter wr = null;
		BufferedReader reader = null;

		try {

			socket = new Socket(ip, Integer.parseInt(port));
			String path = "/getstamp";
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
			if ((line = reader.readLine()) != null) {

				if (line.equals("HTTP/1.1 200 OK")) {

					while (line != null) {

						if (line.startsWith("{")) {
							System.out.println(line+"TIME!");
							response = line;
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
			System.out.println("Unexpected IO Error");
		}

		return response;
	}

}
