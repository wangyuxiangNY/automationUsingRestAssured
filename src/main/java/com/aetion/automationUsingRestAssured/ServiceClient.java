package com.aetion.automationUsingRestAssured;


import static io.restassured.RestAssured.*;
import io.restassured.RestAssured;
import io.restassured.parsing.Parser;

import java.io.BufferedReader;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.io.FileReader;
import java.util.StringTokenizer;


public class ServiceClient {

	private static final String BASE_URI = "http://qa-takehome.dev.aetion.com:4440";
	private static final String SERVICE_URL_LOGIN = "/login";
	private static final String SERVICE_URL_USER = "/user/";
	private static final String SERVICE_URL_SEARCH_USER = "/user/search";
	
    private static final String  USER_NAME = "mwang";
    private static final String PASSWORD = "mwang123";
    
	private static String authToken;
	
	//For faster search, Map user's email as key in the hashMap
	Map<String, User> userList;
	
	//hold all the verification error messages 
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
    	
    public  void loginWithBadCredential() throws Exception
    {   
    	String bad_user = "what";
    	String bad_password ="ever";
		String json = "{\"username\":\"" + bad_user + "\",\"password\":\""  + bad_password + "\"} ";

		System.out.println("see json:" + json);
		
        given()
    	.contentType("application/json").
    	body(json).
        when().
        post(BASE_URI+ SERVICE_URL_LOGIN).then().statusCode(401);
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
    	
    /*
     * create users from the data file as following: 
     * 1. Read in data file and parse each line
     * 2. Send POST reqeust to create user, and put response in userList for late verification
     * 3. Verify the created users with the original data, to make sure data are not corrupted. 
     * 
     * @param userFile the plain .txt file holding all the data info
     */
    public  void createUsers(String userFile) throws Exception
    {   //First, get all user data
    	System.out.println("about to read user data from the file");
    	
    	List<User> users = new ArrayList<User>();
    	users = readInUserData(userFile);
    	System.out.println("user count:" + users.size());
    	
    	for (User user: users)
    	{
    		String jsonBody = "{\"email\":\""+ user.getEmail() + "\",\"first_name\":\"" + user.getFirst_name() 
 			+ "\", \"last_name\":\"" + user.getLast_name() + "\",\"age\":\" " + user.getAge() + "\"} ";

    		User createdUser = 
	    	given().
	    	header("X-Auth-Token", authToken).
	    	header("content-type", "application/json").
	    	body(jsonBody).
	    	//log().all().
	    	when().
	    	post(BASE_URI+ SERVICE_URL_USER).as(User.class);
	    	

    		
    		userList.put(user.getEmail(), createdUser);
    		
	    	System.out.println("Verifying details for userID:" + createdUser.getId());
	    	//Verify that ID is set and is bigger than 0,
    	    if (createdUser.getId() < 0)
    	    	errors.append("Generated ID is not positive.");
    	    
    	    verifyUserDetailsExceptID(user, createdUser);
    			
    	}	
    }
    
    
    private void verifyUserID(User actual, User expected)
    {
    	//Verify that ID is set and is bigger than 0,
        if (actual.getId() < 1) errors.append("ID is not correct.");
 	    
 	    //verif user ID is not altered.
        if(actual.getId()!= expected.getId())
 	    	errors.append("ID doesn't match.");
    }
    
    private static void verifyUserDetailsExceptID(User actual, User expected)
    {
	    if(!actual.getEmail().equals(expected.getEmail()))
	    	errors.append("email is not right.");
	    if(!actual.getFirst_name().equals(expected.getFirst_name()))
	    	errors.append("first_name is not right.");;
	    if(!actual.getLast_name().equals(expected.getLast_name()))
	    	errors.append("last_name is not right.");;
	    if(actual.getAge() != expected.getAge())
	    	errors.append("age is not right.");;
    }
    
    /*
     * Get users in the userList one by one and verify each users details to make sure that they matches.
     */
    public void getUsers()
    {
    	User originalUser, retrievedUser;
    	Iterator iterator = userList.entrySet().iterator();
    	while (iterator.hasNext())
    	{   
            Map.Entry pair = (Map.Entry)iterator.next();
    		originalUser = (User)pair.getValue();
    		retrievedUser = getUser(originalUser.getId());
    		verifyUserID(originalUser, retrievedUser);
    		verifyUserDetailsExceptID(originalUser, retrievedUser);
    	}
    }
    private  User getUser(int userID)
    {   
    	 User retrievedUser = 	given().
    	 header("X-Auth-Token", authToken).
    	 header("content-type", "application/json").
    	 log().all().
    	 when().
    	 get(BASE_URI+ SERVICE_URL_USER + userID).as(User.class);
    	 
    	 System.out.println("see retrieved user:" + retrievedUser.toString());
    	
    	 return retrievedUser;
    	
    	
    }
    
    /* Send PUT request to modify those users' corresponded info. 
     * To modify a user's info, we first need to find out its ID based  its email. Thus the HashMap<String, User> userList
     * Steps: 
     * 1. Read in data from the file
     * 2. parse each line, trace out user ID based on email, and replace the old value with new
     * 3. Send PUT request over with new value for modification
     * 4. Verify the result by comparing the data returned in response with the expected value
     * 
     * @param usersToModifyFile: plain txt file holidng '3 Users to correct' info 
     */
    
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
    	        	      System.out.println("old/new:" + oldValue + "/" + newValue);
    	        	 }
    	        	 
    	        	 //Now trace out userID and go modify
    	        	 User userToModify = userList.get(email);
    	        	 if (userToModify == null)
    	        		 System.out.println("userToModify is null.");
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
    	       
    	        	 System.out.println("MAKE SURE: " +  userToModify.getAge() );
    	        	 
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
		
		verifyUserID(response, newData);
		verifyUpdate(response, newData);
		
    }
    	
    private static void verifyUpdate(User actual, User expected)
    {
	    if(!actual.getEmail().equals(expected.getEmail()))
	    	errors.append("email is not updated.");
	    if(!actual.getFirst_name().equals(expected.getFirst_name()))
	    	errors.append("first_name is not updated.");;
	    if(!actual.getLast_name().equals(expected.getLast_name()))
	    	errors.append("last_name is not updated.");;
	    if(actual.getAge() != expected.getAge())
	    	errors.append("age is not updated.");;
    }
    
    /* Search users whose age falls within the specified range
     *
     * @param startAge
     * @param endAge
     * @return qualified users
     */
    public  User[] search(int startAge, int endAge)
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
	    {	System.out.println("Search result:" +  user.toString());
	        //Verify retrieved result is right
	    	if (user.getAge()> 45 || user.getAge()<35)
	    		errors.append("Search result is not right.");
	    }
	    return response;
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
