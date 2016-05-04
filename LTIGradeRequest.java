package org.assistments.connector.lti;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.assistments.connector.utility.Constants;
import org.assistments.connector.utility.Response;
import org.oscelot.lti.tp.Outcome;
import org.oscelot.lti.tp.ResourceLink;
import org.oscelot.lti.tp.User;

import com.google.gson.Gson;

@WebServlet({ "/LTIGradeRequest", "/ltigraderequest" })
public class LTIGradeRequest extends HttpServlet {

	private static final long serialVersionUID = 1L;

	public LTIGradeRequest() {
        super();
        // TODO Auto-generated constructor stub
    }
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	doPost(request, response);
    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		/*
		String userRef = request.getParameter("user_ref");
		
		String problemSetID = request.getParameter("problem_set_id");
		
		String classRef = request.getParameter("class_ref");
		
		ExternalUserDAO userDAO = new ExternalUserDAO(Utility.PARTNER_REFERENCE);
		ExternalUser externalUser = userDAO.findByExternalRef(userRef);
		
		String partnerExternalRef = problemSetID + classRef;
		ExternalAssignmentDAO assignmentDAO = new ExternalAssignmentDAO(Utility.PARTNER_REFERENCE);
		ExternalAssignment assignment = assignmentDAO.findByPartnerExternalRef(partnerExternalRef);
		String assignmentRef = assignment.getExternal_refernce();
		
		String ltiUserId = Utility.generateLTIUserId(userRef);
		String resourceLinkId = Utility.generateResourceLinkId(userRef, assignmentRef);
		
		ServletContext sc = getServletContext();
//		HttpSession session = request.getSession();
		User ltiUser = (User)sc.getAttribute(ltiUserId);
		ResourceLink resourceLink = (ResourceLink)sc.getAttribute(resourceLinkId);
		if(ltiUser != null && resourceLink != null && assignmentRef != null) {
			Response res = AssignmentController.getGrade(userRef, assignmentRef, Utility.PARTNER_ID);
			if(res.getHttpCode() < 400) {
				@SuppressWarnings("rawtypes")
				Map jsonObject = new Gson().fromJson(res.getContent(), Map.class);
				String grade = jsonObject.get("score").toString();
				float score = Float.valueOf(grade) / 100;
				Outcome outcome = new Outcome();
				outcome.setValue(new Float(score).toString());
				outcome.setType(ResourceLink.EXT_TYPE_DECIMAL);
				boolean b = resourceLink.doOutcomesService(ResourceLink.EXT_WRITE,outcome,ltiUser);
				if(b) {
					sc.removeAttribute(ltiUserId);
					sc.removeAttribute(resourceLinkId);
				}
			}
		}
		String accessToken = externalUser.getUser_access_token();
		String addrToGo = Utility.getUserPageURL( Constants.STUDENT_ROLE, Utility.PARTNER_REFERENCE, accessToken, assignment.getExternal_refernce());
		
		 response.setStatus(HttpServletResponse.SC_OK);
		  response.sendRedirect(addrToGo);*/
		
	}
}
