package org.assistments.connector.lti;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.codec.binary.Base32;
import org.assistments.connector.domain.PartnerToAssistments;
import org.assistments.connector.domain.PartnerToAssistments.ColumnNames;
import org.assistments.connector.exception.ReferenceNotFoundException;
import org.assistments.connector.exception.TransferUserException;
import org.assistments.connector.service.AccountService;
import org.assistments.connector.service.AssignmentService;
import org.assistments.connector.service.StudentClassService;
import org.assistments.connector.service.impl.AccountServiceImpl;
import org.assistments.connector.service.impl.AssignmentServiceImpl;
import org.assistments.connector.service.impl.StudentClassServiceImpl;
import org.assistments.connector.utility.Constants;
import org.assistments.service.domain.ReferenceTokenPair;
import org.oscelot.lti.tp.Callback;
import org.oscelot.lti.tp.DataConnector;
import org.oscelot.lti.tp.ResourceLink;
import org.oscelot.lti.tp.ToolConsumer;
import org.oscelot.lti.tp.ToolProvider;
import org.oscelot.lti.tp.User;
import org.oscelot.lti.tp.dataconnector.None;

@WebServlet({ "/Connect", "/connect", "/connect/" })
public class Connect extends HttpServlet {

	private static final long serialVersionUID = 1L;
	public static final String partnerRef = "Hien-Ref";
	
	public Connect() {
		super();
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	doPost(request, response);
    }
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		DataConnector dc = new None();

		ToolConsumer toolConsumer = new ToolConsumer("testing.edu", dc, true);
		toolConsumer.setName("Testing");
		toolConsumer.setSecret("secret");
		toolConsumer.save();		

		Callback doLaunch = new DoLaunch();
		ToolProvider toolProvider = new ToolProvider(request, response, doLaunch, dc);
		
		toolProvider.execute();
	}
}

class DoLaunch implements Callback {
	
