import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * This class will create a BackEnd socket to handle any valid requests and
 * create a response to FrontEnd Server.
 * 
 * @author miaoguo
 * 
 */
public class DataSocket implements Runnable {

	private Socket socket;
	private Twitter twitter;
	private ServerStatus status;
	private final MultiReaderLock lock;

	public DataSocket(Socket socket, Twitter twitter, ServerStatus status) {
		this.socket = socket;
		this.twitter = twitter;
		this.status = status;
		this.lock = new MultiReaderLock();

	}

	/**
	 * The data server should handle concurrent requests to access and/or update
	 * data.
	 */
	@SuppressWarnings({ "unchecked", "static-access" })
	public void run() {
		System.out.println("Handling a new Request.");
		BufferedReader reader = null;
		OutputStream output = null;
		HTTPRequestLineParser httpparse = new HTTPRequestLineParser();
		try {
			reader = new BufferedReader(new InputStreamReader(
					this.socket.getInputStream()));
			output = this.socket.getOutputStream();

			String line = reader.readLine();
			// System.out.println(line+"!@!@!");
			HTTPRequestLine requestline = httpparse.parse(line);
			String path = requestline.getPath();

			if (requestline.getMethod().equals("POST")
					&& requestline.getPath().equals("/tweets")) {

				String request = null;
				boolean hasPost = false;

				while ((line = reader.readLine()) != null) {

					if (line.startsWith("{")) {
						request = line;

						twitter.addTweet(request);
						System.out.println("add" + request + "to server");

						hasPost = true;

						// write 201 Created for valid request
						String responseheaders = "HTTP/1.1 201 Valid" + "\n\n";

						output.write(responseheaders.getBytes());

						// replicated on all data server
						this.status.updateVersion();
						this.status.timestamp.put(this.status.getId(),
								this.status.getVersion());
						this.status.saveLog(request, this.status.getId(),
								this.status.getVersion());
						this.status.addServer();
						this.status.updateAll(this.status.getId(),
								this.status.getVersion());

					}

				}

				if (hasPost == false) {

					// write 400 Created for Bad request
					String responseheaders = "HTTP/1.1 400 Bad" + "\n\n";

					output.write(responseheaders.getBytes());
				}

			} else if (requestline.getMethod().equals("POST")
					&& requestline.getPath().equals("/updatetweets")) {
				 
				JSONParser parser = new JSONParser();

				boolean hasPost = false;

				while ((line = reader.readLine()) != null) {

					if (line.startsWith("[")) {
						// System.out.println(line+"updatelog");
						JSONArray request = (JSONArray) parser.parse(line);
						int id = Integer.parseInt(request.get(0).toString());
						int version = Integer.parseInt(request.get(1)
								.toString());
						JSONObject logobj = (JSONObject) request.get(2);
						if (logobj.keySet().size() > 0) {
							for (Object logversion : logobj.keySet()) {

								String log = logobj.get(logversion).toString();

								this.twitter.addTweet(log);
								this.status
										.saveLog(log, id,
												Integer.parseInt(logversion
														.toString()));

							}

							this.status.timestamp.put(id, version);
							this.status.updateAll(id, version);

							hasPost = true;

						}
					}

				}

				if (hasPost) {

					// write 400 Created for Bad request
					String responseheaders = "HTTP/1.1 200 ok" + "\n\n";

					output.write(responseheaders.getBytes());
					reader.close();
					output.flush();
					output.close();
					this.socket.close();
				}

			} else if (requestline.getMethod().equals("GET")
					&& path.startsWith("/tweets")) {

				// Search Data

				String query = requestline.getParameters().get("q");
				Integer version = Integer.parseInt(requestline.getParameters()
						.get("v"));
				if (twitter.hasTweet(query)) {
					if (twitter.outVersion(query, version)) {

						JSONObject response = new JSONObject();
						JSONArray tweets = new JSONArray();
						tweets = twitter.getTweet(query);
						System.out.println(tweets);
						Integer ver = twitter.getVersion(query);

						this.lock.lockWrite();
						response.put("q", query);
						response.put("v", ver);
						response.put("tweets", tweets);
						this.lock.unlockWrite();

						String responseheaders = "HTTP/1.1 200 OK\n"
								+ "Content-Length: "
								+ response.toString().getBytes().length
								+ "\n\n";

						output.write(responseheaders.getBytes());
						output.write(response.toString().getBytes());
					} else {

						String responseheaders = "HTTP/1.1 304 Not Modified"
								+ "\n\n";
						output.write(responseheaders.getBytes());

					}
				} else {

					JSONObject response = new JSONObject();
					JSONArray tweets = new JSONArray();
					this.lock.lockWrite();
					response.put("q", query);
					response.put("v", 0);
					response.put("tweets", tweets);
					this.lock.unlockWrite();
					String responseheaders = "HTTP/1.1 200 OK\n"
							+ "Content-Length: "
							+ response.toString().getBytes().length + "\n\n";

					output.write(responseheaders.getBytes());
					output.write((response.toString() + "\n").getBytes());

				}

			} else if (requestline.getMethod().equals("GET")
					&& path.startsWith("/allLog")) {
				JSONParser parser = new JSONParser();
				JSONObject logmap = new JSONObject();
				 
				while ((line = reader.readLine()) != null) {
					 
					if (line.startsWith("[")) {
						 
						JSONArray idObj = (JSONArray) parser.parse(line);
						int id = Integer.parseInt(idObj.get(0).toString());
						 
						if (!this.status.timestamp.containsKey(id)) {

							this.status.timestamp.put(id, 0);
							

						}
						System.out.println(this.status.timestamp);
						 
						
						for (Integer serverid : this.status.logmap.keySet()) {

							JSONObject loginfo = new JSONObject();

							for (Integer version : this.status.logmap.get(
									serverid).keySet()) {

								loginfo.put(
										version,
										this.status.logmap.get(serverid).get(
												version));

							}
							logmap.put(serverid, loginfo);
						}
						
						 
						String responseheaders = "HTTP/1.1 200 OK\n"
								+ "Content-Length: "
								+ logmap.toString().getBytes().length + "\n";

						output.write(responseheaders.getBytes());
						output.write(logmap.toString().getBytes());
						 
						output.flush();
						output.close();
						 
					}
					
				}
				
				//reader.close();
				
				//this.socket.close();

			} else if (requestline.getMethod().equals("GET")
					&& path.startsWith("/hello")) {

				String response = "ok\n";
				String responseheaders = "HTTP/1.1 200 OK\n"
						+ "Content-Length: " + response.getBytes().length
						+ "\n\n";

				output.write(responseheaders.getBytes());
				output.write(response.getBytes());

			}   else if (requestline.getMethod().equals("GET")
					&& path.startsWith("/timestamp")) {

				String request = "";

				while ((line = reader.readLine()) != null) {
					  
					if (line.startsWith("[")) {
						request = line;
						 
						JSONParser parse = new JSONParser();
						JSONArray idobj = (JSONArray) parse.parse(request);

						Integer id = Integer.parseInt(idobj.get(0).toString());
						int version = this.status.timestamp.get(id);
						JSONArray response = new JSONArray();
						response.add(version);
						String responseheaders = "HTTP/1.1 200 OK\n"
								+ "Content-Length: "
								+ response.toString().getBytes().length
								+ "\n\n";
						 
						output.write(responseheaders.getBytes());
						output.write((response.toString() + "\n").getBytes());
						output.flush();
						output.close();
						reader.close();
						this.socket.close();
						 
					}
				}
			} else if (requestline.getMethod().equals("GET")
					&& path.startsWith("/getstamp")) {

				JSONObject obj = new JSONObject();
				for (Integer id : this.status.timestamp.keySet()) {

					obj.put(id, this.status.timestamp.get(id));
				}
				System.out.println("timestamp is :" + obj.toString());
				String responseheaders = "HTTP/1.1 200 OK\n"
						+ "Content-Length: " + obj.toString().getBytes().length
						+ "\n\n";

				output.write(responseheaders.getBytes());
				output.write((obj.toString()+"\n").getBytes());
				 

			}
			reader.close();
			output.flush();
			output.close();
			this.socket.close();
		} catch (IOException | ParseException e) {
			System.err.println("IO error");
		} catch (NullPointerException e) {

			System.err.println("Unexpected NullPointerException");
		}

	}

}
