<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Beer Create/Update/Delete</title>
</head>
    <style>
        output {
            font-size: 150%;
        }
    </style>
<body>


Beer Create/Update/Delete <p>

   <jsp:setProperty name="couchbean" property="name" param="name"/>
   <jsp:setProperty name="couchbean" property="breweryId" param="breweryId"/>
   <jsp:setProperty name="couchbean" property="abv" param="abv"/>
   
   <FONT SIZE=+2>
   <% if (request.getParameter("Action").equals("Create")) { %>
       <jsp:setProperty name="couchbean" property="id" param="Id"/>
       <jsp:setProperty name="couchbean" property="create" param="Id"/>
         
        <jsp:getProperty name="couchbean" property="id"/> Create with Status as <jsp:getProperty name="couchbean" property="status"/>
   <%} %>
   <% if (request.getParameter("Action").equals("Update")) { %>
       <jsp:setProperty name="couchbean" property="update" param="Id"/>

        <%= request.getParameter("Id") %> Update with Status as <jsp:getProperty name="couchbean" property="status"/>       
   <%} %>
   <% if (request.getParameter("Action").equals("Delete")) { %>
       <jsp:setProperty name="couchbean" property="delete" param="Id"/>
       <%= request.getParameter("Id") %> Delete with Status as <jsp:getProperty name="couchbean" property="status"/>
   <%} %>

   <% if (request.getParameter("Action").equals("Cancel")) { %>
       <%= request.getParameter("Id") %> Cancelled
   <%} %>  
 </FONT><BR>
    <form name="Beer Form" action="index.jsp">
 <p>

    <input type="submit" value="OK" name="Action"/>
    </form>
          
<jsp:useBean id="couchbean" scope="session" class="net.beer.crud.CouchbaseHelper" />

</body>
</html>