	@Override
	public boolean execute(ToolProvider toolProvider) {
		String userType = "";
		String onBehalf = "";
		String userRef = "";
		User ltiUser = toolProvider.getUser();
		ResourceLink resourceLink = toolProvider.getResourceLink();
		
		//Determine user role from LTI
		if(ltiUser.isAdmin() || ltiUser.isStaff()) {
			userType = Constants.TEACHER_ROLE;
//		} else if ( ltiUser.isLearner() ) {
		} else {
			userType = Constants.STUDENT_ROLE;
		}		
		
		String partnerExternalRef = toolProvider.getUser().getId();
		
		org.assistments.service.domain.User user = Utility.getUserInfo(toolProvider);
		AccountService as = new AccountServiceImpl(Utility.PARTNER_REFERENCE);
		ReferenceTokenPair pair = null;
		try {
			pair = as.transferUser(user, Utility.EDMODO_SCHOOL_REF, 
					partnerExternalRef, "", "lti-user");
		} catch (TransferUserException e1) {
			throw new RuntimeException(e1);
		}
		userRef = pair.getExternalRef();
		onBehalf = pair.getAccessToken();
		
		String contextTitle = toolProvider.getRequest().getParameter("context_title");
		String contextID = toolProvider.getRequest().getParameter("context_id");

//		String url = toolProvider.getRequest().getRequestURL().toString();
		toolProvider.getRequest().getRequestURI();
		if(contextTitle == null || contextTitle.equals("")) {
			if(contextID != null) {
				contextTitle = contextID;
			} else if(resourceLink.getTitle() != null && resourceLink.getTitle().equals("")) {
				contextTitle = resourceLink.getTitle();
			} else {
				contextTitle = "Default Class";
			}
		}
		contextID = resourceLink.getId();
		HttpSession session = toolProvider.getRequest().getSession();
		String appID = toolProvider.getRequest().getParameter("custom_appid");
		if(appID == null || appID.equals("")) {
			//tell teachers problem set id doesn't exist.
			String msg = "Sorry.. There is a syntax error in custom parameters";
			String instruction = "Teacher: Please make sure custom parameters that you typed in is correct. "
					+ "It should be <i>appid=[problem set id]</i> <br><br>"
					+ "Student: Tell your teacher this error. Your teacher should correct this error so you can start the assignment correctly.";
			Utility.redirectToInstructionPage(toolProvider, session, msg, instruction);
			return true;
		}
		appID = appID.trim();
		//convert it into problem set number id
		String problemSetID = Utility.decodeProblemSetString(appID);
		if(problemSetID == null || problemSetID.equals("")) {
			//tell teachers problem set id doesn't exist.
			String msg = "Sorry.. The problem set doesn't exist";
			String instruction = "Teacher: Please make sure the problem set id that you typed in is correct. It should start with PS.<br><br>"
					+ "Student: Tell your teacher this error. Your teacher should correct this error so you can start the assignment correclty.";
			Utility.redirectToInstructionPage(toolProvider, session, msg, instruction);
			return true;
		}
		//If teacher, try to create class. Otherwise enroll in the class.
		if(userType.equals(Constants.TEACHER_ROLE)) {
			
//			String classRef = Utility.createClass(contextTitle, contextID, Utility.PARTNER_REFERENCE, onBehalf);
			StudentClassService scs = new StudentClassServiceImpl(Utility.PARTNER_REFERENCE, onBehalf);
			String classRef = scs.transferStudentClass(contextTitle, contextID, "", "lti-class");
			//Create assignment if given an appID
			if(problemSetID !=null && problemSetID.length()>0){
				AssignmentService assignmentService = new AssignmentServiceImpl(Utility.PARTNER_REFERENCE, onBehalf);
				partnerExternalRef = problemSetID + classRef;
				String assignmentRef = null;
				assignmentRef = assignmentService.transferClassAssignment(problemSetID, classRef, partnerExternalRef, "", "lti-assignment");
				Base32 base32 = new Base32();
				String reportRef = base32.encodeAsString(assignmentRef.getBytes());
				toolProvider.setRedirectUrl(Constants.CONNECTOR_URL + "report/"+reportRef+"?ref=Hien");
			}
		} else if(userType.equals(Constants.STUDENT_ROLE)) {
			//first check if the student class exists
			String stuClassPartnerRef = contextID;
			StudentClassService scs = new StudentClassServiceImpl(Utility.PARTNER_REFERENCE, onBehalf);
			String classRef = null;
			
			if(scs.isExternalStudentClassExists(stuClassPartnerRef)) {
				//get student class external reference
				PartnerToAssistments pta = null;
				try {
					pta = scs.find(ColumnNames.PARTNER_EXTERNAL_REFERENCE, stuClassPartnerRef).get(0);
				} catch (org.assistments.connector.exception.ReferenceNotFoundException e) {
					// this should never happen since it's sure that the class exists.
				}
				classRef = pta.getAssistmentsExternalRefernce();
			} else {
				//tell students the assignment hasn't been created yet.
				String msg = "The assignment hasn't been created yet.";
				String instruction = "Please tell your teacher that he/she "
						+ "has to click the assignment after it is created. So you can start the assignment correctly.";
				Utility.redirectToInstructionPage(toolProvider, session, msg, instruction);
				return true;
			}
			String assignmentPartnerRef = problemSetID + classRef;
			AssignmentService assignmentService = new AssignmentServiceImpl(Utility.PARTNER_REFERENCE, onBehalf);
			String assignmentRef = null;
			try {
				assignmentRef = assignmentService.find(ColumnNames.PARTNER_EXTERNAL_REFERENCE, assignmentPartnerRef)
					.get(0).getAssistmentsExternalRefernce();
			} catch (ReferenceNotFoundException e) {
				String msg = "The assignment hasn't been created yet.";
				String instruction = "Please tell your teacher that he/she "
						+ "has to click the assignment after it is created. So you can start the assignment correctly.";
				Utility.redirectToInstructionPage(toolProvider, session, msg, instruction);
				return true;
			}
			//put all parameter to servlet context
			ServletContext sc = toolProvider.getRequest().getServletContext();
			String ltiUserId = Utility.generateLTIUserId(userRef);
			String resourceLinkId = Utility.generateResourceLinkId(userRef, assignmentRef);
			
			sc.setAttribute(ltiUserId, ltiUser);
			sc.setAttribute(resourceLinkId, resourceLink);
			
			scs.enrollStudent(userRef, classRef);
			//Goto assignment if given an appID
			if(problemSetID !=null && problemSetID.length()>0){
				// go to tutor
				String onExit = Utility.generateStudentReportURL(userRef, assignmentRef, "lti");
				try {
					onExit = URLEncoder.encode(onExit, "UTF-8");
					onExit = URLEncoder.encode(onExit, "UTF-8");
				} catch (UnsupportedEncodingException e) {
					throw new RuntimeException(e);
				}
				
				String fullName = user.getDisplayName();
				String tutorURL = assignmentService.getAssignment(assignmentRef, onExit);
				try {
					tutorURL = URLEncoder.encode(Constants.ASSISSTments_URL, "UTF-8") + tutorURL;
					fullName = URLEncoder.encode(fullName, "UTF-8");
				} catch (UnsupportedEncodingException e) {
					throw new RuntimeException(e);
				}
				String loginURL = Constants.LOGIN_URL;
				String addressToGo = String.format("%1$s?partner=%2$s&access=%3$s&on_success=%4$s&on_failure=%5$s", 
						loginURL, Utility.PARTNER_REFERENCE, onBehalf, tutorURL, Utility.LOGIN_FAILURE);
				try {
					addressToGo = URLEncoder.encode(addressToGo, "UTF-8");
				} catch (UnsupportedEncodingException e) {
					throw new RuntimeException(e);
				}
				String tutorLink = Constants.CONNECTOR_HTTPS_URL + "tutor?tutor_link="
						+ addressToGo + "&student_name="+fullName;
				/*
				try {
					tutorLink = URLEncoder.encode(tutorLink, "UTF-8");
				} catch (UnsupportedEncodingException e) {
					throw new RuntimeException(e);
				}*/
				toolProvider.setRedirectUrl(tutorLink);
			}
		}		
		return true;
	}
}
