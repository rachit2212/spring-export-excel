<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>POC</title>
</head>
<body>
	<form action="./getExcel" method="post">
		CompanyId: <input type="number" name="companyId" /><br />
		<!-- Mock response: <input type="text" name="mockResponse" /><br /> -->
		<button type="submit">Start POC</button>
	</form>
</body>
</html>