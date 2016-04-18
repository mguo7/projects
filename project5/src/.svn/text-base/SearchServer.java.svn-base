import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
 
public class SearchServer {

	private static final Logger log = LogManager.getLogger();
	private static final int PORT = 8080;

        public static void main(String[] args) throws Exception {
                // Enable DEBUG logging
                System.setProperty("org.eclipse.jetty.LEVEL", "DEBUG");
                Server server = new Server(PORT);
                
                ServletHandler handler = new ServletHandler();
        		server.setHandler(handler);

        		handler.addServletWithMapping(SearchServlet.class, "/");
        		handler.addServletWithMapping(LoginUserServlet.class,"/login");
        		handler.addServletWithMapping(LoginRegisterServlet.class, "/register");
//        		//handler.addServletWithMapping(LoginWelcomeServlet.class,  "/welcome");
         		handler.addServletWithMapping(LoginRedirectServlet.class, "/redirect");
         		handler.addServletWithMapping(LogoutServlet.class, "/logout");
         		handler.addServletWithMapping(ChangePasswordServlet.class, "/newpassword");
         		handler.addServletWithMapping(HistoryServlet.class, "/history");
         		
        		log.info("Starting server on port " + PORT + "...");

        		try {
        			server.start();
        			server.join();

        			log.info("Exiting...");
        		}
        		catch (Exception ex) {
        			log.fatal("Interrupted while running server.", ex);
        			System.exit(-1);
        		}
        }
}