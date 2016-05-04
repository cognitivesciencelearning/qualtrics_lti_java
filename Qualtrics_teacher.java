package org.assistments.connector.lti;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.oscelot.lti.tp.Callback;
import org.oscelot.lti.tp.DataConnector;
import org.oscelot.lti.tp.ResourceLink;
import org.oscelot.lti.tp.ToolProvider;
import org.oscelot.lti.tp.User;
import org.oscelot.lti.tp.dataconnector.None;

@WebServlet({ "/qualtrics_teacher/*" })
public class Qualtrics extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	public Qualtrics() {
		super();
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doPost(req, resp);
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String pathInfo = req.getPathInfo();
		String sid = pathInfo.substring(1);
		String urlPattern = req.getParameter("custom_url_pattern");
		String url = urlPattern + "?SID=" + sid;
		String teacher_url = req.getParameter("custom_teacher_url");

		
		url = "https://" + url;


		String role = req.getParameter("roles");
		DataConnector dc = new None();
		
		Callback doLaunch = new DoLaunch(url);
		ToolProvider toolProvider = new ToolProvider(req, resp, doLaunch, dc);
		toolProvider.execute();
		
	}
	class DoLaunch implements Callback {
		
		String url;
		
		public DoLaunch(String url) {
			this.url = url;
		}
		
		@Override
		public boolean execute(ToolProvider toolProvider) {
			User ltiUser = toolProvider.getUser();

			ResourceLink resourceLink = toolProvider.getResourceLink();
			String userId = ltiUser.getId();
			String resourceId = resourceLink.getId();
			//Determine user role from LTI
			if(ltiUser.isAdmin() || ltiUser.isStaff()) {
				String teacher_url = toolProvider.getRequest().getParameter("custom_teacher_url");
				url = teacher_url;
				toolProvider.setRedirectUrl(teacher_url);
			}
			else {
				url += "&user_id="+userId + "&resource_id=" + resourceId;
				userId += "_" + resourceId;
				ServletContext context = toolProvider.getRequest().getServletContext();
				context.setAttribute(userId, ltiUser);
				context.setAttribute(resourceId, resourceLink);
				toolProvider.setRedirectUrl(url);
			}
			return true;
		}
	}
}

