package com.aetion.automationUsingRestAssured;


import static io.restassured.RestAssured.*;
import io.restassured.RestAssured;
import io.restassured.parsing.Parser;

import java.io.BufferedReader;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.io.FileReader;
import java.util.StringTokenizer;

import static org.junit.Assert.assertTrue;

public class ServiceClient {

	private static final String BASE_URI = "http://qa-takehome.dev.aetion.com:4440";
	private static final String SERVICE_URL_LOGIN = "/login";
	private static final String SERVICE_URL_USER = "/user/";
	//private static final String SERVICE_URL_GET_USER = "/user/{id}";
	private static final String SERVICE_URL_SEARCH_USER = "/user/search";
	
    private static final String  USER_NAME = "mwang";
    private static final String PASSWORD = "mwang123";
    
	private static String authToken;
	
	//For faster search, Map user's email as key in the hashMap
	Map<String, User> userList;
	
	//hold all the error messages 
	public static StringBuffer errors = new StringBuffer(); 
	  
	
	public ServiceClient()
	{
		RestAssured.baseURI  =  BASE_URI;
		RestAssured.defaultParser = Parser.JSON;
		userList = new HashMap<String,User>();
	}
	
    public  void login() throws Exception
    {
    	login(USER_NAME, PASSWORD);
    }
    	
    public  void login(String userName, String password) throws Exception
    {
    	
		
		String json = "{\"username\":\"" + userName + "\",\"password\":\""  + password + "\"} ";

		System.out.println("see json:" + json);
		
        authToken =  given()
    	.contentType("application/json").
    	body(json).
        when().
        post(BASE_URI+ SERVICE_URL_LOGIN).then().statusCode(200).
        extract().
        path("token");
        
        System.out.println("See authToken:" + authToken);
		      
    }    
    	
    public  void createUsers(String userFile)
    {   //First, get all user data
    	System.out.println("about to read user data from the file");
    	
    	List<User> users = new ArrayList<User>();
    	users = readInUserData(userFile);
    	System.out.println("user count:" + users.size());
    	
    	for (User user: users)
    	{
    		String jsonBody = "{\"email\":\""+ user.getEmail() + "\",\"first_name\":\"" + user.getFirst_name() 
 			+ "\", \"last_name\":\"" + user.getLast_name() + "\",\"age\":\" " + user.getAge() + "\"} ";

    		User response = 
	    	given().
	    	header("X-Auth-Token", authToken).
	    	header("content-type", "application/json").
	    	body(jsonBody).
	    	log().all().
	    	when().
	    	post(BASE_URI+ SERVICE_URL_USER).as(User.class);
	    	
    		userList.put(user.getEmail(), response);
    		
	    	System.out.println("Verifying details for userID:" + response.getId());
	    	//Verify that ID is set and is bigger than 0,
    	    assertTrue(response.getId() > 0);
    	    
    	    //verif user data is still good.
    	    assertTrue(response.getEmail().equals(user.getEmail()));
    	    assertTrue(response.getFirst_name().equals(user.getFirst_name()));
    	    assertTrue(response.getLast_name().equals(user.getLast_name()));
    	    assertTrue(response.getAge() == user.getAge());
    			
    	}	
    }
    
    private static void verifyUserDetails(User actual, User expected)
    {
    	//Verify that ID is set and is bigger than 0,
    	
    	/*
	    assertTrue(actual.getId() > 0);
	    
	    //verif user data is still good.
	    assertTrue(actual.getEmail().equals(expected.getEmail()));
	    assertTrue(actual.getFirst_name().equals(expected.getFirst_name()));
	    assertTrue(actual.getLast_name().equals(expected.getLast_name()));
	    assertTrue(actual.getAge() == expected.getAge());
	    */
    	
       if (actual.getId() < 1) errors.append("ID is not correct.");
	    
	    //verif user data is still good.
	    if(!actual.getEmail().equals(expected.getEmail()))
	    	errors.append("email is not right.");
	    if(!actual.getFirst_name().equals(expected.getFirst_name()))
	    	errors.append("first_name is not right.");;
	    if(!actual.getLast_name().equals(expected.getLast_name()))
	    	errors.append("last_name is not right.");;
	    if(actual.getAge() != expected.getAge())
	    	errors.append("age is not right.");;
    }
    
