import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@SuppressWarnings("serial")
public class ChangePasswordServlet extends LoginBaseServlet {

	@Override
	public void doGet(
			HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		prepareResponse("Change PassWord", response);

		PrintWriter out = response.getWriter();
		String error = request.getParameter("error");

		if(error != null) {
			String errorMessage = StringUtilities.getStatus(error).message();
			out.println("<p style=\"color: red;\">" + errorMessage + "</p>");
		}

		printForm(out);
		finishResponse(request, response);
	}

	@Override
	public void doPost(
			HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		prepareResponse("ChangePassWord", response);

		
		Map<String, String> cookies = getCookieMap(request);
		String username  = cookies.get("name");
		//System.out.println(username);
		String newpass = request.getParameter("pass");
		
		if(!newpass.isEmpty()){
	   // System.out.println(newpass);
	    Status status = db.changePassword(username, newpass);

		if(status == Status.OK) {
			response.sendRedirect(response.encodeRedirectURL("/"));
		}
		else {
			String url = "/change?error=" + status.name();
			response.sendRedirect(response.encodeRedirectURL(url));
		  }
		}
	}

	private void printForm(PrintWriter out) {
		out.println("<form action=\"/newpassword\" method=\"post\">");
		out.println("<table border=\"0\">");
		out.println("\t<tr>");
		out.println("\t\t<td>New Password:</td>");
		out.println("\t\t<td><input type=\"password\" name=\"pass\" size=\"30\"></td>");
		out.println("</tr>");
		out.println("</table>");
		out.println("<p><input type=\"submit\" value=\"Submit\"></p>");
		out.println("</form>");
	}
}
