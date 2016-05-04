package org.assistments.connector.lti;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpStatus;
import org.oscelot.lti.tp.Outcome;
import org.oscelot.lti.tp.ResourceLink;
import org.oscelot.lti.tp.User;

@WebServlet({ "/qualtrics_result",  "/qualtrics_result/*"})
public class QualtricsResult extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public QualtricsResult() {
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
		String grade = req.getParameter("grade");
		String userId = req.getParameter("user_id");
		String resourceId = req.getParameter("resource_id");
		userId += "_" + resourceId;
		ServletContext context = req.getServletContext();
		
		User ltiUser = (User)context.getAttribute(userId);
		ResourceLink resourceLink = (ResourceLink)context.getAttribute(resourceId);
		double f = Double.valueOf(grade);
		Outcome outcome = new Outcome();
		outcome.setValue(String.valueOf(f));
		outcome.setType(ResourceLink.EXT_TYPE_DECIMAL);
		boolean b = resourceLink.doOutcomesService(ResourceLink.EXT_WRITE, outcome, ltiUser);
		context.removeAttribute(userId);
		context.removeAttribute(resourceId);
		System.out.println("Request received");
		resp.setStatus(HttpStatus.SC_ACCEPTED);
		resp.setContentType("application/json");
	}
}
