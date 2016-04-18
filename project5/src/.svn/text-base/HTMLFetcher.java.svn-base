import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
 
/**
 * A helper class with several static methods that will help fetch a webpage,
 * strip out all of the HTML, and parse the resulting plain text into words.
 * Meant to be used for the web crawler project.
 * 
 * @author Miao Guo
 */
public class HTMLFetcher {
 
	/**
	 * Removes all style and script tags (and any text in between those tags),
	 * all HTML tags, and all special characters/entities.
	 * 
	 * @param html - html code to parse
	 * @return plain text
	 */
	public static String cleanHTML(String html) {
		String text = html.toLowerCase() ;
		text = text.replaceAll("<!--.*?-->", " ");
		text = stripElement("script", text);
		text = stripElement("style", text);
		text = stripTags(text);	
		text = stripEntities(text);
		
		return text;
	}

	/**
	 * Fetches the webpage at the provided URL by opening a socket, sending an
	 * HTTP request, removing the headers, and returning the resulting HTML
	 * code.
	 * 
	 * @param url - webpage to download
	 * @return html - html code
	 */
	public static String fetchHTML(URL addr) {
	 
		int PORT = 80;
		StringBuilder html = new StringBuilder();
		boolean header = true;

		try (   
				Socket socket = new Socket(addr.getHost(), PORT);
				BufferedReader reader = new BufferedReader(new InputStreamReader(
										socket.getInputStream()));
				PrintWriter writer = new PrintWriter(socket.getOutputStream());
		    ) {
			 
			String request = craftRequest(addr);
			writer.println(request);
			writer.flush();

			String line = reader.readLine();
			
			while (line != null) {
		 
				if (header) {
					if (line.trim().isEmpty()) {
						header = false;	
					}
				} else {
				html.append(line + " ");
				
				}
				line = reader.readLine();
			}
		} catch (Exception e) {

			System.err.println("Errors on fetching HTML page " + addr.toString());
		}
 
		return html.toString();
	}

	/**
	 * Removes everything between the element tags, and the element tags
	 * themselves. For example, consider the html code:
	 * 
	 * <pre>
	 * &lt;style type="text/css"&gt;body { font-size: 10pt; }&lt;/style&gt;
	 * </pre>
	 * 
	 * If removing the "style" element, all of the above code will be removed,
	 * and replaced with the empty string.
	 * 
	 * @param name - name of the element to strip, like style or script
	 * @param html - html code to parse
	 * @return html code without the element specified
	 */
	public static String stripElement(String name, String html) {
		 
		String reg = "(<"+name+"([^>]*\n*[^>]*)>([^<]*\n*[^<]*)</"+name+"[^>]*>)";
		html = html.replaceAll(reg, " ");
		  
		return html;
	}

	/**
	 * Removes all HTML tags, which is essentially anything between the < and >
	 * symbols. The tag will be replaced by the empty string.
	 * 
	 * @param html - html code to parse
	 * @return text without any html tags
	 */
	public static String stripTags(String html) {
	 
		html = html.replaceAll("(<([^>]*)>)", " ");
		
		return html;
	}

	/**
	 * Replaces all HTML entities in the text with the empty string. For
	 * example, "2010&ndash;2012" will become "20102012".
	 * 
	 * @param html - the text with html code being checked
	 * @return text with HTML entities replaced by a space
	 */
	public static String stripEntities(String html) {
		 
		html = html.replaceAll("&.*?;", " ");
		
		return html;
	}

	/**
	 * Crafts the HTTP GET request from the URL.
	 * 
	 * @param url - Getting request from the url
	 * @return HTTP request
	 */
	private static String craftRequest(URL url) {
		String host = url.getHost();
		String resource = url.getFile().isEmpty() ? "/" : url.getFile();

		StringBuffer output = new StringBuffer();
		output.append("GET " + resource + " HTTP/1.1\n");
		output.append("Host: " + host + "\n");
		output.append("Connection: close\n");
		output.append("\r\n");

		return output.toString();
	}

}