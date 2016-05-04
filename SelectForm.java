package org.assistments.connector.lti;

import java.io.IOException;
import java.net.URLEncoder;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.assistments.connector.utility.Constants;
import org.oscelot.lti.tp.Callback;
import org.oscelot.lti.tp.ToolProvider;

@WebServlet({ "/SelectForm", "/select_form" })
public class SelectForm extends HttpServlet {

	private static final long serialVersionUID = 1L;

	public SelectForm() {
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
		String returnURL = req.getParameter("return_url");
		String encodedID = req.getParameter("problem_set_id").trim().toUpperCase();
		String launchURL = Constants.CONNECTOR_HTTPS_URL + "connect?custom_appid="+encodedID;
		launchURL = URLEncoder.encode(launchURL, "UTF-8");
		String redirectURL = returnURL + "?return_type=lti_launch_url&url="+launchURL;
		resp.sendRedirect(redirectURL);
		
	}
	
	class DoLaunch implements Callback {

		@Override
		public boolean execute(ToolProvider toolProvider) {
			String returnURL = toolProvider.getReturnUrl();
			String encodedID = toolProvider.getRequest().getParameter("problem_set_id");
			String decodedID = Utility.decodeProblemSetString(encodedID);
			String launchURL = Constants.CONNECTOR_HTTPS_URL + "connect?custom_appid="+decodedID;
			String redirectURL = returnURL + "?return_type=lti_launch_url&url="+launchURL;
			toolProvider.setRedirectUrl(redirectURL);;
			return true;
		}
		
	}
}
