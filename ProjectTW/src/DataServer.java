import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Timer;


/**
 * This class will build a BackEnd Server which will use basic java socket. The
 * server will use up to 10 threads to process incoming requests concurrently.
 * 
 * @author miaoguo
 * 
 */
public class DataServer {

	public static void main(String args[]) throws IOException {

		String port;
		int id;
		String discoveryIp;
		String discoveryPort;
		 
		Twitter twitter = new Twitter();

		 

		if (args.length >= 2 && !args[1].isEmpty()) {
			port = args[0];
			id = Integer.parseInt(args[1]);
		} else {

			System.out.println("Please enter a port and a priority id for Server");
			return;
		}

		if(args.length>=4 && !args[2].isEmpty() && !args[3].isEmpty()){
			
			discoveryIp = args[2];
			discoveryPort = args[3];
			
		} else {
			
			System.out.println("Please enter Discovery Server's Ip and Port.");
			return;
			
		}
		 
		// {ip}
		ServerSocket serversocket = new ServerSocket(Integer.parseInt(port));
		ExecutorService executor = Executors.newFixedThreadPool(10);

		String addr = InetAddress.getLocalHost().getHostAddress();
		// addr = addr.split("/")[1];
		ServerStatus status = new ServerStatus(id, port, addr, 0,twitter);
		status.discoveryIp = discoveryIp;
		status.discoveryPort = discoveryPort;
		
		status.getTweets();
		String response = status.addServer();

		if (!response.equals("ok")) {

			// return;

			while (!response.equals("ok")) {
				System.err.println("Can not find Discovery Server!");
				response = status.addServer();
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		Timer timer = new Timer(); 
		timer.schedule(new ServerDetector(status),0,3000);
		
		try {

			while (true) {

				Socket socket = serversocket.accept();
				executor.execute(new DataSocket(socket, twitter,status));
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
