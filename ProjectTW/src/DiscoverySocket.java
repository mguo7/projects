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
public class DiscoverySocket implements Runnable {

	private Socket socket;
	private ServerHandler servers;
	//private final MultiReaderLock lock;
	 

	public DiscoverySocket(Socket socket, ServerHandler servers) {
		this.socket = socket;
		this.servers = servers;
		//this.lock = new MultiReaderLock();

	}

	/**
	 * The data server should handle concurrent requests to access and/or update
	 * data.
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
			// System.out.println(line);
			HTTPRequestLine requestline = httpparse.parse(line);
			// System.out.println(requestline.getPath());
			if (requestline.getMethod().equals("GET")
					&& requestline.getPath().equals("/addme")) {

				String response = "[]\n";
				while ((line = reader.readLine()) != null) {

					if (line.startsWith("{")) {

						servers.addServer(line);

						//System.out.println(response);
						// write 200 ok request
						String responseheaders = "HTTP/1.1 200 OK\n"
								+ "Content-Length: "
								+ response.toString().getBytes().length + "\n\n";
						output.write(responseheaders.getBytes());
						output.write(response.getBytes());

						reader.close();
						output.flush();
						output.close();
						this.socket.close();

						// this.socket.close();

					}

				}

				// inform primary ip:port to front-end
			} else if (requestline.getMethod().equals("GET")
					&& requestline.getPath().equals("/getServer")) {

				String response = "";
			 
				if (this.servers.servers.size()>0) {
					 
					int min = this.servers.balancer.get(this.servers.balancer.firstKey());
					String servermin = this.servers.balancer.firstKey();
					for(String server:this.servers.balancer.keySet()){
						if(min>this.servers.balancer.get(server)){
							
							min = this.servers.balancer.get(server);
							servermin = server;
						}
						
					}
					JSONArray ipport = new JSONArray();

					ipport.add(this.servers.servers.get(servermin).getIP());
					ipport.add(this.servers.servers.get(servermin).getPort());
					
					int user = this.servers.balancer.get(servermin);
					this.servers.balancer.put(servermin, user+1);
					 			
					
					// write 200 ok request
					String responseheaders = "HTTP/1.1 200 OK\n"
							+ "Content-Length: "
							+ response.toString().getBytes().length + "\n";
					output.write(responseheaders.getBytes());
					output.write(ipport.toString().getBytes());
				} else {

					String responseheaders = "HTTP/1.1 404 Not Found\n"
							+ "Content-Length: "
							+ response.toString().getBytes().length + "\n";
					output.write(responseheaders.getBytes());
					output.write(response.getBytes());

				}
				System.out.println(this.servers.balancer);
				reader.close();
				output.flush();
				output.close();
				this.socket.close();

			}  else if (requestline.getMethod().equals("GET")
					&& requestline.getPath().equals("/removeServer")) {
				String request = "";

				while ((line = reader.readLine()) != null) {
					// System.out.println(line);
					if (line.startsWith("[")) {

						request = line;
						JSONParser parse = new JSONParser();
						JSONArray serverInfo = (JSONArray) parse.parse(request);
						String ip = serverInfo.get(0).toString();
						String port = serverInfo.get(1).toString();
						String ipport = ip + ":" + port;

						this.servers.servers.remove(ipport);
						this.servers.balancer.remove(ipport);
						System.out.println(this.servers.servers);

						String responseheaders = "HTTP/1.1 200 OK\n"
								+ "Content-Length: " + 0 + "\n";
						output.write(responseheaders.getBytes());

						reader.close();
						output.flush();
						output.close();
						this.socket.close();

					}

				}

				if (request.equals("")) {

					String responseheaders = "HTTP/1.1 400 Bad\n"
							+ "Content-Length: " + 0 + "\n";
					output.write(responseheaders.getBytes());

					reader.close();
					output.flush();
					output.close();
					this.socket.close();

				}

			} else if (requestline.getMethod().equals("GET")
					&& requestline.getPath().equals("/getAll")) {

				JSONObject serverobj = new JSONObject();

				if (this.servers.servers.keySet().size() > 0) {
					for (String server : this.servers.servers.keySet()) {

						// JSONObject ipport = new JSONObject();
						JSONArray ipport = new JSONArray();

						ipport.add(this.servers.servers.get(server).getIP());
						ipport.add(this.servers.servers.get(server).getPort());

						serverobj.put(this.servers.servers.get(server).getId(),
								ipport);
					}

					String response = serverobj.toString() + "\n";
					System.out.println(response);
					// write 200 ok request
					String responseheaders = "HTTP/1.1 200 OK\n"
							+ "Content-Length: "
							+ response.toString().getBytes().length + "\n";
					output.write(responseheaders.getBytes());
					output.write(response.getBytes());
				} else {

					String responseheaders = "HTTP/1.1 400 Bad\n"
							+ "Content-Length: " + 0 + "\n";
					output.write(responseheaders.getBytes());

				}

				reader.close();
				output.flush();
				output.close();
				this.socket.close();

			} else if (requestline.getMethod().equals("GET")
					&& requestline.getPath().equals("/lowerversion")) {

				String request = "";
				JSONObject serverobj = new JSONObject();

				while ((line = reader.readLine()) != null) {
					// System.out.println(line);
					if (line.startsWith("[")) {

						request = line;
						JSONParser parse = new JSONParser();
						JSONArray verInfo = (JSONArray) parse.parse(request);
						String version = verInfo.get(0).toString();
						Integer ver = Integer.parseInt(version);
						for (String server : this.servers.servers.keySet()) {

							if (this.servers.servers.get(server).getVersion() < ver) {
								// System.out.println(ver);
								JSONArray ipport = new JSONArray();

								ipport.add(this.servers.servers.get(server)
										.getIP());
								ipport.add(this.servers.servers.get(server)
										.getPort());
								ipport.add(this.servers.servers.get(server)
										.getVersion());

								serverobj.put(this.servers.servers.get(server)
										.getId(), ipport);

							} 

						}

						String response = serverobj.toString() + "\n";
						System.out.println(response);
						// write 200 ok request
						String responseheaders = "HTTP/1.1 200 OK\n"
								+ "Content-Length: "
								+ response.toString().getBytes().length + "\n";
						output.write(responseheaders.getBytes());
						output.write(response.getBytes());

						reader.close();
						output.flush();
						output.close();
						this.socket.close();
					}
				}

			} else if (requestline.getMethod().equals("GET")
					&& requestline.getPath().equals("/latestversion")) {
				
				String request = "";
				JSONObject serverobj = new JSONObject();

				while ((line = reader.readLine()) != null) {
					// System.out.println(line);
					if (line.startsWith("[")) {

						request = line;
						JSONParser parse = new JSONParser();
						JSONArray priInfo = (JSONArray) parse.parse(request);
						String priversion = priInfo.get(0).toString();
						Integer priver = Integer.parseInt(priversion);
						int latest = priver;
						String latestip = "";
						String latestport = "";
						for (String server : this.servers.servers.keySet()) {

							if (this.servers.servers.get(server).getVersion() > latest) {
								// System.out.println(ver);
								 latest = this.servers.servers.get(server).getVersion();
								latestip = this.servers.servers.get(server).getIP();
								latestport = this.servers.servers.get(server).getPort();
							} 

						}

						if(latest == priver) {
							
							// write 404 not found
							
							String responseheaders = "HTTP/1.1 404 Not Found\n"
									+ "Content-Length: "
									+ 0 + "\n";
							output.write(responseheaders.getBytes());
							
						} else {
							
							
							JSONArray ipport = new JSONArray();

							ipport.add(latestip);
							ipport.add(latestport);
							String response = serverobj.toString() + "\n";
							System.out.println(response);
							// write 200 ok request
							String responseheaders = "HTTP/1.1 200 OK\n"
									+ "Content-Length: "
									+ response.toString().getBytes().length + "\n";
							output.write(responseheaders.getBytes());
							output.write(response.getBytes());
							
							
						}
						
						reader.close();
						output.flush();
						output.close();
						this.socket.close();
					}
				} 
				
			} else {

				String response = "No servers!\n";
				System.out.println(response);
				// write 200 ok request
				String responseheaders = "HTTP/1.1 400 BAD\n"
						+ "Content-Length: "
						+ response.toString().getBytes().length + "\n";
				output.write(responseheaders.getBytes());
				output.write(response.getBytes());

				reader.close();
				output.flush();
				output.close();
				this.socket.close();

			}

		} catch (IOException | ParseException e) {
			// System.err.println("IO error"); ignore
		}

	}
}
