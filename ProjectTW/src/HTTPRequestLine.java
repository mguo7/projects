import java.util.HashMap;

/**
 * HTTPRequestLine is a data structure that stores a Java representation of the
 * parsed Request-Line.
 **/
public class HTTPRequestLine {

	private HTTPConstants.HTTPMethod method;
	private String uripath;
	private HashMap<String, String> parameters;
	private String httpversion;

	/*
	 * You are expected to add appropriate constructors/getters/setters to
	 * access and update the data in this class.
	 */

	@SuppressWarnings("static-access")
	public HTTPRequestLine(String method, String uripath, String httpversion) {
		this.method = this.method.valueOf(method);
		this.uripath = uripath;
		this.httpversion = httpversion;
		this.parameters = new HashMap<String, String>();

	}

	public void setParameters(String key, String value) {

		this.parameters.put(key, value);
	}

	public String getMethod() {

		return this.method.toString();
	}

	public String getPath() {

		return this.uripath;
	}

	public String getVersion() {

		return this.httpversion;
	}

	public HashMap<String, String> getParameters() {

		return this.parameters;
	}
}