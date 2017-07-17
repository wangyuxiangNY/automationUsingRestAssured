package com.aetion.automationUsingRestAssured;

public class User {

	private String email, first_name, last_name;
	private int age, id;
	
	public User()
	{
		
	}
	
	public User(String email, String first_name, String last_name, int age, int id)
	{
		this.email = email;
		this.first_name = first_name;
		this.last_name = last_name;
		this.age = age;
		this.id = id;
	}
	
	public String getEmail()
	{
		return email;
	}
	
	public String getFirst_name()
	{
		return first_name;
	}
	
	public String getLast_name()
	{
		return last_name;
	}
	
	public int getAge()
	{
		return age;
	}
	
	public int getId()
	{
		return id;
	}
	
	
	public void setEmail(String email)
	{
		 this.email = email;
	}
	
	public void setFirst_name(String first_name)
	{
		this.first_name = first_name;
	}
	
	public void setLast_name(String last_name)
	{
		this.last_name =  last_name;
	}
	
	public void  setAge(int age)
	{
		this.age = age;
	}
	
	
	
	public String toString()
	{
		return ("userID:" + id +"\t" + 
	               "first name: " + first_name + "\t" +
				    "last Name:" + last_name + "\t" + 
	               "email:" + email + "\t" +
				    "age:" + age );
	}
}
