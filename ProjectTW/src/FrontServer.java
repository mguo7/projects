import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This class will build a FrontEnd Server which will use basic java socket. The server
 * will use up to 10 threads to process incoming requests concurrently.
 * 
 * @author miaoguo
 * 
 */
public class FrontServer {

	public static void main(String args[]) throws IOException {

		int port;
		String discoveryIp;
		String discoveryPort;
		
		System.out.println(args[2]);

		if (args.length >= 1 && !args[0].isEmpty()) {
			port = Integer.parseInt(args[0]);
		} else {

			System.out.println("Please enter a port number for Front Server");
			return;
		}

		if(args.length>=3 && !args[1].isEmpty() && !args[2].isEmpty()){
			
			discoveryIp = args[1];
			discoveryPort = args[2];
			
		} else {
			
			System.out.println("Please enter Discovery Server's Ip and Port.");
			return;
			
		}
		
		ServerSocket serversocket = new ServerSocket(port);
		ExecutorService executor = Executors.newFixedThreadPool(10); 
		String addr = InetAddress.getLocalHost().getHostAddress();
		Twitter twitter = new Twitter();
		ServerStatus status = new ServerStatus(0, args[0], addr, 0, twitter);
		status.discoveryIp = discoveryIp;
		status.discoveryPort = discoveryPort;
		
		try {
			CacheHandler cache = new CacheHandler();
			while (true) {

				Socket socket = serversocket.accept();
				executor.execute(new FrontSocket(socket, cache,status));
			}

		} catch (Exception e) {
			System.err.println("Unexpecter Error");
			return;
		} finally {
			serversocket.close();
			executor.shutdown();
		}

	}

}
