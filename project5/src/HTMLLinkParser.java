import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class will create a regular expression that is able to 
 *  parse links from HTML.   
 *  @author Miao Guo
 */
public class HTMLLinkParser {

        /**
         * The regular expression used to parse the HTML for links.
         * 
         */
        public static final String REGEX = "(?<=a href=\")([a-zA-Z0-9@:%_+.~#?&//=])+[.](/*?[-a-zA-Z0-9@:%_+.~#?&=/]*)";

        /**
         * The group in the regular expression that captures the raw link. 
         */
        public static final int GROUP = 2;

        /**
         * Parses the provided text for HTML links. You should not need to modify
         * this method.
         *
         * @param text - valid HTML code, with quoted attributes and URL encoded links
         * @return list of links found in HTML code
         */
        public static ArrayList<String> listLinks(String text, String currentlink) {
                // list to store links
        	 
                ArrayList<String> links = new ArrayList<String>();

                // compile string into regular expression
                Pattern p = Pattern.compile(REGEX, Pattern.CASE_INSENSITIVE);

                // match provided text against regular expression
                Matcher m = p.matcher(text);

                // loop through every match found in text
                for(int i = 0; i<= GROUP;i++){
                while(m.find()) {
                
                	    String link = m.group(i);
                	    	
                	    	try {
								URL base = new URL(currentlink);
								URL absolute = new URL(base, link);
								link = absolute.toString();
							    URI url = URI.create(link);
			                    links.add("http://" + url.getAuthority() + url.getPath());
							} catch (MalformedURLException e) {
								
								System.err.println("Incorrect url form!");
							}  catch(IllegalArgumentException ie) {
				                
		                		   System.err.println("Incorrect url form : " + link);
		                	   }
                 	}
            }// out of i
                
                return links;
        }
        
}