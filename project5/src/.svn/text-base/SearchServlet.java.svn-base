import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class SearchServlet extends LoginBaseServlet {

	InvertedIndex index;
	IndexBuilder indexbuilder;
	WorkQueue workers;

	public SearchServlet() {
		super();
		index = new InvertedIndex();
		WorkQueue workers = new WorkQueue();
		indexbuilder = new IndexBuilder(
				"http://www.cs.usfca.edu/~sjengle/cs212/crawl/birds.html",
				workers);
		indexbuilder.parseUrl(
				"http://www.cs.usfca.edu/~sjengle/cs212/crawl/birds.html",
				index);
	}

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) {

		float start = System.nanoTime();

		ArrayList<SearchResults> results;
		results = new ArrayList<SearchResults>();

		try {

			String query = request.getParameter("query");
			SearchBuilder searchbuilder = new SearchBuilder(workers);
			results = searchbuilder.buildSearch(query, index);

		}

		catch (Exception e) {
			log.warn("Unable to properly write HTTP response.");
		}

		float runtime = System.nanoTime() - start;

		String username = getUsername(request);

		try {
			PrintWriter writer = response.getWriter();
			prepareResponse("Search", response);

			if (username != null) {

				//String query = new String();
			 

				writer.print("<p>Welcome, " + username);
				writer.print("&nbsp;&nbsp;<a href=\"/newpassword\">Change Password</a>");
				writer.print("&nbsp;&nbsp;<a href=\"/history\">View History</a>");
				writer.println("&nbsp;&nbsp;<a href=\"/logout\"> Logout </a>");

			} else {
				writer.print("<p><a href=\"/login\"> Login </a>");
				writer.println("&nbsp;&nbsp;<a href=\"/register\"> Register </a>");
				writer.println();
			}
			writer.println("<body bgcolor=\"#e3e3e2\">");
			writer.println("<div align=\"center\">");
			writer.println("<p>");
			writer.println("<img src=\"http://v2.freep.cn/3tb_1312120405445ktq512293.png\">");
			//writer.println("<h1>Easy Search</h1>");
			writer.println("<p>");
			writer.println("<p>");
			writer.println("<form action=\"/\" method=\"post\">");
			writer.println("<table border=\"0\">");
			writer.println("\t<tr>");
			writer.println("\t\t<td><input type=\"text\" name=\"query\" value=\"\" size=\"80\"></td>");
			writer.println("\t\t<td><input type=\"submit\" value=\"Search\"></td>");
			if(username!=null){
			writer.println("\t<td><p><input type=\"checkbox\" name=\"save\" value=\"off\">");
			writer.println("Do not save my query :)</p></td>");
			}
			writer.println("\t</tr>");
			writer.println("</table>");
			writer.println("</form>");
			writer.println("</div>");
			writer.print("</body>");

			synchronized (results) {

				if (!results.isEmpty()) {

					writer.println("<p>There are " + results.size()
							+ " results. " + "( About " + runtime
							+ " seconds )");

					for (SearchResults result : results) {
						writer.println("<p>" + result.toString() + "<p>");
						writer.println("<br>");
					}

					writer.println("<br>");
				} // out query

			}
			results.clear();

			writer.flush();
			writer.close();
			response.setStatus(HttpServletResponse.SC_OK);
			finishResponse(request, response);
		} catch (IOException e) {
			log.warn("Unable to properly write HTTP response.");
		}
	}

	/**
	 * Responses to POST requests by creating and saving a new cookie based on
	 * the name/value provided in the form, or deletes a cookie if necessary
	 */
	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		String username = getUsername(request);
		if (username != null) {

			String query = new String();
			if (request.getParameter("query") != null) {
				query = request.getParameter("query");
				
				System.out.println(request.getParameter("save"));
			}
			if (!query.isEmpty()&&request.getParameter("save") == null) {

				db.AddHistory(username, query);

			}
 
		}
		doGet(request, response);
	}

}