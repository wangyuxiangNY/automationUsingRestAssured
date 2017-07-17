package com.aetion.automationUsingRestAssured;


import static io.restassured.RestAssured.given;

import static io.restassured.path.json.JsonPath.with;
import static io.restassured.matcher.RestAssuredMatchers.*;
import static org.hamcrest.Matchers.notNullValue;
import io.restassured.RestAssured;
import io.restassured.parsing.Parser;
import io.restassured.response.Response;


import static org.junit.Assert.*; 

import org.junit.Test; 
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Before; 
import org.junit.After; 
import org.junit.Rule;
import org.junit.rules.TestName;


public class TestWebService {
	
	@Rule public TestName name = new TestName();
	private static ServiceClient client;
	
	//hold all the error messages 
	public static StringBuffer errors = new StringBuffer(); 
	private  static final String userFile = "users.txt";
	private  static final String userToModifyFile = "usersToCorrect.txt";
	 
	@BeforeClass
    public static void prepare() throws Exception
	{
		client = new ServiceClient();
		client.login();
		client.createUsers(userFile);
	
    }
	
	
	@Before
    public void init() {
		//clear the error from last run.
		client.getErrors().delete(0, client.getErrors().length());
    }
	

    @Ignore("skip")
    public void testLogin() throws Exception
    {
        System.out.println("test method:" +  name.getMethodName() );
        client.login();
       
        //Really need to do this?
       
         System.out.println(name.getMethodName() + " is Done.");
            
    }

	
	    
    @Ignore("skip")
    public void testLogin_badCredential() throws Exception
    {
        System.out.println("test method:" +  name.getMethodName() );
        try{
        	client.login("what", "ever");
         }catch(Exception e)
         {
             handleException(e);
         }
        
         System.out.println(name.getMethodName() + " is Done.");
            
    }
	    
	    
    @Test
    public void testCreateUsers() throws Exception
    {
        System.out.println("test method:" +  name.getMethodName() );
        try{
        	client.createUsers(userFile);
         }catch(Exception e)
         {
             handleException(e);
         }
        
         System.out.println(name.getMethodName() + " is Done.");
            
    }

    @Test
    public void testSearch() throws Exception
    {
        System.out.println("test method:" +  name.getMethodName() );
        try{
        	client.search(35, 45);
         }catch(Exception e)
         {
             handleException(e);
         }
        
         System.out.println(name.getMethodName() + " is Done.");
            
    }
	    

    @Test
    public void testGetUser() throws Exception
    {
        System.out.println("test method:" +  name.getMethodName() );
        try{
        	ServiceClient.getUser(2);
         }catch(Exception e)
         {
             handleException(e);
         }
        
         System.out.println(name.getMethodName() + " is Done.");
            
    }
	    
    @Test
    public void testModifyUser() throws Exception
    {
        System.out.println("test method:" +  name.getMethodName() );
        try{
        	client.modifyUser(userToModifyFile);
         }catch(Exception e)
         {
             handleException(e);
         }
        
         System.out.println(name.getMethodName() + " is Done.");
            
    }
	    
     @After
    public void tearDown() throws Exception{
	   
	 if (client.getErrors().length() > 0)
		 fail(ServiceClient.getErrors().toString());
    	
    }

    private void handleException(Exception e)
    {   
        e.printStackTrace();
        fail("Exception is thrown.");
        
    }


}
