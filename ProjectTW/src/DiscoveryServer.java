import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class DiscoveryServer {
	
	public static void main(String args[]) throws IOException {

		int port;
		if (args.length == 0) {
			port = 8080;
		} else {
			port = Integer.parseInt(args[0]);
		}
		
		ServerSocket serversocket = new ServerSocket(port);
		ExecutorService executor = Executors.newFixedThreadPool(10); 
		
 
		try {
			 
 		
			ServerHandler servers = new ServerHandler();
			
			while (true) {
				
				Socket socket = serversocket.accept();
				executor.execute(new DiscoverySocket(socket, servers));
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
