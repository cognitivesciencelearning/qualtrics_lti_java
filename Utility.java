package org.assistments.connector.lti;

import java.net.URLEncoder;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.http.HttpSession;

import org.assistments.connector.controller.AssignmentController;
import org.assistments.connector.controller.StudentClassController;
import org.assistments.connector.controller.UserController;
import org.assistments.connector.domain.StudentClass;
import org.assistments.connector.utility.Constants;
import org.assistments.connector.utility.Response;
import org.assistments.dao.controller.ExternalAssignmentDAO;
import org.assistments.dao.controller.ExternalStudentClassDAO;
import org.assistments.dao.domain.ExternalAssignment;
import org.assistments.dao.domain.ExternalStudentClass;
import org.oscelot.lti.tp.ToolProvider;
import org.oscelot.lti.tp.User;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Utility {
	
	public static final String PARTNER_REFERENCE = "Hien-Ref";
	public static final String PARTNER_ID = "3";
	public static final int CLASS_SECTION = 2;
	public static final String COURSE_NUMBER = "1";
	public static final String SECTION_NUMBER = "1";
	public static final String PASSWORD = "12345";
	public static final int USER_TYPE_ID = 1;
	public static final String TIMEZONE = "GMT-4";
	public static final String REGISTRATION_CODE = "HIEN-API";
	public static final String EDMODO_SCHOOL_REF = "bd03e53e4801837807c5e023ffa816d9";
	public static final String STUDENT_REPORT_SUFFIX = "_student_report_url";
	
	static final String m_prefix = "PS";
    static final String m_version = "A";
    
    public static void redirectToInstructionPage(ToolProvider toolProvider, HttpSession session, String msg, String instruction) {
    	session.setAttribute("message", msg);
		session.setAttribute("instruction", instruction);
//		String reqURL= toolProvider.getRequest().getRequestURL().toString();
//		String servletName = toolProvider.getRequest().getServletPath();
//		String regex = servletName+"$";
		String url = Constants.CONNECTOR_URL + "instruction";
		toolProvider.setRedirectUrl(url);
    }
    
    public static String generateResourceLinkId(String userRef, String assignmentRef) {
    	return userRef + "_" + assignmentRef + "_" + "resourceLink";
    }
    
    public static String generateLTIUserId(String userRef) {
    	return userRef + "_ltiUser";
    }
	
	/*
	 * Transfer user from LTI to ASSISTments Token for Dao is currently the LTI
	 * user's ID
	 */
    /*
	public static ArrayList<String> transferUser(ToolProvider toolProvider,
			String outside_access_token,
			String currently_loggin_outside_user_token) {
		String userRef = "";
		String accessToken = "";
		ArrayList<String> assistAccount = new ArrayList<String>();

		String partnerExternalRef = toolProvider.getUser().getId();

		ExternalUser externalUser = new ExternalUser(PARTNER_REFERENCE);
		if (toolProvider.getUser().getId()
				.equals(currently_loggin_outside_user_token))
			externalUser.setUser_connector_token(outside_access_token);
		else
			externalUser.setUser_connector_token("");
		
		externalUser.setPartner_external_reference(partnerExternalRef);
		externalUser.setExternal_refernce_type_id(USER_TYPE_ID);

		org.assistments.connector.domain.User assistUser = getUserInfo(toolProvider);

		ExternalUserDAO userDAO = new ExternalUserDAO(PARTNER_REFERENCE);
		if (!userDAO.isUserExist(partnerExternalRef)) {
			// create new User
			Response r = UserController.createUser(assistUser,
					Utility.PARTNER_REFERENCE);
			if (r.getHttpCode() == 201) {
				JsonElement jElement = new JsonParser().parse(r.getContent());
				JsonObject jObject = jElement.getAsJsonObject();
				userRef = jObject.get("user").getAsString();
			}
			if (userRef == null || userRef.equals("")) {
				return null;
			}
			externalUser.setExternal_refernce(userRef);

			accessToken = createAccessToken(userRef);

			if (accessToken == null) {
				return null;
			}
			externalUser.setUser_access_token(accessToken);
			externalUser.setUser_connector_token(toolProvider.getConsumer().getKey());

			// update db
			userDAO.addNewUser(externalUser);
			
			//Assign user to Edmodo
			String school_ref = Utility.EDMODO_SCHOOL_REF;
			SchoolController.assignUserToSchool(userRef, school_ref, Connect.partnerRef, accessToken);
		} else {
			externalUser = userDAO.findByPartnerExternalRef(partnerExternalRef);
			
			if(externalUser.getUser_access_token() == null || externalUser.getUser_access_token() == "") {
				String tmp = createAccessToken(externalUser.getExternal_refernce());
				externalUser.setExternal_refernce(tmp);
			}
			// need to update outside access_token because change frequently
			externalUser.setUser_connector_token(outside_access_token);
			userDAO.update(externalUser);
		}
		assistAccount.add(externalUser.getExternal_refernce());
		assistAccount.add(externalUser.getUser_access_token());

		return assistAccount;
	}*/
	/**
	 * This method will check if class already exists. If not, a new class will be created and return class ref.
	 * Otherwise, it will return class ref from partner_external_references table
	 *  
	 * @param contextTitle -- class name from outsider
	 * @param contextID -- class id from outsider
	 * @param partnerRef
	 * @param onBehalf
	 * @return class reference from ASSISTments
	 */
	public static String createClass(String contextTitle, String contextID, String partnerRef,
			String onBehalf) {
		String classRef = "";

		try {
			//TODO : 
			String partnerExternalRef = contextTitle + contextID;
			ExternalStudentClass externalClass = new ExternalStudentClass(partnerRef);
			externalClass.setPartner_reference(PARTNER_REFERENCE);
			externalClass.setExternal_refernce_type_id(CLASS_SECTION);
			externalClass.setPartner_external_reference(partnerExternalRef);
			ExternalStudentClassDAO classDAO = new ExternalStudentClassDAO(partnerRef);
			if (!classDAO.isClassExist(partnerExternalRef)) {
				StudentClass assisTmentsClass = new StudentClass(contextTitle);
				Response r  = StudentClassController.createStudentClass(assisTmentsClass, partnerRef, onBehalf);
				
				if(r.getHttpCode() == 201) {
					classRef = parseClassJson(r.getContent());
				} else {
					//prevent adding new entry into database
					return null;
				}

				// insert new school into db
				externalClass.setExternal_refernce(classRef);
				externalClass.setUser_access_token(onBehalf); // who create the
																// class
				classDAO.addNewClass(externalClass);

			} else {
				externalClass = classDAO.findByPartnerExternalRef(partnerExternalRef);
				classRef = externalClass.getExternal_refernce();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return classRef;
	}
	
	public static String fetchClassRef(String contextID, String contextTitle, String onBehalf) {
		String classRef = "";
		try {
			
			ExternalStudentClassDAO classDAO = new ExternalStudentClassDAO(PARTNER_REFERENCE);
			String partnerExternalRef = contextTitle + contextID;
			if (!classDAO.isClassExist(partnerExternalRef)) {
				return null;
			} else {
				ExternalStudentClass studentClass = classDAO.findByPartnerExternalRef(partnerExternalRef);
				classRef = studentClass.getExternal_refernce();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return classRef;
	}
	
	public static String getAssignment(String appId, String classRef) {
		String assignmentRef = "";
		try {
			
			ExternalAssignment assignment = new ExternalAssignment(PARTNER_REFERENCE);
			assignment.setExternal_refernce_type_id(Constants.EXTERNAL_ASSIGNMENT_TYPE_ID);
			//TODO: I'm not sure this is the best way to set partner external reference. (Duplicate appid in the same class) 
			String partnerExternalRef = appId + classRef;
			assignment.setPartner_external_reference(partnerExternalRef);
			
			ExternalAssignmentDAO assignmentDAO = new ExternalAssignmentDAO(PARTNER_REFERENCE);
			if (assignmentDAO.isAssignmentExist(partnerExternalRef)) {
				assignment = assignmentDAO.findByPartnerExternalRef(partnerExternalRef);
				assignmentRef = assignment.getExternal_refernce();
			}
			
		}catch (Exception e) {
			e.printStackTrace();
		}
		return assignmentRef;
	}
	
	public static String createAssignment(String appId, String classRef, String onBehalf) 
			throws ReferenceNotFoundException{
		String assignmentRef = "";
		
		try {
			
			ExternalAssignment assignment = new ExternalAssignment(PARTNER_REFERENCE);
			assignment.setExternal_refernce_type_id(Constants.EXTERNAL_ASSIGNMENT_TYPE_ID);
			//TODO: I'm not sure this is the best way to set partner external reference. (Duplicate appid in the same class) 
			String partnerExternalRef = appId + classRef;
			assignment.setPartner_external_reference(partnerExternalRef);
			
			ExternalAssignmentDAO assignmentDAO = new ExternalAssignmentDAO(PARTNER_REFERENCE);
			if (!assignmentDAO.isAssignmentExist(partnerExternalRef)) {
				Response r = AssignmentController.createAssignment(appId, classRef, Utility.PARTNER_REFERENCE, onBehalf);
				if(r.getHttpCode() == 201) {
					assignmentRef = parseAssignmentJson(r.getContent());
					assignment.setExternal_refernce(assignmentRef);
					assignmentDAO.addNewAssignment(assignment);
				} else if(r.getHttpCode() == 404){
					throw new ReferenceNotFoundException(r.getContent());
				} else {
					return null;
				}
			}
			else {
				assignment = assignmentDAO.findByPartnerExternalRef(partnerExternalRef);
				assignmentRef = assignment.getExternal_refernce();
			}
			
		}catch (Exception e) {
			e.printStackTrace();
		}
		return assignmentRef;
	}
	
	private static String parseAssignmentJson(String assingmentRef) {
		JsonElement jEelement = new JsonParser().parse(assingmentRef);
	    JsonObject  jObject = jEelement.getAsJsonObject();
	    String assignmentRef = jObject.get("assignment").toString(); // "d15daef3972c4a7528afd6f97e9f536a"
	    assignmentRef = assignmentRef.substring(1, assignmentRef.length()-1);
	    return assignmentRef;

	}

	protected static org.assistments.service.domain.User getUserInfo(
			ToolProvider toolProvider) {
//		org.assistments.connector.domain.User user = new org.assistments.connector.domain.User();
		org.assistments.service.domain.User user = new org.assistments.service.domain.User();
		User ltiUser = toolProvider.getUser();
		// principal or proxy by TEACHER or STUDENT
		if (ltiUser.isAdmin() || ltiUser.isStaff()) {
			user.setUserType(Constants.PRINCIPAL);
			user.setPassword(Utility.PASSWORD);
			user.setFirstName(ltiUser.getFirstname());
			user.setLastName(ltiUser.getLastname());

			// create a unique timestamp so email address and username is unique
			Long  time = uniqueCurrentTimeMS();
			String email = time.toString() + "@lti.com";
			user.setEmail(email);
			user.setUsername(email);
			user.setDisplayName(ltiUser.getFullname());
			user.setTimeZone(Utility.TIMEZONE);
			user.setRegistrationCode(Utility.REGISTRATION_CODE);
//		} else if (ltiUser.isLearner()) {
		} else {
			user.setUserType(Constants.PROXY);
			user.setPassword(Utility.PASSWORD);
			user.setFirstName(ltiUser.getFirstname());
			user.setLastName(ltiUser.getLastname());
//			user.setEmail(ltiUser.getEmail());
			Long  time = uniqueCurrentTimeMS();
			String email = time.toString() + "@lti.com";
			user.setEmail(email);
			user.setUsername(ltiUser.getFullname());
			user.setDisplayName(ltiUser.getFullname());
			user.setTimeZone(Utility.TIMEZONE);
			user.setRegistrationCode(Utility.REGISTRATION_CODE);
		}

		return user;
	}
	
	public static String getUserPageURL(String userType, String partnerRef, String onBehalf, String assignmentRef ) {
		
		String addressToGo = "";
		
		String onFailure = "www.assistments.org";
		String onExit = Constants.CONNECTOR_HTTPS_URL + "LTIGradeRequest";
//		String from = "LTI";
		
		if (userType.equals(Constants.TEACHER_ROLE)) {
//			String onSuccess = Constants.TEACHER_PAGE+"/"+appId+"?from="+from+"&report="+report;
			String onSuccess = Constants.ASSISSTments_URL + "external_teacher/student_class/assignment?ref=" + assignmentRef;
			
			try {
				onSuccess = URLEncoder.encode(onSuccess, "UTF-8");
			} catch (Exception e) {
				e.printStackTrace();
			}
			addressToGo = String.format("%1$s?partner=%2$s&access=%3$s&on_success=%4$s&on_failure=%5$s", Constants.LOGIN_URL, partnerRef, onBehalf, onSuccess, onFailure);
		}else if (userType.equals(Constants.STUDENT_ROLE)) {
			String onSuccess = Constants.ASSISSTments_URL + "external_tutor/student_class/assignment?ref=" + assignmentRef 
				+"&onExit="+onExit+"&partnerID="+Utility.PARTNER_ID;
			try {
				onSuccess = URLEncoder.encode(onSuccess, "UTF-8");
			} catch (Exception e) {
				e.printStackTrace();
			}
 			addressToGo = String.format("%1$s?partner=%2$s&access=%3$s&on_success=%4$s&on_failure=%5$s", Constants.LOGIN_URL, partnerRef, onBehalf, onSuccess, onFailure);
		}
		return addressToGo;
	}
	
	public static String getUserPageURL(String userType, String partnerRef, String onBehalf, String appId, String report) {
		
		String addressToGo = "";
		
		String onFailure = "www.assistments.org";
//		String onExit = "http://www.assistments.org:8080/connector/LTIGradeRequest";
		String onExit = Constants.CONNECTOR_HTTPS_URL + "LTIGradeRequest";
		String from = "LTI";
		
		if (userType.equals(Constants.TEACHER_ROLE)) {
			String onSuccess = Constants.TEACHER_PAGE+"/"+appId+"?from="+from+"&report="+report;
			try {
				onSuccess = URLEncoder.encode(onSuccess, "UTF-8");
			} catch (Exception e) {
				e.printStackTrace();
			}
			addressToGo = String.format("%1$s?partner=%2$s&access=%3$s&on_success=%4$s&on_failure=%5$s", Constants.LOGIN_URL, partnerRef, onBehalf, onSuccess, onFailure);
		}else if (userType.equals(Constants.STUDENT_ROLE)) {
//			onExit = Constants.STUDENT_PAGE+"/"+appId+"?onExit=null&partner_id=3&from="+from;

			String onSuccess = Constants.STUDENT_PAGE+"/"+appId+"?onExit="+onExit+"&partner_id=3&from="+from;
			try {
				onSuccess = URLEncoder.encode(onSuccess, "UTF-8");
			} catch (Exception e) {
				e.printStackTrace();
			}
 			addressToGo = String.format("%1$s?partner=%2$s&access=%3$s&on_success=%4$s&on_failure=%5$s", Constants.LOGIN_URL, partnerRef, onBehalf, onSuccess, onFailure);
		}
		return addressToGo;
	}
	
	/**
	 * Create access token inside ASSISTments for this user based on user external reference
	 * Before calling this function, make sure the user does not have access token
	 * @param userRef -- use external reference inside ASSISTments
	 * @return access  token inside ASSSISTments
	 */
	public static String createAccessToken(String userRef) {
		String accessToken= "";
//			ASSISTmentsRequest assistmentsRequest = new ASSISTmentsRequest();
//			assist_access_token = assistmentsRequest.createAccessToken(userRef);
		Response r = UserController.createAccessToken(userRef, Utility.PARTNER_REFERENCE);
		if(r.getHttpCode() < 400) {
			JsonElement jElement = new JsonParser().parse(r.getContent());
			JsonObject jObject = jElement.getAsJsonObject();
			accessToken = jObject.get("access").getAsString();
		} else {
			return null;
		}
		return accessToken;
	}
	
	private static String parseClassJson(String classRefJson) {
		JsonElement jEelement = new JsonParser().parse(classRefJson);
		JsonObject jObject = jEelement.getAsJsonObject();
		String classRef = jObject.get("class").toString(); // "d15daef3972c4a7528afd6f97e9f536a"
		classRef = classRef.substring(1, classRef.length() - 1);
		return classRef;

	}
	private static final AtomicLong LAST_TIME_MS = new AtomicLong();
	public static final String LOGIN_FAILURE = "www.assistments.org";
	public static final String ERROR_SOURCE_TYPE = "Connector";
	public static long uniqueCurrentTimeMS() {
	    long now = System.currentTimeMillis();
	    while(true) {
	        long lastTime = LAST_TIME_MS.get();
	        if (lastTime >= now)
	            now = lastTime+1;
	        if (LAST_TIME_MS.compareAndSet(lastTime, now))
	            return now;
	    }
	}
	
	/**
	 * Convert encoded problem set string to problem set id
	 * @param psString -- encoded problem set string (PSxxxx)
	 * @return problem set number id (a String)
	 */
	public static String decodeProblemSetString(String psString) {
        if (psString.isEmpty()) {
            return null;
        }
        
        // decode prefix
        if (!psString.substring(0, m_prefix.length()).equalsIgnoreCase(m_prefix)) {
            return null;
        }
        
        // decode version
        if (!psString.substring(m_prefix.length(), m_prefix.length() + m_version.length()).equalsIgnoreCase(m_version)) {
            return null;
        }
        
        // decode problem id
        String code = "abcdefghjkmnpqrstuvwxyz23456789";
        int decodedId = 0;
        String psStringLowerCase = psString.toLowerCase();
        for (int i = m_prefix.length() + m_version.length(); i < psStringLowerCase.length(); i++) {
            char c = psStringLowerCase.charAt(i);
            int oldValue = decodedId;
            decodedId = decodedId * code.length() + code.indexOf(c);
            if (decodedId < oldValue) {
                return null;
            }
        }
        return new Integer(decodedId).toString();
    }

	public static String generateStudentReportURL(String userRef,
			String assignmentRef) {
		return Constants.CONNECTOR_HTTPS_URL + "studentReport?assignment_ref=" + assignmentRef
				+ "&student_ref=" + userRef;
	}

	public static String generateStudentReportId(String studentRef,
			String assignmentRef) {
		return studentRef + "_" + assignmentRef + STUDENT_REPORT_SUFFIX;
	}
	
	public static String generateStudentReportURL(String studentRef,
			String assignmentRef, String from) {
		return Constants.CONNECTOR_HTTPS_URL + "studentReport?assignment_ref=" + assignmentRef
				+ "&student_ref=" + studentRef + "&from=" + from;
	}
}
