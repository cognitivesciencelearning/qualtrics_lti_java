package org.assistments.connector.lti;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.oscelot.lti.tp.Callback;
import org.oscelot.lti.tp.ToolProvider;

@WebServlet({ "/Select", "/select" })
public class Select  extends HttpServlet {

	private static final long serialVersionUID = 6328194247027472343L;

	public Select() {
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
		String returnURL = req.getParameter("launch_presentation_return_url");
		req.setAttribute("return_url", returnURL);
		RequestDispatcher dispatcher = req.getRequestDispatcher("/select.jsp");
		dispatcher.forward(req, resp);
	}
	
	class DoLaunch implements Callback {

		@Override
		public boolean execute(ToolProvider toolProvider) {
			
			return true;
		}
		
	}
}