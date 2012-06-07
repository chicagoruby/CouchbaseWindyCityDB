<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@page import="java.util.Iterator"%>
<%@page import="java.util.List"%>
<%@page import="net.beer.crud.CouchbaseHelper"%>
<%
    CouchbaseHelper ch = new CouchbaseHelper();
 
    String query = request.getParameter("q");
 
    List<String> breweries = ch.getBreweries(query);
 
    Iterator<String> iterator = breweries.iterator();
    while(iterator.hasNext()) {
        String brewery = (String)iterator.next();
        out.println(brewery);
    }
%>