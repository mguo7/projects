import java.util.HashMap;
import java.util.TreeMap;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class ServerHandler {

	public HashMap<String, ServerStatus> servers;
	public TreeMap<String, Integer> balancer;
	private final MultiReaderLock lock;

	public ServerHandler() {

		this.servers = new HashMap<String, ServerStatus>();
		this.balancer = new TreeMap<String, Integer>();
		this.lock = new MultiReaderLock();
	}

	public void addServer(String request) {

		JSONParser jsonParser = new JSONParser();
		JSONObject jsonServer;

		try {

			jsonServer = (JSONObject) jsonParser.parse(request);
			String ip = (String) jsonServer.get("ip");
			String port = (String) jsonServer.get("port");
			int id = Integer.parseInt(jsonServer.get("id").toString());
			int version = Integer
					.parseInt(jsonServer.get("version").toString());
			 

			String host = ip + ":" + (String) jsonServer.get("port");
			Twitter twitter = new Twitter();
			ServerStatus status = new ServerStatus(id, port, ip, version,
					twitter);

			this.lock.lockWrite();

			servers.put(host, status);
			if(!this.balancer.containsKey(host)){
				
				this.balancer.put(host, 0);
			}
			
			
			this.lock.unlockWrite();
			System.out.println(servers);

		} catch (ParseException e) {
			// TODO Auto-generated catch block
			System.err.println("Can not parse Request!");
		}

	}

 

}