    public static void getUser(int userID)
    {   
    	
    	/*
    	User response = 
    	    	given().
    	    	header("X-Auth-Token", authToken).
    	    	header("content-type", "application/json").
    	    	log().all().
    	    	when().
    	    	get(BASE_URI+ SERVICE_URL_USER + userID).as(User.class);
        	
    	  assertTrue(response.getId() == userID);  
    	  */
    	
    	    String response = 	given().
    	    	header("X-Auth-Token", authToken).
    	    	header("content-type", "application/json").
    	    	log().all().
    	    	when().
    	    	get(BASE_URI+ SERVICE_URL_USER + userID).getBody().asString();
    	    System.out.println("See getUser response:" + response);
    }
    
    
   
    
    //to modify, first need to find out the corresponding id for put request, then replace the field to modify
    public  void modifyUser(String usersToModifyFile)
    {   
    	BufferedReader br = null;
    	try{
    			br = new BufferedReader(new FileReader(usersToModifyFile));
    	}catch(Exception e)
    	{
    		e.printStackTrace();
    	}
    	
    	String email ="";
    	String itemToChange ="";
    	String oldValue="";
    	String newValue="";
    	
    	try {
    	   
    	    String line = br.readLine();

    	    while (line != null) {
    	        line = br.readLine();
    	        System.out.println("see line:" + line);
    	       
    	        if (line != null)
    	        {
    	        	//first find out what to userID, then what to modify..
    	        	StringTokenizer st = new StringTokenizer(line); //"\\t");
    	        	 while (st.hasMoreTokens()) {
    	        	      email = st.nextToken().trim();
    	        	      itemToChange = st.nextToken().trim();
    	        	      oldValue = st.nextToken().trim();
    	        	      newValue = st.nextToken().trim();
    	        	 }
    	        	 
    	        	 //Now trace out userID and go modify
    	        	 User userToModify = userList.get(email);
    	        	 switch (itemToChange) {
	    	             case "first_name":
	    	                 userToModify.setFirst_name(newValue);
	    	                 break;
	    	             case "last_name":
	    	                 userToModify.setLast_name(newValue);
	    	                 break;
	    	             case "email":
	    	                 userToModify.setEmail(newValue);
	    	                 break;
	    	             case "age":
	    	                 userToModify.setAge(Integer.valueOf(newValue));
	    	                 break;   
	    	             default:
	    	                 throw new IllegalArgumentException("Invalid field name: " + itemToChange);
    	             }
    	       
    	        	 modifyUser(userToModify.getId(), userToModify);
    	        }
    	    }
    	  
    	}catch(Exception e)
    	{
    		e.printStackTrace();
    	} finally {
    		try{
    	       br.close();
    		}catch(Exception e)
    		{
    			
    		}
    	}
    	
    	
    }
    
  
    private void modifyUser(int userID, User newData)
    {
    	String jsonString = "{\"id\":" + userID + ",\"email\":\""+ newData.getEmail() + "\",\"first_name\":\"" + newData.getFirst_name() 
		+ "\", \"last_name\":\"" + newData.getLast_name() + "\",\"age\":\" " + newData.getAge() + "\"} ";
       
    	System.out.println("modifyUser().. see json:" + jsonString);
    	
    
		User response = 
		given().
		header("X-Auth-Token", authToken).
		header("content-type", "application/json").
		body(jsonString).
		log().all().
		when().
		put(BASE_URI+ SERVICE_URL_USER + userID).as(User.class);
		
		verifyUserDetails(response, newData);
		
    }
    	
    
    
    public  void search(int startAge, int endAge)
    {   
        String jsonString = "{\"start_age\":" + startAge + "," +  "\"end_age\":" + endAge + "}";
	    System.out.println("See jsonString:" + jsonString);
    	
	    User[] response = 
	        	given().
	        	header("X-Auth-Token", authToken).
	        	header("content-type", "application/json").
	        	body(jsonString).
	        	log().all().
	        	when().
	        	post(BASE_URI+ SERVICE_URL_SEARCH_USER).as(User[].class);
	    
	    //List out users returned
	    for (User user: response)
	    	System.out.println(user.toString());
    }
    
    
    private  List<User> readInUserData(String file)
    {
    	List<User> users = new ArrayList<User>();
    	
    	BufferedReader br = null;
    	try{
    			br = new BufferedReader(new FileReader(file));
    	}catch(Exception e)
    	{
    		e.printStackTrace();
    	}
    	try {
    	   
    	    String line = br.readLine();

    	    while (line != null) {
    	        line = br.readLine();
    	        if (line != null)
    	           users.add(parse(line));
    	    }
    	  
    	}catch(Exception e)
    	{
    		e.printStackTrace();
    	} finally {
    		try{
    	       br.close();
    		}catch(Exception e)
    		{
    			
    		}
    	}
    	return users;
    }
    
    private static User parse(String userData)
    {   System.out.println("See userData:" + userData);
        List<String> fields = new ArrayList<String>();
    	StringTokenizer st = new StringTokenizer(userData); //"\\t");
    	 while (st.hasMoreTokens()) {
    	     fields.add(st.nextToken().trim());
    	 }
    	 
    	 return (new User((String)fields.get(0), (String)fields.get(1), (String)fields.get(2), Integer.parseInt(fields.get(3)), -1));
    }
    
    public static StringBuffer getErrors()
    {
    	return errors;
    }
}
