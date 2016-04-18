import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;


public class HTTPRequestLineParser {

	/**
	 * This method takes as input the Request-Line exactly as it is read from
	 * the socket. It returns a Java object of type HTTPRequestLine containing a
	 * Java representation of the line.
	 * 
	 * The signature of this method may be modified to throw exceptions you feel
	 * are appropriate. The parameters and return type may not be modified.
	 * 
	 * 
	 * @param line
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static HTTPRequestLine parse(String line)
			throws UnsupportedEncodingException {

		// A Request-Line is a METHOD followed by SPACE followed by URI followed
		// by SPACE followed by VERSION
		// A VERSION is 'HTTP/' followed by 1.0 or 1.1
		// A URI is a '/' followed by PATH followed by optional '?' PARAMS
		// PARAMS are of the form key'='value'&'
		 
		String[] lines = line.split("\\s+");
		String method = lines[0];
		String uri = lines[1];
		String version = lines[2].split("/")[1];
		String params = "";
		HTTPRequestLine requestline = new HTTPRequestLine(method, uri, version);

		if (uri.contains("?")) {
			params = uri.split("\\?")[1];
			if (params.contains("&")) {
				String[] parlist = params.split("&");

				for (String param : parlist) {

					String key = param.split("=")[0];
					String value = param.split("=")[1];
					value = URLDecoder.decode(value, "UTF-8");
					if (!key.isEmpty() && !value.isEmpty()) {
						requestline.setParameters(key, value);
					}
				}
			} else {
				String key = params.split("=")[0];
				String value = params.split("=")[1];
				value = URLDecoder.decode(value, "UTF-8");
				if (!key.isEmpty() && !value.isEmpty()) {
					requestline.setParameters(key, value);
				}
			}
		}

		 return requestline;
	}

}