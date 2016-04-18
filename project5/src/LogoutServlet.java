import java.io.IOException;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@SuppressWarnings("serial")
public class LogoutServlet extends LoginBaseServlet {

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
			
		  Cookie[] cookies = request.getCookies();
		   Map<String, String> cookiemap = getCookieMap(request);
		    
		   String user  = cookiemap.get("name");
		   
           if (cookies != null) {
                   for (Cookie cookie : cookies) {
                	   
                	   if(cookie.getName().equals(user))
                           cookie.setValue(null);
                           cookie.setMaxAge(0);
                           response.addCookie(cookie);
                   }
           }
        response.addCookie(new Cookie("login", "false"));
		response.sendRedirect("/");
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		doGet(request, response);
	}
}
