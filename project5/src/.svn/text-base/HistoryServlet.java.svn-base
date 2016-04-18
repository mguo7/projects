import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class HistoryServlet extends LoginBaseServlet {

	/** Pre-existing database connector. */
	protected DatabaseConnector connector;

	/** Initiailizes database connector to use for all servlet requests. */
	public HistoryServlet() {
		super();
		try {
			this.connector = new DatabaseConnector();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Responds to HTTP GET requests with a simple web page containing a static
	 * table of contacts.
	 */
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws IOException {

		PrintWriter out = response.getWriter();
		
		out.printf("<!DOCTYPE html>%n");
		out.printf("<html lang=\"en\">%n");
		out.printf("<head>%n");
		out.printf("\t<meta charset=\"utf-8\">%n");
		out.printf("\t<title>Contact Listing</title>%n");
		out.printf("</head>%n%n");
		out.printf("<body>%n");
		out.println("<form method=\"post\" action=\"/history\">");
		out.println("<p>");
		out.println("<div align=\"center\">");
		out.println("<h1>SEARCH HISTORY</h1><p><p>");
		out.println("&nbsp;&nbsp;<a href=\"/\">Back To Homepage</a>");
		out.printf("<table cellspacing=\"0\" cellpadding=\"2\" border=\"1\">%n");
		
		outputContacts(request, response);
		
		out.printf("</table>%n");
		out.println("<br>");
		out.println("<input type=\"submit\" value=\"Clear\">");
		out.println("</div>");
		out.println("</form>");
		out.printf("%n</body>%n</html>%n");
		out.flush();

		response.setStatus(HttpServletResponse.SC_OK);
		finishResponse(request, response);
	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws IOException {

		Map<String, String> cookiemap = getCookieMap(request);

		String username = cookiemap.get("name");

		String DELETE = "DELETE FROM users_history WHERE username = " + "'"
				+ username + "'";
		
		try {
			Connection db = connector.getConnection();
			Statement statement = db.createStatement();
			statement.execute(DELETE + ";");

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		response.sendRedirect("/history");
		response.setStatus(HttpServletResponse.SC_OK);
		finishResponse(request, response);
	}
  

	/**
	 * Outputs the contact information, without any links.
	 * 
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	protected void outputContacts(HttpServletRequest request,
			HttpServletResponse response) throws IOException {

		PrintWriter out = response.getWriter();
		String cellFormat = "\t<td>%s</td>%n";

		Map<String, String> cookiemap = getCookieMap(request);

		String username = cookiemap.get("name");

		String SELECT = "SELECT query FROM users_history WHERE username = "
				+ "'" + username + "'";

		try (Connection db = connector.getConnection();
				Statement statement = db.createStatement();
				ResultSet results = statement.executeQuery(SELECT + ";");) {
			// The text used in results.getString(String) must match the
			// column names in the SELECT statement.
			while (results.next()) {
				out.printf("<tr>%n");
				out.printf(cellFormat, results.getString("query"));
				out.printf("</tr>%n");
			}

		} catch (SQLException e) {
			out.printf("\t<td colspan=\"5\">%s</td>%n", e.getMessage());
		}
	}
}