<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>ASSISTments</title>
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/pure-release-0.6.0/pure-min.css">
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/stylesheets/styles.css">
	<style type="text/css">
div.pure-control-group label.pure-u-1 {
    width:auto;
}
</style>
</head>
<body>
	<div id="page-wrap">
		<div style="padding: 20px 0 0 0; background-color: #E1E6E6;">
			<img alt="ASSISTments"
				src="${pageContext.request.contextPath}/images/AssistmentsTMBlack.png">
		</div>
		
		<form action="SelectForm" method="post"
			class="pure-form pure-form-aligned" style="margin-top: 40px;">
			<input type="hidden" id="return_url"
				name="return_url" value=<%=request.getAttribute("return_url") == null ? "" : request.getAttribute("return_url")%>>
			<div class="pure-control-group">
				<label for="email" class="pure-u-1">Please enter the survey url</label> <input type="text" name="problem_set_id" id="problem_set_id"
					placeholder="Survey URL" required>
					<input type="submit" value="Select"
					class="pure-button pure-button-primary">
			</div>
			<!-- 
			<div class="pure-controls">
				<input type="submit" value="Select"
					class="pure-button pure-button-primary">
			</div>
			 -->
		</form>
	</div>
</body>
</html>