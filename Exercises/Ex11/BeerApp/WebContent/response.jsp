<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<script language="javascript">
function adjust(obj)
{
if(obj.name == "Create")
{
document.test.action = "beercreate.jsp";
alert("b1 was clicked");
return true;
}
if(obj.name == "")
{
document.test.action = "http://appsr/webapp/Dacs/left.html";
alert("b2 was clicked");
return true;
}
if(obj.name == "B3")
{
document.test.action = "http://appsr/webapp/Dacs/right.html";
alert("b3 was clicked");
return true;
}
}
</script>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Beer Details</title>
    <link rel="stylesheet" type="text/css" href="css/jquery.autocomplete.css" />
    <script type="text/javascript"
            src="http://ajax.googleapis.com/ajax/libs/jquery/1.3.2/jquery.min.js"></script>
    <script src="js/jquery.autocomplete.js"></script>
    <style>
        input {
            font-size: 120%;
        }
    </style>
</head>
<body>

    <jsp:useBean id="couchbean" scope="session" class="net.beer.crud.CouchbaseHelper" />
    <jsp:setProperty name="couchbean" property="id" param="beer"/>

<p>
     Beer  <b><%= request.getParameter("beer") %></b> Details

    <form name="Beer Form" action="beerCRUD.jsp">
    Key:
    <input type="text" id="Id" name="Id" value=<%= request.getParameter("beer") %>>
    <br>
    Name:
    <input type="text" id="name" name="name" value='<jsp:getProperty name="couchbean" property="name"/>'>
    <br>
    Brewery Id:
    <input type="text" id="breweryId" name="breweryId" value=<jsp:getProperty name="couchbean" property="breweryId"/>>
 
    <script>
        $("#breweryId").autocomplete("getBreweries.jsp");
    </script>
    <br>
    ABV:
    <input type="text" id="abv" name="abv" value=<jsp:getProperty name="couchbean" property="abv"/>>
    <p>
    <%-- <a href="beercreate.jsp?beer="><input type="submit" value="Create"/></a> --%>
    <input type="submit" value="Create" name="Action">
    <input type="submit" value="Update" name="Action"/>
    <input type="submit" value="Delete" name="Action"/>
    <input type="submit" value="Cancel" name="Action"/>
    </form>
</body>
</html>